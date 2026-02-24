package com.neo4flix.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Réponse renvoyée après un login ou register réussi.
 * Contient le token JWT et les infos de base de l'utilisateur.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;          // JWT token à utiliser dans les requêtes suivantes
    private String tokenType;      // Toujours "Bearer"
    private String userId;
    private String username;
    private String email;
    private String role;
}
