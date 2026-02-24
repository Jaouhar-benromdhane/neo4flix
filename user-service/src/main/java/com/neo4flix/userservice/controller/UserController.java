package com.neo4flix.userservice.controller;

import com.neo4flix.userservice.dto.ApiResponse;
import com.neo4flix.userservice.dto.UserResponse;
import com.neo4flix.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de gestion des profils utilisateurs.
 * 
 * GET  /users/me          → profil de l'utilisateur connecté (JWT requis)
 * GET  /users             → liste tous les users (ADMIN seulement)
 * GET  /users/{userId}    → profil d'un user (ADMIN seulement)
 * DELETE /users/{userId}  → désactive un compte (ADMIN seulement)
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gestion des profils utilisateurs")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    /**
     * Retourne le profil de l'utilisateur connecté.
     * Spring Security met l'email dans Authentication.getName() via JwtAuthenticationFilter.
     */
    @GetMapping("/me")
    @Operation(summary = "Mon profil", description = "Retourne le profil de l'utilisateur connecté")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(Authentication authentication) {
        String email = authentication.getName(); // extrait du JWT par JwtAuthenticationFilter
        UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    /**
     * Liste tous les utilisateurs — ADMIN uniquement (contrôlé par SecurityConfig).
     */
    @GetMapping
    @Operation(summary = "Tous les utilisateurs", description = "Liste tous les comptes (ADMIN)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    /**
     * Profil d'un utilisateur spécifique — ADMIN uniquement.
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Profil par ID", description = "Retourne le profil d'un utilisateur par userId")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    /**
     * Désactive un compte — ADMIN uniquement.
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Désactiver un compte", description = "Soft delete : désactive le compte sans supprimer les données")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable String userId) {
        userService.disableUser(userId);
        return ResponseEntity.ok(ApiResponse.ok("Compte désactivé", null));
    }
}
