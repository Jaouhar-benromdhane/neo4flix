package com.neo4flix.userservice.config;

import com.neo4flix.userservice.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration Spring Security pour le user-service.
 * 
 * Règles d'accès :
 *   - POST /auth/**         → PUBLIC  (register + login)
 *   - GET  /users/me        → Authentifié (n'importe quel rôle)
 *   - GET  /users/**        → ADMIN seulement
 *   - Swagger               → PUBLIC
 *   - Tout le reste         → Authentifié
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Routes publiques : inscription et connexion uniquement
                .requestMatchers("/auth/register", "/auth/login").permitAll()
                // 2FA : nécessite d'être authentifié (JWT requis)
                .requestMatchers("/auth/2fa/**").authenticated()
                // OAuth2 callback : public (géré par Spring Security)
                .requestMatchers("/auth/oauth2/**", "/oauth2/**", "/login/oauth2/**").permitAll()
                // Swagger
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**"
                ).permitAll()
                // Profil personnel + watchlist/watched : tout utilisateur connecté
                .requestMatchers("/users/me", "/users/me/**").authenticated()
                // Liste des utilisateurs : ADMIN seulement
                .requestMatchers("/users/**").hasRole("ADMIN")
                // Tout le reste : authentifié
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/auth/oauth2/success", true)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt pour hacher les mots de passe.
     * Strength 12 = bon équilibre sécurité/performance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
