package com.neo4flix.movieservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 1, max = 255)
    private String title;

    @NotNull(message = "L'année de sortie est obligatoire")
    @Min(value = 1888, message = "L'année doit être >= 1888")
    @Max(value = 2100)
    private Integer releaseYear;

    @Size(max = 2000)
    private String synopsis;

    private String posterUrl;

    @Size(max = 10)
    private String language;

    @Min(value = 1)
    @Max(value = 600)
    private Integer duration;

    @NotEmpty(message = "Au moins un genre est requis")
    private Set<String> genres;

    private Set<String> directors;

    private Set<String> actors;
}
