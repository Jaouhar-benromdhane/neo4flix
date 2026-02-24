package com.neo4flix.ratingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Réponse renvoyée après création ou récupération d'une note.
 * Représente la relation (:User)-[:RATED {score}]->(:Movie) dans Neo4j.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {

    private String ratingId;       // UUID unique de cette note
    private String userId;         // Qui a noté
    private String username;       // Nom d'affichage
    private String movieId;        // Quel film
    private String movieTitle;     // Titre du film
    private Double score;          // Note entre 1.0 et 10.0
    private String comment;        // Commentaire optionnel
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
