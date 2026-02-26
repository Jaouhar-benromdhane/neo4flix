package com.neo4flix.userservice;

import com.neo4flix.userservice.dto.AuthResponse;
import com.neo4flix.userservice.dto.LoginRequest;
import com.neo4flix.userservice.dto.RegisterRequest;
import com.neo4flix.userservice.entity.Role;
import com.neo4flix.userservice.entity.User;
import com.neo4flix.userservice.exception.EmailAlreadyExistsException;
import com.neo4flix.userservice.repository.UserRepository;
import com.neo4flix.userservice.security.JwtUtil;
import com.neo4flix.userservice.service.AuthService;
import com.neo4flix.userservice.service.TwoFactorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthService.
 * Utilise Mockito pour simuler Neo4j → pas besoin de base de données.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Tests unitaires")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TwoFactorService twoFactorService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("alice");
        registerRequest.setEmail("alice@test.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("alice@test.com");
        loginRequest.setPassword("password123");

        savedUser = User.builder()
                .userId("uuid-123")
                .username("alice")
                .email("alice@test.com")
                .passwordHash("$2a$12$hashedpassword")
                .role(Role.USER.name())
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Register : succès → retourne AuthResponse avec token")
    void register_success() {
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$12$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken("alice@test.com", List.of("USER"), "uuid-123"))
                .thenReturn("eyJhbGciOiJIUzI1NiJ9.test.token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getEmail()).isEqualTo("alice@test.com");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("Register : email déjà utilisé → EmailAlreadyExistsException")
    void register_emailAlreadyExists() {
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("alice@test.com");
    }

    @Test
    @DisplayName("Login : succès → retourne AuthResponse avec token")
    void login_success() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("password123", "$2a$12$hashedpassword")).thenReturn(true);
        when(jwtUtil.generateToken("alice@test.com", List.of("USER"), "uuid-123"))
                .thenReturn("eyJhbGciOiJIUzI1NiJ9.test.token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getUsername()).isEqualTo("alice");
    }

    @Test
    @DisplayName("Login : mot de passe incorrect → IllegalArgumentException")
    void login_wrongPassword() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("password123", "$2a$12$hashedpassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email ou mot de passe incorrect");
    }

    @Test
    @DisplayName("Login : email inconnu → IllegalArgumentException")
    void login_unknownEmail() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email ou mot de passe incorrect");
    }

    @Test
    @DisplayName("Login : compte désactivé → IllegalArgumentException")
    void login_disabledAccount() {
        savedUser.setEnabled(false);
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(savedUser));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Compte désactivé");
    }
}
