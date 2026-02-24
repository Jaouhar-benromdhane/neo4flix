package com.neo4flix.userservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Utilitaire JWT : génère et valide les tokens.
 * 
 * Structure d'un token Neo4flix :
 *   Header : { "alg": "HS256" }
 *   Payload : { "sub": "email", "roles": ["USER"], "userId": "uuid", "iat": ..., "exp": ... }
 *   Signature : HMAC-SHA256 avec le secret
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs; // en millisecondes

    /**
     * Retourne la clé secrète décodée en bytes.
     * La clé doit faire au moins 32 caractères (256 bits pour HS256).
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(secret.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Génère un JWT pour un utilisateur.
     * @param email  identifiant principal (subject)
     * @param roles  liste des rôles ["USER"] ou ["ADMIN"]
     * @param userId UUID métier de l'utilisateur
     */
    public String generateToken(String email, List<String> roles, String userId) {
        return Jwts.builder()
                .subject(email)
                .claims(Map.of(
                        "roles", roles,
                        "userId", userId
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrait l'email (subject) du token.
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extrait la liste des rôles du token.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object roles = parseClaims(token).get("roles");
        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }
        return List.of();
    }

    /**
     * Extrait le userId du token.
     */
    public String extractUserId(String token) {
        return (String) parseClaims(token).get("userId");
    }

    /**
     * Vérifie si le token est valide (signature + non expiré).
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Vérifie si le token est expiré.
     */
    public boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
