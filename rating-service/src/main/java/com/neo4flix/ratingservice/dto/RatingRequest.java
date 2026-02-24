package com.neo4flix.ratingservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Corps de la requête POST /ratings
 * Un utilisateur note un film avec un score entre 1.0 et 10.0.
 */
@Data
public class RatingRequest {

    @NotBlank(message = "Le movieId est obligatoire")
    private String movieId;

    @NotNull(message = "Le score est obligatoire")
    @DecimalMin(value = "1.0", message = "Score minimum : 1.0")
    @DecimalMax(value = "10.0", message = "Score maximum : 10.0")
    private Double score;

    private String comment; // optionnel
}
