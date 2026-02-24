package com.neo4flix.ratingservice.controller;

import com.neo4flix.ratingservice.dto.ApiResponse;
import com.neo4flix.ratingservice.dto.RatingRequest;
import com.neo4flix.ratingservice.dto.RatingResponse;
import com.neo4flix.ratingservice.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de notation.
 *
 * POST   /ratings                      → noter un film (JWT requis)
 * GET    /ratings/movie/{movieId}       → notes d'un film (public)
 * GET    /ratings/user/me               → mes notes (JWT requis)
 * DELETE /ratings/{ratingId}            → supprimer ma note (JWT requis)
 */
@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
@Tag(name = "Ratings", description = "Gestion des notes (:User)-[:RATED]->(:Movie)")
public class RatingController {

    private final RatingService ratingService;

    /**
     * Note un film.
     * Le userId est extrait du JWT (pas besoin de le passer dans le body).
     */
    @PostMapping
    @Operation(summary = "Noter un film", description = "Crée ou met à jour la note de l'utilisateur connecté sur un film")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<RatingResponse>> rateMovie(
            @Valid @RequestBody RatingRequest request,
            Authentication authentication) {

        String userId = extractUserId(authentication);
        RatingResponse response = ratingService.rateMovie(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Film noté avec succès", response));
    }

    /**
     * Récupère toutes les notes d'un film — public, pas de JWT requis.
     */
    @GetMapping("/movie/{movieId}")
    @Operation(summary = "Notes d'un film", description = "Liste toutes les notes pour un film donné")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getRatingsByMovie(
            @PathVariable String movieId) {

        List<RatingResponse> ratings = ratingService.getRatingsByMovie(movieId);
        return ResponseEntity.ok(ApiResponse.ok(ratings));
    }

    /**
     * Récupère toutes les notes de l'utilisateur connecté.
     */
    @GetMapping("/user/me")
    @Operation(summary = "Mes notes", description = "Liste tous les films notés par l'utilisateur connecté")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getMyRatings(
            Authentication authentication) {

        String userId = extractUserId(authentication);
        List<RatingResponse> ratings = ratingService.getRatingsByUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(ratings));
    }

    /**
     * Supprime une note — l'utilisateur ne peut supprimer que ses propres notes.
     */
    @DeleteMapping("/{ratingId}")
    @Operation(summary = "Supprimer une note", description = "Supprime la note de l'utilisateur connecté")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deleteRating(
            @PathVariable String ratingId,
            Authentication authentication) {

        String userId = extractUserId(authentication);
        ratingService.deleteRating(userId, ratingId);
        return ResponseEntity.ok(ApiResponse.ok("Note supprimée", null));
    }

    /**
     * Extrait le userId depuis le token JWT.
     * JwtAuthenticationFilter stocke les détails dans Authentication.getDetails()
     * sous forme de Map, et le principal est l'email.
     * On utilise le credentials pour passer le userId (voir JwtAuthenticationFilter).
     */
    private String extractUserId(Authentication authentication) {
        // Le userId est passé via credentials dans le filtre JWT
        Object credentials = authentication.getCredentials();
        if (credentials instanceof String) {
            return (String) credentials;
        }
        throw new IllegalStateException("userId introuvable dans le token");
    }
}
