package com.neo4flix.ratingservice.service;

import com.neo4flix.ratingservice.dto.RatingRequest;
import com.neo4flix.ratingservice.dto.RatingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service de notation.
 *
 * Toutes les opérations passent par des requêtes Cypher directes via le Driver Neo4j.
 *
 * Pourquoi le Driver directement (pas Spring Data) ?
 *   → On travaille sur des RELATIONS entre nœuds existants.
 *   → Les nœuds User et Movie ont déjà été créés par d'autres microservices.
 *   → On ne recrée pas les nœuds, on crée/modifie juste la relation RATED.
 */
@Service
@RequiredArgsConstructor
public class RatingService {

    private final Driver neo4jDriver;

    /**
     * Crée ou met à jour la note d'un utilisateur sur un film.
     *
     * Cypher : MERGE crée la relation si elle n'existe pas, ou la met à jour si elle existe.
     * Résultat : (:User {userId})-[:RATED {score, ratingId, ...}]->(:Movie {movieId})
     */
    public RatingResponse rateMovie(String userId, RatingRequest request) {
        String ratingId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        try (Session session = neo4jDriver.session()) {
            // Vérifie que le film existe
            boolean movieExists = session.run(
                "MATCH (m:Movie {movieId: $movieId}) RETURN count(m) > 0 AS exists",
                Map.of("movieId", request.getMovieId())
            ).single().get("exists").asBoolean();

            if (!movieExists) {
                throw new IllegalArgumentException("Film introuvable : " + request.getMovieId());
            }

            // Vérifie que le user existe
            boolean userExists = session.run(
                "MATCH (u:User {userId: $userId}) RETURN count(u) > 0 AS exists",
                Map.of("userId", userId)
            ).single().get("exists").asBoolean();

            if (!userExists) {
                throw new IllegalArgumentException("Utilisateur introuvable : " + userId);
            }

            // MERGE : crée ou met à jour la relation RATED
            // Si l'utilisateur a déjà noté ce film → on met à jour le score
            Record result = session.run("""
                MATCH (u:User {userId: $userId}), (m:Movie {movieId: $movieId})
                MERGE (u)-[r:RATED]->(m)
                ON CREATE SET
                    r.ratingId   = $ratingId,
                    r.score      = $score,
                    r.comment    = $comment,
                    r.createdAt  = $now,
                    r.updatedAt  = $now
                ON MATCH SET
                    r.score      = $score,
                    r.comment    = $comment,
                    r.updatedAt  = $now
                RETURN r.ratingId  AS ratingId,
                       r.score     AS score,
                       r.comment   AS comment,
                       r.createdAt AS createdAt,
                       r.updatedAt AS updatedAt,
                       u.userId    AS userId,
                       u.username  AS username,
                       m.movieId   AS movieId,
                       m.title     AS movieTitle
                """,
                Map.of(
                    "userId",  userId,
                    "movieId", request.getMovieId(),
                    "ratingId", ratingId,
                    "score",   request.getScore(),
                    "comment", request.getComment() != null ? request.getComment() : "",
                    "now",     now.toString()
                )
            ).single();

            // Met à jour averageRating sur le nœud Movie
            updateMovieAverageRating(session, request.getMovieId());

            return mapToResponse(result);
        }
    }

    /**
     * Récupère toutes les notes d'un film.
     */
    public List<RatingResponse> getRatingsByMovie(String movieId) {
        try (Session session = neo4jDriver.session()) {
            return session.run("""
                MATCH (u:User)-[r:RATED]->(m:Movie {movieId: $movieId})
                RETURN r.ratingId  AS ratingId,
                       r.score     AS score,
                       r.comment   AS comment,
                       r.createdAt AS createdAt,
                       r.updatedAt AS updatedAt,
                       u.userId    AS userId,
                       u.username  AS username,
                       m.movieId   AS movieId,
                       m.title     AS movieTitle
                ORDER BY r.createdAt DESC
                """,
                Map.of("movieId", movieId)
            ).list(this::mapToResponse);
        }
    }

    /**
     * Récupère toutes les notes données par un utilisateur.
     */
    public List<RatingResponse> getRatingsByUser(String userId) {
        try (Session session = neo4jDriver.session()) {
            return session.run("""
                MATCH (u:User {userId: $userId})-[r:RATED]->(m:Movie)
                RETURN r.ratingId  AS ratingId,
                       r.score     AS score,
                       r.comment   AS comment,
                       r.createdAt AS createdAt,
                       r.updatedAt AS updatedAt,
                       u.userId    AS userId,
                       u.username  AS username,
                       m.movieId   AS movieId,
                       m.title     AS movieTitle
                ORDER BY r.updatedAt DESC
                """,
                Map.of("userId", userId)
            ).list(this::mapToResponse);
        }
    }

    /**
     * Supprime la note d'un utilisateur sur un film.
     */
    public void deleteRating(String userId, String ratingId) {
        try (Session session = neo4jDriver.session()) {
            // Vérifie que la note appartient bien à cet utilisateur
            boolean owned = session.run("""
                MATCH (u:User {userId: $userId})-[r:RATED {ratingId: $ratingId}]->()
                RETURN count(r) > 0 AS owned
                """,
                Map.of("userId", userId, "ratingId", ratingId)
            ).single().get("owned").asBoolean();

            if (!owned) {
                throw new IllegalArgumentException("Note introuvable ou accès refusé");
            }

            // Récupère le movieId avant suppression (pour recalculer la moyenne)
            String movieId = session.run("""
                MATCH (u:User {userId: $userId})-[r:RATED {ratingId: $ratingId}]->(m:Movie)
                RETURN m.movieId AS movieId
                """,
                Map.of("userId", userId, "ratingId", ratingId)
            ).single().get("movieId").asString();

            // Supprime la relation
            session.run("""
                MATCH ()-[r:RATED {ratingId: $ratingId}]->()
                DELETE r
                """,
                Map.of("ratingId", ratingId)
            );

            updateMovieAverageRating(session, movieId);
        }
    }

    /**
     * Recalcule et met à jour averageRating et totalRatings sur le nœud Movie.
     * Appelé après chaque création ou suppression de note.
     */
    private void updateMovieAverageRating(Session session, String movieId) {
        session.run("""
            MATCH (m:Movie {movieId: $movieId})
            OPTIONAL MATCH (u:User)-[r:RATED]->(m)
            WITH m, count(r) AS total, coalesce(avg(r.score), 0.0) AS avg
            SET m.averageRating = avg,
                m.totalRatings  = total
            """,
            Map.of("movieId", movieId)
        );
    }

    /**
     * Convertit un Record Neo4j en RatingResponse.
     */
    private RatingResponse mapToResponse(Record record) {
        String createdAtStr = record.get("createdAt").isNull() ? null : record.get("createdAt").asString();
        String updatedAtStr = record.get("updatedAt").isNull() ? null : record.get("updatedAt").asString();

        return RatingResponse.builder()
                .ratingId(record.get("ratingId").asString(null))
                .userId(record.get("userId").asString(null))
                .username(record.get("username").asString(null))
                .movieId(record.get("movieId").asString(null))
                .movieTitle(record.get("movieTitle").asString(null))
                .score(record.get("score").isNull() ? null : record.get("score").asDouble())
                .comment(record.get("comment").asString(null))
                .createdAt(createdAtStr != null ? LocalDateTime.parse(createdAtStr) : null)
                .updatedAt(updatedAtStr != null ? LocalDateTime.parse(updatedAtStr) : null)
                .build();
    }
}
