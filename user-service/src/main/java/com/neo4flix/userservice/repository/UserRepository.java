package com.neo4flix.userservice.repository;

import com.neo4flix.userservice.entity.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Accès aux données Neo4j pour les utilisateurs.
 * Spring Data génère automatiquement les requêtes Cypher à partir des noms de méthodes.
 */
@Repository
public interface UserRepository extends Neo4jRepository<User, Long> {

    /**
     * Cherche un utilisateur par email (utilisé lors du login).
     */
    Optional<User> findByEmail(String email);

    /**
     * Cherche un utilisateur par son userId métier (UUID).
     */
    Optional<User> findByUserId(String userId);

    /**
     * Cherche un utilisateur par username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Vérifie si un email est déjà utilisé.
     */
    boolean existsByEmail(String email);

    /**
     * Vérifie si un username est déjà utilisé.
     */
    boolean existsByUsername(String username);

    /**
     * Requête Cypher personnalisée : compte le nombre d'utilisateurs par rôle.
     */
    @Query("MATCH (u:User {role: $role}) RETURN count(u)")
    long countByRole(String role);
}
