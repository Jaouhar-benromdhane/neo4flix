package com.neo4flix.ratingservice;

import com.neo4flix.ratingservice.dto.RatingRequest;
import com.neo4flix.ratingservice.dto.RatingResponse;
import com.neo4flix.ratingservice.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour RatingService.
 * On mocke le Driver Neo4j pour éviter d'avoir une vraie base de données.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RatingService - Tests unitaires")
class RatingServiceTest {

    @Mock
    private Driver neo4jDriver;

    @Mock
    private Session session;

    @Mock
    private Result result;

    @Mock
    private Record record;

    @InjectMocks
    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        when(neo4jDriver.session()).thenReturn(session);
    }

    @Test
    @DisplayName("rateMovie : film inexistant → IllegalArgumentException")
    void rateMovie_movieNotFound() {
        RatingRequest request = new RatingRequest();
        request.setMovieId("unknown-movie-id");
        request.setScore(8.0);

        Result movieCheckResult = mock(Result.class);
        Record movieCheckRecord = mock(Record.class);
        Value falseValue = mock(Value.class);

        when(session.run(contains("MATCH (m:Movie"), anyMap())).thenReturn(movieCheckResult);
        when(movieCheckResult.single()).thenReturn(movieCheckRecord);
        when(movieCheckRecord.get("exists")).thenReturn(falseValue);
        when(falseValue.asBoolean()).thenReturn(false);

        assertThatThrownBy(() -> ratingService.rateMovie("user-123", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Film introuvable");
    }

    @Test
    @DisplayName("deleteRating : note non possédée → IllegalArgumentException")
    void deleteRating_notOwned() {
        Result ownedResult = mock(Result.class);
        Record ownedRecord = mock(Record.class);
        Value falseValue = mock(Value.class);

        when(session.run(contains("MATCH (u:User"), anyMap())).thenReturn(ownedResult);
        when(ownedResult.single()).thenReturn(ownedRecord);
        when(ownedRecord.get("owned")).thenReturn(falseValue);
        when(falseValue.asBoolean()).thenReturn(false);

        assertThatThrownBy(() -> ratingService.deleteRating("user-xyz", "rating-abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("introuvable ou accès refusé");
    }

    @Test
    @DisplayName("getRatingsByMovie : retourne une liste (peut être vide)")
    void getRatingsByMovie_returnsList() {
        when(session.run(contains("MATCH (u:User)-[r:RATED]->(m:Movie {movieId"), anyMap()))
                .thenReturn(result);
        when(result.list(any())).thenReturn(List.of());

        List<RatingResponse> ratings = ratingService.getRatingsByMovie("some-movie-id");

        assertThat(ratings).isNotNull();
        assertThat(ratings).isEmpty();
    }

    @Test
    @DisplayName("getRatingsByUser : retourne une liste (peut être vide)")
    void getRatingsByUser_returnsList() {
        when(session.run(contains("MATCH (u:User {userId"), anyMap()))
                .thenReturn(result);
        when(result.list(any())).thenReturn(List.of());

        List<RatingResponse> ratings = ratingService.getRatingsByUser("user-123");

        assertThat(ratings).isNotNull();
        assertThat(ratings).isEmpty();
    }
}
