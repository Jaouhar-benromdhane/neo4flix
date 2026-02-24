package com.neo4flix.recommendationservice;

import com.neo4flix.recommendationservice.dto.RecommendedMovie;
import com.neo4flix.recommendationservice.service.RecommendationService;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationService - Tests unitaires")
class RecommendationServiceTest {

    @Mock
    private Driver neo4jDriver;

    @Mock
    private Session session;

    @Mock
    private Result result;

    @InjectMocks
    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        when(neo4jDriver.session()).thenReturn(session);
    }

    @Test
    @DisplayName("getPopularMovies : retourne une liste (peut être vide)")
    void getPopularMovies_returnsList() {
        when(session.run(contains("popularityScore"), anyMap())).thenReturn(result);
        when(result.list(any())).thenReturn(List.of());

        List<RecommendedMovie> movies = recommendationService.getPopularMovies(10);

        assertThat(movies).isNotNull();
        assertThat(movies).isEmpty();
    }

    @Test
    @DisplayName("getSimilarMovies : film inexistant → IllegalArgumentException")
    void getSimilarMovies_movieNotFound() {
        Result checkResult = mock(Result.class);
        Record checkRecord = mock(Record.class);
        Value falseValue = mock(Value.class);

        when(session.run(contains("MATCH (m:Movie {movieId"), anyMap())).thenReturn(checkResult);
        when(checkResult.single()).thenReturn(checkRecord);
        when(checkRecord.get("exists")).thenReturn(falseValue);
        when(falseValue.asBoolean()).thenReturn(false);

        assertThatThrownBy(() -> recommendationService.getSimilarMovies("unknown-id", 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Film introuvable");
    }

    @Test
    @DisplayName("getCollaborativeRecommendations : retourne une liste")
    void getCollaborativeRecommendations_returnsList() {
        when(session.run(contains("MATCH (me:User"), anyMap())).thenReturn(result);
        when(result.list(any())).thenReturn(List.of());

        List<RecommendedMovie> recs = recommendationService.getCollaborativeRecommendations("user-123", 10);

        assertThat(recs).isNotNull();
    }

    @Test
    @DisplayName("getPersonalizedRecommendations : cold start → complète avec popular")
    void getPersonalizedRecommendations_coldStart() {
        // Filtrage collaboratif retourne 0 résultats → cold start
        when(session.run(contains("MATCH (me:User"), anyMap())).thenReturn(result);
        when(result.list(any())).thenReturn(List.of());

        // Films populaires retourne aussi 0 → liste vide attendue
        Result popularResult = mock(Result.class);
        when(session.run(contains("popularityScore"), anyMap())).thenReturn(popularResult);
        when(popularResult.list(any())).thenReturn(List.of());

        List<RecommendedMovie> recs = recommendationService.getPersonalizedRecommendations("new-user-id");

        assertThat(recs).isNotNull();
        assertThat(recs).isEmpty();
    }
}
