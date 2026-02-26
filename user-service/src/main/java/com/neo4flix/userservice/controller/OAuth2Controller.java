package com.neo4flix.userservice.controller;

import com.neo4flix.userservice.dto.AuthResponse;
import com.neo4flix.userservice.entity.Role;
import com.neo4flix.userservice.entity.User;
import com.neo4flix.userservice.repository.UserRepository;
import com.neo4flix.userservice.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints OAuth2 — connexion via Google.
 *
 * Flux :
 *   1. Frontend redirige vers GET /oauth2/authorization/google
 *   2. Google redirige vers GET /auth/oauth2/callback (ce controller)
 *   3. Spring Security a déjà authentifié l'utilisateur OAuth2
 *   4. On crée/récupère le compte et on retourne un JWT Neo4flix
 *
 * Configuration nécessaire dans application.yml :
 *   spring.security.oauth2.client.registration.google.client-id=...
 *   spring.security.oauth2.client.registration.google.client-secret=...
 */
@RestController
@RequestMapping("/auth/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2", description = "Authentification via Google OAuth2")
public class OAuth2Controller {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Appelé après le callback Google réussi.
     * Spring Security a déjà validé le token Google via OAuth2LoginAuthenticationFilter.
     *
     * @param oauth2User L'utilisateur authentifié par Google (email, name, picture)
     * @return JWT Neo4flix
     */
    @GetMapping("/success")
    @Operation(summary = "Callback OAuth2 Google", description = "Retourne un JWT après authentification Google")
    public ResponseEntity<AuthResponse> oauth2Success(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return ResponseEntity.status(401).build();
        }

        String email = oauth2User.getAttribute("email");
        String name  = oauth2User.getAttribute("name");

        // Créer le compte si c'est le premier login Google
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .username(name != null ? name.replaceAll("\\s+", "_").toLowerCase() : email.split("@")[0])
                    .passwordHash("OAUTH2_GOOGLE") // pas de mot de passe pour les comptes OAuth2
                    .role(Role.USER.name())
                    .enabled(true)
                    .build();
            newUser.prePersist();
            return userRepository.save(newUser);
        });

        // Générer un JWT Neo4flix
        String token = jwtUtil.generateToken(
                user.getEmail(),
                List.of(user.getRole()),
                user.getUserId()
        );

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build());
    }
}
