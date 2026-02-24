package com.neo4flix.recommendationservice.service;

import com.neo4flix.recommendationservice.dto.RecommendedMovie;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║           RECOMMENDATION SERVICE — CŒUR DU PROJET           ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * Ce service exploite la PUISSANCE des graphes Neo4j pour faire
 * des recommandations de films.
 *
 * POURQUOI Neo4j EST PARFAIT POUR LES RECOMMANDATIONS ?
 * ──────────────────────────────────────────────────────
 * Dans une base relationnelle (MySQL), pour trouver des films similaires,
 * il faudrait des dizaines de JOINs et des sous-requêtes complexes.
 *
 * Dans Neo4j, c'est naturel : le graphe EST la structure de recommandation.
 * Les relations (:User)-[:RATED]->(:Movie) créent automatiquement un réseau
 * de connexions qu'on peut traverser en Cypher.
 *
 * 3 ALGORITHMES IMPLÉMENTÉS :
 * ───────────────────────────
 * 1. Filtrage collaboratif  → "Des gens comme toi ont aimé..."
 * 2. Content-based          → "Ce film ressemble à celui-ci..."
 * 3. Populaire              → "Les mieux notés en ce moment..."
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final Driver neo4jDriver;

    // ══════════════════════════════════════════════════════════════
    // ALGORITHME 1 : FILTRAGE COLLABORATIF
    // ══════════════════════════════════════════════════════════════

    /**
     * Recommande des films basé sur les goûts d'utilisateurs similaires.
     *
     * LOGIQUE :
     *   1. Trouve les films qu'Alice a bien notés (score >= 7)
     *   2. Trouve d'autres utilisateurs qui ont aussi bien noté ces films
     *      → Ce sont les "utilisateurs similaires"
     *   3. Trouve les films que ces utilisateurs ont aimés, mais qu'Alice n'a pas encore vus
     *   4. Trie par nombre d'utilisateurs similaires qui recommandent ce film
     *
     * CYPHER COMMENTÉ :
     *
     *   MATCH (me:User {userId: $userId})-[myRating:RATED]->(commonMovie:Movie)
     *   ↑ Trouve les films qu'Alice a notés
     *
     *   WHERE myRating.score >= 7.0
     *   ↑ Seulement les films qu'elle a AIMÉS (score >= 7)
     *
     *   MATCH (commonMovie)<-[theirRating:RATED]-(similar:User)
     *   ↑ Trouve d'autres users qui ont aussi noté ces mêmes films
     *
     *   WHERE similar.userId <> $userId AND theirRating.score >= 7.0
     *   ↑ Exclut Alice elle-même, seulement ceux qui ont aussi AIMÉ
     *
     *   MATCH (similar)-[r:RATED]->(reco:Movie)
     *   ↑ Films notés par ces utilisateurs similaires
     *
     *   WHERE NOT (me)-[:RATED]->(reco)
     *   ↑ Exclut les films qu'Alice a déjà notés
     *
     *   RETURN reco, count(similar) AS relevanceScore
     *   ↑ Plus un film est recommandé par beaucoup d'utilisateurs similaires,
     *     plus son relevanceScore est élevé
     */
    public List<RecommendedMovie> getCollaborativeRecommendations(String userId, int limit) {
        try (Session session = neo4jDriver.session()) {
            return session.run("""
                MATCH (me:User {userId: $userId})-[myRating:RATED]->(commonMovie:Movie)
                WHERE myRating.score >= 7.0
                MATCH (commonMovie)<-[theirRating:RATED]-(similar:User)
                WHERE similar.userId <> $userId AND theirRating.score >= 7.0
                MATCH (similar)-[r:RATED]->(reco:Movie)
                WHERE NOT (me)-[:RATED]->(reco) AND r.score >= 7.0
                WITH reco, count(DISTINCT similar) AS nbSimilarUsers, avg(r.score) AS avgScore
                OPTIONAL MATCH (reco)-[:HAS_GENRE]->(g:Genre)
                RETURN reco.movieId      AS movieId,
                       reco.title        AS title,
                       reco.releaseYear  AS releaseYear,
                       reco.averageRating AS averageRating,
                       reco.totalRatings  AS totalRatings,
                       collect(g.name)   AS genres,
                       toFloat(nbSimilarUsers) AS relevanceScore,
                       nbSimilarUsers    AS nbUsers
                ORDER BY relevanceScore DESC, avgScore DESC
                LIMIT $limit
                """,
                Map.of("userId", userId, "limit", limit)
            ).list(record -> mapToRecommendedMovie(record, buildCollabReason(record)));
        }
    }

    // ══════════════════════════════════════════════════════════════
    // ALGORITHME 2 : CONTENT-BASED FILTERING
    // ══════════════════════════════════════════════════════════════

    /**
     * Trouve des films similaires à un film donné.
     *
     * LOGIQUE :
     *   1. Récupère les genres et réalisateurs du film cible
     *   2. Cherche d'autres films qui partagent ces genres/réalisateurs
     *   3. Plus un film partage de genres/réalisateurs, plus il est similaire
     *
     * CYPHER COMMENTÉ :
     *
     *   MATCH (target:Movie {movieId: $movieId})
     *   MATCH (target)-[:HAS_GENRE]->(g:Genre)<-[:HAS_GENRE]-(similar:Movie)
     *   ↑ Films qui partagent au moins un genre avec le film cible
     *
     *   WHERE similar.movieId <> $movieId
     *   ↑ Exclut le film lui-même
     *
     *   WITH similar, count(g) AS sharedGenres
     *   ↑ Compte combien de genres sont partagés (plus = plus similaire)
     *
     *   OPTIONAL MATCH (target)-[:DIRECTED_BY]->(d:Director)<-[:DIRECTED_BY]-(similar)
     *   WITH similar, sharedGenres, count(d) AS sharedDirectors
     *   ↑ Bonus si même réalisateur
     *
     *   relevanceScore = sharedGenres * 2 + sharedDirectors * 3
     *   ↑ Les réalisateurs comptent plus que les genres (facteur 3 vs 2)
     */
    public List<RecommendedMovie> getSimilarMovies(String movieId, int limit) {
        try (Session session = neo4jDriver.session()) {

            // Vérifie que le film existe
            boolean exists = session.run(
                "MATCH (m:Movie {movieId: $movieId}) RETURN count(m) > 0 AS exists",
                Map.of("movieId", movieId)
            ).single().get("exists").asBoolean();

            if (!exists) {
                throw new IllegalArgumentException("Film introuvable : " + movieId);
            }

            return session.run("""
                MATCH (target:Movie {movieId: $movieId})
                MATCH (target)-[:HAS_GENRE]->(g:Genre)<-[:HAS_GENRE]-(similar:Movie)
                WHERE similar.movieId <> $movieId
                WITH target, similar, count(DISTINCT g) AS sharedGenres
                OPTIONAL MATCH (target)-[:DIRECTED_BY]->(d:Director)<-[:DIRECTED_BY]-(similar)
                WITH similar, sharedGenres, count(DISTINCT d) AS sharedDirectors
                OPTIONAL MATCH (similar)-[:HAS_GENRE]->(sg:Genre)
                WITH similar, sharedGenres, sharedDirectors, collect(sg.name) AS genres,
                     (sharedGenres * 2 + sharedDirectors * 3) AS relevanceScore
                RETURN similar.movieId       AS movieId,
                       similar.title         AS title,
                       similar.releaseYear   AS releaseYear,
                       similar.averageRating AS averageRating,
                       similar.totalRatings  AS totalRatings,
                       genres,
                       toFloat(relevanceScore) AS relevanceScore,
                       sharedGenres,
                       sharedDirectors
                ORDER BY relevanceScore DESC, similar.averageRating DESC
                LIMIT $limit
                """,
                Map.of("movieId", movieId, "limit", limit)
            ).list(record -> mapToRecommendedMovie(record, buildContentReason(record)));
        }
    }

    // ══════════════════════════════════════════════════════════════
    // ALGORITHME 3 : POPULARITÉ GLOBALE
    // ══════════════════════════════════════════════════════════════

    /**
     * Retourne les films les plus populaires.
     *
     * LOGIQUE :
     *   Score de popularité = moyenne des notes × log(nombre de notes + 1)
     *
     *   POURQUOI ce calcul ?
     *   → Un film avec 1 note de 10/10 n'est PAS plus populaire
     *     qu'un film avec 1000 notes de 9/10.
     *   → On pondère par le nombre de notes (logarithme pour éviter
     *     qu'un film avec 10000 notes écrase tout).
     *
     *   Exemple :
     *   Film A : 10/10 × log(1+1) = 10 × 0.3 = 3.0
     *   Film B : 9/10  × log(100+1) = 9 × 2.0 = 18.0  ← clairement plus populaire
     */
    public List<RecommendedMovie> getPopularMovies(int limit) {
        try (Session session = neo4jDriver.session()) {
            return session.run("""
                MATCH (m:Movie)
                WHERE m.totalRatings > 0
                OPTIONAL MATCH (m)-[:HAS_GENRE]->(g:Genre)
                WITH m, collect(g.name) AS genres,
                     m.averageRating * log(toFloat(m.totalRatings) + 1.0) AS popularityScore
                RETURN m.movieId       AS movieId,
                       m.title         AS title,
                       m.releaseYear   AS releaseYear,
                       m.averageRating AS averageRating,
                       m.totalRatings  AS totalRatings,
                       genres,
                       popularityScore AS relevanceScore
                ORDER BY popularityScore DESC
                LIMIT $limit
                """,
                Map.of("limit", limit)
            ).list(record -> mapToRecommendedMovie(record, buildPopularReason(record)));
        }
    }

    // ══════════════════════════════════════════════════════════════
    // AGRÉGATION : Combine les 3 algorithmes
    // ══════════════════════════════════════════════════════════════

    /**
     * Recommandations personnalisées pour un utilisateur.
     *
     * Stratégie :
     *   - Si l'utilisateur a noté des films → Filtrage collaboratif (principal)
     *   - Si peu de données → Complète avec les films populaires (cold start)
     *
     * "Cold start" = problème classique des systèmes de recommandation :
     *   que faire pour un nouvel utilisateur qui n'a encore rien noté ?
     *   → On lui montre les films populaires.
     */
    public List<RecommendedMovie> getPersonalizedRecommendations(String userId) {
        List<RecommendedMovie> collab = getCollaborativeRecommendations(userId, 10);

        if (collab.size() >= 5) {
            // Assez de données collaboratives → retourne directement
            return collab;
        }

        // Cold start : complète avec les films populaires
        List<RecommendedMovie> popular = getPopularMovies(10 - collab.size());

        // Fusionne les deux listes (collaboratif en premier)
        java.util.List<RecommendedMovie> combined = new java.util.ArrayList<>(collab);
        popular.stream()
            .filter(p -> collab.stream().noneMatch(c -> c.getMovieId().equals(p.getMovieId())))
            .forEach(combined::add);

        return combined;
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS — Conversion et messages explicatifs
    // ══════════════════════════════════════════════════════════════

    private RecommendedMovie mapToRecommendedMovie(Record record, String reason) {
        List<String> genres = record.get("genres").asList(v -> v.asString());

        return RecommendedMovie.builder()
                .movieId(record.get("movieId").asString(null))
                .title(record.get("title").asString(null))
                .releaseYear(record.get("releaseYear").isNull() ? null : record.get("releaseYear").asInt())
                .averageRating(record.get("averageRating").isNull() ? 0.0 : record.get("averageRating").asDouble())
                .totalRatings(record.get("totalRatings").isNull() ? 0 : record.get("totalRatings").asInt())
                .genres(genres)
                .relevanceScore(record.get("relevanceScore").isNull() ? 0.0 : record.get("relevanceScore").asDouble())
                .reason(reason)
                .build();
    }

    private String buildCollabReason(Record record) {
        int nb = record.get("nbUsers").isNull() ? 0 : record.get("nbUsers").asInt();
        return nb + " utilisateur(s) avec des goûts similaires ont aussi aimé ce film";
    }

    private String buildContentReason(Record record) {
        int genres = record.get("sharedGenres").isNull() ? 0 : record.get("sharedGenres").asInt();
        int directors = record.get("sharedDirectors").isNull() ? 0 : record.get("sharedDirectors").asInt();
        StringBuilder sb = new StringBuilder();
        if (genres > 0) sb.append(genres).append(" genre(s) en commun");
        if (directors > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("même réalisateur");
        }
        return sb.toString();
    }

    private String buildPopularReason(Record record) {
        double avg = record.get("averageRating").isNull() ? 0.0 : record.get("averageRating").asDouble();
        int total = record.get("totalRatings").isNull() ? 0 : record.get("totalRatings").asInt();
        return String.format("Populaire : %.1f/10 basé sur %d note(s)", avg, total);
    }
}
