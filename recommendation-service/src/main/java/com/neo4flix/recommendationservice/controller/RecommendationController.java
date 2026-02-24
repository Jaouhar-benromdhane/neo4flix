package com.neo4flix.recommendationservice.controller;

import com.neo4flix.recommendationservice.dto.ApiResponse;
import com.neo4flix.recommendationservice.dto.RecommendedMovie;
import com.neo4flix.recommendationservice.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de recommandation.
 *
 * GET /recommendations/me                     → Recommandations personnalisées (JWT requis)
 * GET /recommendations/movie/{movieId}/similar → Films similaires (public)
 * GET /recommendations/popular                → Films populaires (public)
 */
@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "Moteur de recommandation basé sur le graphe Neo4j")
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * Recommandations personnalisées pour l'utilisateur connecté.
     * Combine filtrage collaboratif + films populaires (cold start).
     */
    @GetMapping("/me")
    @Operation(
        summary = "Mes recommandations",
        description = "Films recommandés basés sur tes notes et celles d'utilisateurs similaires"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<RecommendedMovie>>> getMyRecommendations(
            Authentication authentication) {

        String userId = (String) authentication.getCredentials();
        List<RecommendedMovie> recommendations =
                recommendationService.getPersonalizedRecommendations(userId);

        return ResponseEntity.ok(ApiResponse.ok(
                recommendations.isEmpty()
                    ? "Note des films pour obtenir des recommandations personnalisées !"
                    : recommendations.size() + " film(s) recommandé(s) pour toi",
                recommendations
        ));
    }

    /**
     * Films similaires à un film donné — public, pas de JWT requis.
     */
    @GetMapping("/movie/{movieId}/similar")
    @Operation(
        summary = "Films similaires",
        description = "Films partageant les mêmes genres ou réalisateurs"
    )
    public ResponseEntity<ApiResponse<List<RecommendedMovie>>> getSimilarMovies(
            @PathVariable String movieId,
            @Parameter(description = "Nombre de résultats (défaut: 10)")
            @RequestParam(defaultValue = "10") int limit) {

        List<RecommendedMovie> similar = recommendationService.getSimilarMovies(movieId, limit);
        return ResponseEntity.ok(ApiResponse.ok(similar));
    }

    /**
     * Films les plus populaires — public.
     */
    @GetMapping("/popular")
    @Operation(
        summary = "Films populaires",
        description = "Films les mieux notés pondérés par le nombre de votes"
    )
    public ResponseEntity<ApiResponse<List<RecommendedMovie>>> getPopularMovies(
            @Parameter(description = "Nombre de résultats (défaut: 10)")
            @RequestParam(defaultValue = "10") int limit) {

        List<RecommendedMovie> popular = recommendationService.getPopularMovies(limit);
        return ResponseEntity.ok(ApiResponse.ok(popular));
    }
}
