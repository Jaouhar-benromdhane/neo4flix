package com.neo4flix.movieservice.service;

import com.neo4flix.movieservice.dto.MovieRequest;
import com.neo4flix.movieservice.dto.MovieResponse;
import com.neo4flix.movieservice.entity.Actor;
import com.neo4flix.movieservice.entity.Director;
import com.neo4flix.movieservice.entity.Genre;
import com.neo4flix.movieservice.entity.Movie;
import com.neo4flix.movieservice.exception.MovieNotFoundException;
import com.neo4flix.movieservice.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MovieService {

    private final MovieRepository movieRepository;

    // ─── Créer un film ─────────────────────────────────────────────────────────
    public MovieResponse createMovie(MovieRequest request) {
        log.info("Création du film : {}", request.getTitle());

        Movie movie = Movie.builder()
                .movieId(UUID.randomUUID().toString())
                .title(request.getTitle())
                .releaseYear(request.getReleaseYear())
                .synopsis(request.getSynopsis())
                .posterUrl(request.getPosterUrl())
                .language(request.getLanguage())
                .duration(request.getDuration())
                .averageRating(0.0)
                .totalRatings(0)
                .genres(request.getGenres().stream()
                        .map(name -> Genre.builder().name(name).build())
                        .collect(Collectors.toSet()))
                .directors(request.getDirectors() != null
                        ? request.getDirectors().stream()
                            .map(name -> Director.builder().name(name).build())
                            .collect(Collectors.toSet())
                        : java.util.Set.of())
                .actors(request.getActors() != null
                        ? request.getActors().stream()
                            .map(name -> Actor.builder().name(name).build())
                            .collect(Collectors.toSet())
                        : java.util.Set.of())
                .build();

        Movie saved = movieRepository.save(movie);
        log.info("Film créé avec l'id : {}", saved.getMovieId());
        return toResponse(saved);
    }

    // ─── Obtenir tous les films (liste simple) ──────────────────────────────────
    @Transactional(readOnly = true)
    public List<MovieResponse> getAllMoviesList(int page, int size) {
        return movieRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Obtenir un film par movieId ────────────────────────────────────────────
    @Transactional(readOnly = true)
    public MovieResponse getMovieById(String movieId) {
        Movie movie = movieRepository.findByMovieId(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Film non trouvé : " + movieId));
        return toResponse(movie);
    }

    // ─── Modifier un film ──────────────────────────────────────────────────────
    public MovieResponse updateMovie(String movieId, MovieRequest request) {
        log.info("Mise à jour du film : {}", movieId);
        Movie movie = movieRepository.findByMovieId(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Film non trouvé : " + movieId));

        movie.setTitle(request.getTitle());
        movie.setReleaseYear(request.getReleaseYear());
        movie.setSynopsis(request.getSynopsis());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setLanguage(request.getLanguage());
        movie.setDuration(request.getDuration());
        movie.setGenres(request.getGenres().stream()
                .map(name -> Genre.builder().name(name).build())
                .collect(Collectors.toSet()));
        if (request.getDirectors() != null) {
            movie.setDirectors(request.getDirectors().stream()
                    .map(name -> Director.builder().name(name).build())
                    .collect(Collectors.toSet()));
        }
        if (request.getActors() != null) {
            movie.setActors(request.getActors().stream()
                    .map(name -> Actor.builder().name(name).build())
                    .collect(Collectors.toSet()));
        }

        Movie saved = movieRepository.save(movie);
        return toResponse(saved);
    }

    // ─── Supprimer un film ─────────────────────────────────────────────────────
    public void deleteMovie(String movieId) {
        log.info("Suppression du film : {}", movieId);
        Movie movie = movieRepository.findByMovieId(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Film non trouvé : " + movieId));
        movieRepository.delete(movie);
    }

    // ─── Recherche multi-critères ──────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<MovieResponse> searchMovies(String title, String genre,
                                            Integer year, String director, String actor) {
        if (title != null && !title.isBlank()) {
            return movieRepository.findByTitleContainingIgnoreCase(title).stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
        if (genre != null && !genre.isBlank()) {
            return movieRepository.findByGenre(genre).stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
        if (year != null) {
            return movieRepository.findByReleaseYear(year).stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
        if (director != null && !director.isBlank()) {
            return movieRepository.findByDirector(director).stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
        if (actor != null && !actor.isBlank()) {
            return movieRepository.findByActor(actor).stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
        return movieRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    // ─── Top films par note ────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<MovieResponse> getTopRated(int limit) {
        return movieRepository.findTopRated(limit).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Mise à jour de la note moyenne (appelé par rating-service) ──────────
    public void updateRating(String movieId, Double newAverage, Integer totalRatings) {
        Movie movie = movieRepository.findByMovieId(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Film non trouvé : " + movieId));
        movie.setAverageRating(newAverage);
        movie.setTotalRatings(totalRatings);
        movieRepository.save(movie);
    }

    // ─── Mapper entité → DTO ──────────────────────────────────────────────────
    private MovieResponse toResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .movieId(movie.getMovieId())
                .title(movie.getTitle())
                .releaseYear(movie.getReleaseYear())
                .synopsis(movie.getSynopsis())
                .posterUrl(movie.getPosterUrl())
                .language(movie.getLanguage())
                .duration(movie.getDuration())
                .averageRating(movie.getAverageRating())
                .totalRatings(movie.getTotalRatings())
                .genres(movie.getGenres() == null ? java.util.Set.of()
                        : movie.getGenres().stream()
                            .map(Genre::getName)
                            .collect(Collectors.toSet()))
                .directors(movie.getDirectors() == null ? java.util.Set.of()
                        : movie.getDirectors().stream()
                            .map(Director::getName)
                            .collect(Collectors.toSet()))
                .actors(movie.getActors() == null ? java.util.Set.of()
                        : movie.getActors().stream()
                            .map(Actor::getName)
                            .collect(Collectors.toSet()))
                .build();
    }
}
