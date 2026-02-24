package com.neo4flix.movieservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {

    private Long id;
    private String movieId;
    private String title;
    private Integer releaseYear;
    private String synopsis;
    private String posterUrl;
    private String language;
    private Integer duration;
    private Double averageRating;
    private Integer totalRatings;
    private Set<String> genres;
    private Set<String> directors;
    private Set<String> actors;
}
