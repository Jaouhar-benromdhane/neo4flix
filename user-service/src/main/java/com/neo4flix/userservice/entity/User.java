package com.neo4flix.userservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Nœud Neo4j représentant un utilisateur.
 * 
 * Schéma dans le graphe :
 *   (:User {userId, email, username, passwordHash, role, ...})
 *   (:User)-[:RATED {score, timestamp}]->(:Movie)   (géré par rating-service)
 *   (:User)-[:WATCHLISTED]->(:Movie)                (géré par user-service)
 */
@Node("User")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * Identifiant métier unique (UUID), utilisé dans les endpoints REST.
     * On n'expose JAMAIS l'id Neo4j interne (Long) dans les API.
     */
    @Property("userId")
    private String userId;

    @Property("email")
    private String email;

    @Property("username")
    private String username;

    /**
     * Mot de passe haché avec BCrypt.
     * JAMAIS retourné dans les réponses API.
     */
    @Property("passwordHash")
    private String passwordHash;

    @Property("role")
    private String role; // "USER" ou "ADMIN"

    @Property("enabled")
    private boolean enabled;

    /** Double authentification (2FA) — désactivée par défaut */
    @Property("twoFactorEnabled")
    private boolean twoFactorEnabled;

    /** Secret TOTP (base32) — jamais exposé dans les réponses API */
    @Property("twoFactorSecret")
    private String twoFactorSecret;

    @Property("createdAt")
    private LocalDateTime createdAt;

    @Property("updatedAt")
    private LocalDateTime updatedAt;

    /**
     * Appelé avant la création pour initialiser userId et createdAt.
     */
    public void prePersist() {
        if (this.userId == null) {
            this.userId = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        if (this.role == null) {
            this.role = Role.USER.name();
        }
        this.enabled = true;
    }
}
