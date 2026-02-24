package com.neo4flix.userservice.entity;

/**
 * Rôles possibles d'un utilisateur dans Neo4flix.
 * USER = utilisateur standard
 * ADMIN = administrateur (peut créer/modifier/supprimer des films)
 */
public enum Role {
    USER,
    ADMIN
}
