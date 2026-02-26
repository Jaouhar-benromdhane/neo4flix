package com.neo4flix.userservice.service;

import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Gestion des relations graphe :
 *   (:User)-[:WATCHED  {watchedAt}]->(:Movie)   → film vu
 *   (:User)-[:SAVED    {savedAt}]->(:Movie)      → film enregistré (watchlist)
 *
 * On utilise le Driver Neo4j directement (pas Spring Data Neo4j)
 * car les relations cross-service ne sont pas mappées en entités Java.
 */
@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final Driver neo4jDriver;

    // ─────────────────────────────── WATCHED ───────────────────────────────

    /**
     * Crée la relation (User)-[:WATCHED]->(Movie).
     * Si la relation existe déjà, elle est mise à jour (MERGE).
     */
    public void markAsWatched(String userId, String movieId) {
        checkMovieExists(movieId);
        try (Session session = neo4jDriver.session()) {
            session.run("""
                MATCH (u:User {userId: $userId})
                MATCH (m:Movie {movieId: $movieId})
                MERGE (u)-[r:WATCHED]->(m)
                SET r.watchedAt = $watchedAt
                """,
                Map.of("userId", userId, "movieId", movieId,
                       "watchedAt", LocalDateTime.now().toString())
            );
        }
    }

    /**
     * Supprime la relation (User)-[:WATCHED]->(Movie).
     */
    public void removeWatched(String userId, String movieId) {
        try (Session session = neo4jDriver.session()) {
            session.run("""
                MATCH (u:User {userId: $userId})-[r:WATCHED]->(m:Movie {movieId: $movieId})
                DELETE r
                """,
                Map.of("userId", userId, "movieId", movieId)
            );
        }
    }

    /**
     * Retourne la liste des films vus par l'utilisateur.
     */
    public List<Map<String, Object>> getWatchedMovies(String userId) {
        try (Session session = neo4jDriver.session()) {
            return session.run("""
                MATCH (u:User {userId: $userId})-[r:WATCHED]->(m:Movie)
                RETURN m.movieId AS movieId, m.title AS title,
                       m.releaseYear AS releaseYear, m.genres AS genres,
                       m.posterUrl AS posterUrl, r.watchedAt AS watchedAt
                ORDER BY r.watchedAt DESC
                """,
                Map.of("userId", userId)
            ).list(record -> Map.of(
                "movieId",     safeGet(record, "movieId"),
                "title",       safeGet(record, "title"),
                "releaseYear", safeGet(record, "releaseYear"),
                "genres",      safeGet(record, "genres"),
                "posterUrl",   safeGet(record, "posterUrl"),
                "watchedAt",   safeGet(record, "watchedAt")
            ));
        }
    }

    // ─────────────────────────────── SAVED ────────────────────────────────

    /**
     * Ajoute le film à la watchlist (User)-[:SAVED]->(Movie).
     */
    public void saveMovie(String userId, String movieId) {
        checkMovieExists(movieId);
        try (Session session = neo4jDriver.session()) {
            session.run("""
                MATCH (u:User {userId: $userId})
                MATCH (m:Movie {movieId: $movieId})
                MERGE (u)-[r:SAVED]->(m)
                SET r.savedAt = $savedAt
                """,
                Map.of("userId", userId, "movieId", movieId,
                       "savedAt", LocalDateTime.now().toString())
            );
        }
    }

    /**
     * Retire le film de la watchlist.
     */
    public void unsaveMovie(String userId, String movieId) {
        try (Session session = neo4jDriver.session()) {
            session.run("""
                MATCH (u:User {userId: $userId})-[r:SAVED]->(m:Movie {movieId: $movieId})
                DELETE r
                """,
                Map.of("userId", userId, "movieId", movieId)
            );
        }
    }

    /**
     * Retourne la watchlist de l'utilisateur.
     */
    public List<Map<String, Object>> getSavedMovies(String userId) {
        try (Session session = neo4jDriver.session()) {
            return session.run("""
                MATCH (u:User {userId: $userId})-[r:SAVED]->(m:Movie)
                RETURN m.movieId AS movieId, m.title AS title,
                       m.releaseYear AS releaseYear, m.genres AS genres,
                       m.posterUrl AS posterUrl, r.savedAt AS savedAt
                ORDER BY r.savedAt DESC
                """,
                Map.of("userId", userId)
            ).list(record -> Map.of(
                "movieId",     safeGet(record, "movieId"),
                "title",       safeGet(record, "title"),
                "releaseYear", safeGet(record, "releaseYear"),
                "genres",      safeGet(record, "genres"),
                "posterUrl",   safeGet(record, "posterUrl"),
                "savedAt",     safeGet(record, "savedAt")
            ));
        }
    }

    // ─────────────────────────────── HELPERS ──────────────────────────────

    private void checkMovieExists(String movieId) {
        try (Session session = neo4jDriver.session()) {
            boolean exists = session.run(
                "MATCH (m:Movie {movieId: $movieId}) RETURN count(m) > 0 AS exists",
                Map.of("movieId", movieId)
            ).single().get("exists").asBoolean();
            if (!exists) {
                throw new IllegalArgumentException("Film introuvable : " + movieId);
            }
        }
    }

    private Object safeGet(org.neo4j.driver.Record record, String key) {
        try {
            var val = record.get(key);
            return val.isNull() ? null : val.asObject();
        } catch (Exception e) {
            return null;
        }
    }
}
