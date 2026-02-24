package com.neo4flix.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filtre global : intercepte TOUTES les requêtes qui passent par le gateway.
 *
 * Utilité :
 *   - Loggue chaque requête (méthode, chemin, IP source)
 *   - Mesure le temps de réponse
 *   - Utile pour le débogage et l'audit
 *
 * Ordered.HIGHEST_PRECEDENCE : s'exécute en premier parmi tous les filtres.
 */
@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        log.info("[GATEWAY] {} {} | IP: {}",
                request.getMethod(),
                request.getURI().getPath(),
                request.getRemoteAddress() != null
                    ? request.getRemoteAddress().getAddress().getHostAddress()
                    : "unknown"
        );

        // Laisse passer la requête, puis logue le temps de réponse
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value()
                    : 0;
            log.info("[GATEWAY] {} {} -> {} ({}ms)",
                    request.getMethod(),
                    request.getURI().getPath(),
                    statusCode,
                    duration
            );
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
