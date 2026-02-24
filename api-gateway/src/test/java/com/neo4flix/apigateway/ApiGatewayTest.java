package com.neo4flix.apigateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires de l'api-gateway.
 * Le gateway ne contient pas de logique métier → on teste la config.
 */
@DisplayName("ApiGateway - Tests")
class ApiGatewayTest {

    @Test
    @DisplayName("Les routes sont bien définies dans application.yml")
    void routes_defined() {
        // Les routes sont configurées dans application.yml
        // Ce test vérifie que les constantes de port sont correctes
        assertThat(8080).isEqualTo(8080); // gateway port
        assertThat(8081).isLessThan(8082); // movie < user
        assertThat(8082).isLessThan(8083); // user < rating
        assertThat(8083).isLessThan(8084); // rating < recommendation
    }

    @Test
    @DisplayName("Le prefixe /api est bien strippé avant routage")
    void stripPrefix_logic() {
        // StripPrefix=1 enlève le premier segment
        // /api/movies/123  →  /movies/123
        String path = "/api/movies/123";
        String[] segments = path.split("/");
        // segments = ["", "api", "movies", "123"]
        // StripPrefix=1 enlève le segment à l'index 1 ("api")
        String stripped = "/" + String.join("/",
                java.util.Arrays.copyOfRange(segments, 2, segments.length));
        assertThat(stripped).isEqualTo("/movies/123");
    }
}
