package com.neo4flix.userservice.controller;

import com.neo4flix.userservice.dto.ApiResponse;
import com.neo4flix.userservice.repository.UserRepository;
import com.neo4flix.userservice.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoints de gestion de la watchlist et des films vus.
 *
 * WATCHED  (films vus) :
 *   POST   /users/me/watched/{movieId}   → marquer comme vu
 *   DELETE /users/me/watched/{movieId}   → retirer des vus
 *   GET    /users/me/watched             → liste des films vus
 *
 * SAVED (watchlist) :
 *   POST   /users/me/saved/{movieId}     → ajouter à la watchlist
 *   DELETE /users/me/saved/{movieId}     → retirer de la watchlist
 *   GET    /users/me/saved               → ma watchlist
 */
@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
@Tag(name = "Watchlist", description = "Films vus et watchlist")
@SecurityRequirement(name = "Bearer Authentication")
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final UserRepository userRepository;

    // ─────────── WATCHED ───────────

    @PostMapping("/watched/{movieId}")
    @Operation(summary = "Marquer un film comme vu")
    public ResponseEntity<ApiResponse<Void>> markWatched(
            @PathVariable String movieId,
            Authentication auth) {
        String userId = getUserId(auth);
        watchlistService.markAsWatched(userId, movieId);
        return ResponseEntity.ok(ApiResponse.ok("Film marqué comme vu", null));
    }

    @DeleteMapping("/watched/{movieId}")
    @Operation(summary = "Retirer un film des films vus")
    public ResponseEntity<ApiResponse<Void>> removeWatched(
            @PathVariable String movieId,
            Authentication auth) {
        String userId = getUserId(auth);
        watchlistService.removeWatched(userId, movieId);
        return ResponseEntity.ok(ApiResponse.ok("Film retiré des vus", null));
    }

    @GetMapping("/watched")
    @Operation(summary = "Liste des films vus")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getWatched(Authentication auth) {
        String userId = getUserId(auth);
        List<Map<String, Object>> movies = watchlistService.getWatchedMovies(userId);
        return ResponseEntity.ok(ApiResponse.ok(movies));
    }

    // ─────────── SAVED ───────────

    @PostMapping("/saved/{movieId}")
    @Operation(summary = "Ajouter un film à la watchlist")
    public ResponseEntity<ApiResponse<Void>> saveMovie(
            @PathVariable String movieId,
            Authentication auth) {
        String userId = getUserId(auth);
        watchlistService.saveMovie(userId, movieId);
        return ResponseEntity.ok(ApiResponse.ok("Film ajouté à la watchlist", null));
    }

    @DeleteMapping("/saved/{movieId}")
    @Operation(summary = "Retirer un film de la watchlist")
    public ResponseEntity<ApiResponse<Void>> unsaveMovie(
            @PathVariable String movieId,
            Authentication auth) {
        String userId = getUserId(auth);
        watchlistService.unsaveMovie(userId, movieId);
        return ResponseEntity.ok(ApiResponse.ok("Film retiré de la watchlist", null));
    }

    @GetMapping("/saved")
    @Operation(summary = "Ma watchlist")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSaved(Authentication auth) {
        String userId = getUserId(auth);
        List<Map<String, Object>> movies = watchlistService.getSavedMovies(userId);
        return ResponseEntity.ok(ApiResponse.ok(movies));
    }

    // ─────────── HELPER ───────────

    /**
     * Récupère le userId métier de l'utilisateur connecté à partir de son email (dans le JWT).
     */
    private String getUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"))
                .getUserId();
    }
}
