package com.neo4flix.movieservice.repository;

import com.neo4flix.movieservice.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends Neo4jRepository<Movie, Long> {

    // Trouver par movieId (UUID métier)
    Optional<Movie> findByMovieId(String movieId);

    // Recherche par titre (insensible à la casse, contient)
    @Query("MATCH (m:Movie) WHERE toLower(m.title) CONTAINS toLower($title) " +
           "OPTIONAL MATCH (m)-[:HAS_GENRE]->(g:Genre) " +
           "OPTIONAL MATCH (m)-[:DIRECTED_BY]->(d:Director) " +
           "OPTIONAL MATCH (m)-[:STARS]->(a:Actor) " +
           "RETURN m, collect(g), collect(d), collect(a)")
    List<Movie> findByTitleContainingIgnoreCase(@Param("title") String title);

    // Recherche par genre
    @Query("MATCH (m:Movie)-[:HAS_GENRE]->(g:Genre) WHERE toLower(g.name) = toLower($genre) " +
           "OPTIONAL MATCH (m)-[:HAS_GENRE]->(allGenres:Genre) " +
           "OPTIONAL MATCH (m)-[:DIRECTED_BY]->(d:Director) " +
           "OPTIONAL MATCH (m)-[:STARS]->(a:Actor) " +
           "RETURN m, collect(allGenres), collect(d), collect(a)")
    List<Movie> findByGenre(@Param("genre") String genre);

    // Recherche par année
    @Query("MATCH (m:Movie) WHERE m.releaseYear = $year " +
           "OPTIONAL MATCH (m)-[:HAS_GENRE]->(g:Genre) " +
           "OPTIONAL MATCH (m)-[:DIRECTED_BY]->(d:Director) " +
           "OPTIONAL MATCH (m)-[:STARS]->(a:Actor) " +
           "RETURN m, collect(g), collect(d), collect(a)")
    List<Movie> findByReleaseYear(@Param("year") Integer year);

    // Recherche par réalisateur
    @Query("MATCH (m:Movie)-[:DIRECTED_BY]->(d:Director) WHERE toLower(d.name) CONTAINS toLower($director) " +
           "OPTIONAL MATCH (m)-[:HAS_GENRE]->(g:Genre) " +
           "OPTIONAL MATCH (m)-[:STARS]->(a:Actor) " +
           "RETURN m, collect(g), collect(d), collect(a)")
    List<Movie> findByDirector(@Param("director") String director);

    // Recherche par acteur
    @Query("MATCH (m:Movie)-[:STARS]->(a:Actor) WHERE toLower(a.name) CONTAINS toLower($actor) " +
           "OPTIONAL MATCH (m)-[:HAS_GENRE]->(g:Genre) " +
           "OPTIONAL MATCH (m)-[:DIRECTED_BY]->(d:Director) " +
           "RETURN m, collect(g), collect(d), collect(a)")
    List<Movie> findByActor(@Param("actor") String actor);

    // Tous les films paginés (méthode native Spring Data Neo4j)
    Page<Movie> findAll(Pageable pageable);

    // Top films par note moyenne
    @Query("MATCH (m:Movie) WHERE m.averageRating IS NOT NULL " +
           "OPTIONAL MATCH (m)-[:HAS_GENRE]->(g:Genre) " +
           "RETURN m, collect(g) " +
           "ORDER BY m.averageRating DESC LIMIT $limit")
    List<Movie> findTopRated(@Param("limit") int limit);

    // Vérifier si le movieId existe déjà
    boolean existsByMovieId(String movieId);
}
