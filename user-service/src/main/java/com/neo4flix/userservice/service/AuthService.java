package com.neo4flix.userservice.service;

import com.neo4flix.userservice.dto.AuthResponse;
import com.neo4flix.userservice.dto.LoginRequest;
import com.neo4flix.userservice.dto.RegisterRequest;
import com.neo4flix.userservice.entity.Role;
import com.neo4flix.userservice.entity.User;
import com.neo4flix.userservice.exception.EmailAlreadyExistsException;
import com.neo4flix.userservice.repository.UserRepository;
import com.neo4flix.userservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service d'authentification.
 * Gère l'inscription et la connexion des utilisateurs.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TwoFactorService twoFactorService;

    /**
     * Inscription d'un nouvel utilisateur.
     * 
     * Étapes :
     *   1. Vérifie que l'email n'existe pas déjà
     *   2. Hash le mot de passe avec BCrypt
     *   3. Sauvegarde l'utilisateur dans Neo4j
     *   4. Génère un JWT et retourne une AuthResponse
     */
    public AuthResponse register(RegisterRequest request) {
        // Vérification unicité email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                "L'email '" + request.getEmail() + "' est déjà utilisé"
            );
        }

        // Vérification unicité username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new EmailAlreadyExistsException(
                "Le username '" + request.getUsername() + "' est déjà utilisé"
            );
        }

        // Construction de l'entité User
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER.name())
                .enabled(true)
                .build();

        // Initialise userId et timestamps
        user.prePersist();

        // Sauvegarde dans Neo4j
        User saved = userRepository.save(user);

        // Génération du JWT
        String token = jwtUtil.generateToken(
                saved.getEmail(),
                List.of(saved.getRole()),
                saved.getUserId()
        );

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(saved.getUserId())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .role(saved.getRole())
                .build();
    }

    /**
     * Connexion d'un utilisateur existant.
     * 
     * Étapes :
     *   1. Cherche l'utilisateur par email
     *   2. Vérifie le mot de passe avec BCrypt
     *   3. Génère un JWT et retourne une AuthResponse
     */
    public AuthResponse login(LoginRequest request) {
        // Cherche l'utilisateur
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect"));

        // Vérifie que le compte est actif
        if (!user.isEnabled()) {
            throw new IllegalArgumentException("Compte désactivé");
        }

        // Vérifie le mot de passe (BCrypt compare hash stocké avec mot de passe fourni)
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Email ou mot de passe incorrect");
        }

        // ── 2FA : si activée, vérifier le code TOTP ──
        if (user.isTwoFactorEnabled()) {
            String code = request.getTwoFactorCode();
            if (code == null || code.isBlank()) {
                // Mot de passe correct mais code 2FA manquant → demander le code
                return AuthResponse.builder()
                        .requires2FA(true)
                        .userId(user.getUserId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build();
            }
            // Vérifier le code TOTP
            if (!twoFactorService.verifyCode(user.getTwoFactorSecret(), code)) {
                throw new IllegalArgumentException("Code 2FA invalide");
            }
        }

        // Génération du JWT
        String token = jwtUtil.generateToken(
                user.getEmail(),
                List.of(user.getRole()),
                user.getUserId()
        );

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .requires2FA(false)
                .build();
    }
}
