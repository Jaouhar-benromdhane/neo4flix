package com.neo4flix.recommendationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Un film recommandé avec son score de pertinence.
 * Le score explique POURQUOI ce film est recommandé.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedMovie {

    private String movieId;
    private String title;
    private Integer releaseYear;
    private Double averageRating;
    private Integer totalRatings;
    private List<String> genres;

    /**
     * Score de pertinence calculé par l'algorithme.
     * Plus le score est élevé, plus le film est recommandé.
     *
     * Pour le filtrage collaboratif : nombre d'utilisateurs similaires qui l'ont aimé.
     * Pour le content-based : nombre de genres/réalisateurs en commun.
     */
    private Double relevanceScore;

    /**
     * Raison de la recommandation — pour afficher à l'utilisateur.
     * Exemples:
     *   "3 utilisateurs qui ont aimé The Matrix ont aussi aimé ce film"
     *   "Même genre que The Matrix : Action, Sci-Fi"
     *   "Populaire cette semaine : noté 9.2/10 par 42 utilisateurs"
     */
    private String reason;
}
