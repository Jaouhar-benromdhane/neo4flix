package com.neo4flix.movieservice.controller;

import com.neo4flix.movieservice.dto.ApiResponse;
import com.neo4flix.movieservice.dto.MovieRequest;
import com.neo4flix.movieservice.dto.MovieResponse;
import com.neo4flix.movieservice.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@Tag(name = "Movies", description = "API de gestion des films")
@CrossOrigin(origins = "*")
public class MovieController {

    private final MovieService movieService;

    // ─── GET /movies ──────────────────────────────────────────────────────────
    @GetMapping
    @Operation(summary = "Lister tous les films")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getAllMoviesList(page, size)));
    }

    // ─── GET /movies/search ───────────────────────────────────────────────────
    @GetMapping("/search")
    @Operation(summary = "Rechercher des films par titre, genre, année, réalisateur ou acteur")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> searchMovies(
            @RequestParam(required = false) @Parameter(description = "Titre du film") String title,
            @RequestParam(required = false) @Parameter(description = "Genre (ex: Action)") String genre,
            @RequestParam(required = false) @Parameter(description = "Année de sortie") Integer year,
            @RequestParam(required = false) @Parameter(description = "Nom du réalisateur") String director,
            @RequestParam(required = false) @Parameter(description = "Nom d'un acteur") String actor) {

        List<MovieResponse> results = movieService.searchMovies(title, genre, year, director, actor);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    // ─── GET /movies/top-rated ────────────────────────────────────────────────
    @GetMapping("/top-rated")
    @Operation(summary = "Obtenir les films les mieux notés")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getTopRated(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getTopRated(limit)));
    }

    // ─── GET /movies/{movieId} ────────────────────────────────────────────────
    @GetMapping("/{movieId}")
    @Operation(summary = "Obtenir les détails d'un film par son ID")
    public ResponseEntity<ApiResponse<MovieResponse>> getMovieById(
            @PathVariable String movieId) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getMovieById(movieId)));
    }

    // ─── POST /movies ─────────────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Créer un nouveau film (Admin uniquement)")
    public ResponseEntity<ApiResponse<MovieResponse>> createMovie(
            @Valid @RequestBody MovieRequest request) {
        MovieResponse response = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Film créé avec succès", response));
    }

    // ─── PUT /movies/{movieId} ────────────────────────────────────────────────
    @PutMapping("/{movieId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Modifier un film (Admin uniquement)")
    public ResponseEntity<ApiResponse<MovieResponse>> updateMovie(
            @PathVariable String movieId,
            @Valid @RequestBody MovieRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Film mis à jour", movieService.updateMovie(movieId, request)));
    }

    // ─── DELETE /movies/{movieId} ─────────────────────────────────────────────
    @DeleteMapping("/{movieId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Supprimer un film (Admin uniquement)")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable String movieId) {
        movieService.deleteMovie(movieId);
        return ResponseEntity.ok(ApiResponse.ok("Film supprimé avec succès", null));
    }

    // ─── PUT /movies/{movieId}/rating ─────────────────────────────────────────
    // Endpoint interne — appelé par le rating-service
    @PutMapping("/{movieId}/rating")
    @Operation(summary = "Mettre à jour la note moyenne (usage interne microservices)")
    public ResponseEntity<Void> updateRating(
            @PathVariable String movieId,
            @RequestParam Double average,
            @RequestParam Integer total) {
        movieService.updateRating(movieId, average, total);
        return ResponseEntity.ok().build();
    }
}
