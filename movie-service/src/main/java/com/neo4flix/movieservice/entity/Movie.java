package com.neo4flix.movieservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.util.HashSet;
import java.util.Set;

@Node("Movie")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    @GeneratedValue
    private Long id;

    @Property("movieId")
    private String movieId;          // UUID métier

    @Property("title")
    private String title;

    @Property("releaseYear")
    private Integer releaseYear;

    @Property("synopsis")
    private String synopsis;

    @Property("posterUrl")
    private String posterUrl;

    @Property("language")
    private String language;

    @Property("duration")
    private Integer duration;        // en minutes

    @Property("averageRating")
    private Double averageRating;

    @Property("totalRatings")
    private Integer totalRatings;

    @Relationship(type = "HAS_GENRE", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    @Relationship(type = "DIRECTED_BY", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<Director> directors = new HashSet<>();

    @Relationship(type = "STARS", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<Actor> actors = new HashSet<>();
}
