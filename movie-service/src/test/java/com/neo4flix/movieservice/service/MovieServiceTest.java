package com.neo4flix.movieservice.service;

import com.neo4flix.movieservice.dto.MovieRequest;
import com.neo4flix.movieservice.dto.MovieResponse;
import com.neo4flix.movieservice.entity.Genre;
import com.neo4flix.movieservice.entity.Movie;
import com.neo4flix.movieservice.exception.MovieNotFoundException;
import com.neo4flix.movieservice.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    private Movie sampleMovie;
    private MovieRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleMovie = Movie.builder()
                .id(1L)
                .movieId("test-uuid-123")
                .title("The Matrix")
                .releaseYear(1999)
                .synopsis("A hacker learns the truth about reality.")
                .language("EN")
                .duration(136)
                .averageRating(4.5)
                .totalRatings(100)
                .genres(Set.of(Genre.builder().name("Sci-Fi").build()))
                .build();

        sampleRequest = MovieRequest.builder()
                .title("The Matrix")
                .releaseYear(1999)
                .synopsis("A hacker learns the truth about reality.")
                .language("EN")
                .duration(136)
                .genres(Set.of("Sci-Fi"))
                .build();
    }

    @Test
    void shouldCreateMovieSuccessfully() {
        when(movieRepository.save(any(Movie.class))).thenReturn(sampleMovie);

        MovieResponse response = movieService.createMovie(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("The Matrix");
        assertThat(response.getReleaseYear()).isEqualTo(1999);
        verify(movieRepository, times(1)).save(any(Movie.class));
    }

    @Test
    void shouldGetMovieByIdSuccessfully() {
        when(movieRepository.findByMovieId("test-uuid-123"))
                .thenReturn(Optional.of(sampleMovie));

        MovieResponse response = movieService.getMovieById("test-uuid-123");

        assertThat(response).isNotNull();
        assertThat(response.getMovieId()).isEqualTo("test-uuid-123");
        assertThat(response.getTitle()).isEqualTo("The Matrix");
    }

    @Test
    void shouldThrowMovieNotFoundException_whenMovieNotFound() {
        when(movieRepository.findByMovieId("unknown-id"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.getMovieById("unknown-id"))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("unknown-id");
    }

    @Test
    void shouldDeleteMovieSuccessfully() {
        when(movieRepository.findByMovieId("test-uuid-123"))
                .thenReturn(Optional.of(sampleMovie));
        doNothing().when(movieRepository).delete(sampleMovie);

        assertThatCode(() -> movieService.deleteMovie("test-uuid-123"))
                .doesNotThrowAnyException();

        verify(movieRepository, times(1)).delete(sampleMovie);
    }

    @Test
    void shouldUpdateMovieSuccessfully() {
        when(movieRepository.findByMovieId("test-uuid-123"))
                .thenReturn(Optional.of(sampleMovie));
        when(movieRepository.save(any(Movie.class))).thenReturn(sampleMovie);

        sampleRequest.setTitle("The Matrix Reloaded");
        MovieResponse response = movieService.updateMovie("test-uuid-123", sampleRequest);

        assertThat(response).isNotNull();
        verify(movieRepository, times(1)).save(any(Movie.class));
    }
}
