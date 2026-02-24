package com.neo4flix.userservice.service;

import com.neo4flix.userservice.dto.UserResponse;
import com.neo4flix.userservice.entity.User;
import com.neo4flix.userservice.exception.UserNotFoundException;
import com.neo4flix.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service de gestion des profils utilisateurs.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Récupère un utilisateur par son email (utilisé par /users/me).
     */
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé : " + email));
        return toResponse(user);
    }

    /**
     * Récupère un utilisateur par son userId métier.
     */
    public UserResponse getUserById(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé : " + userId));
        return toResponse(user);
    }

    /**
     * Liste tous les utilisateurs (ADMIN uniquement).
     */
    public List<UserResponse> getAllUsers() {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Désactive un compte (soft delete).
     */
    public void disableUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé : " + userId));
        user.setEnabled(false);
        userRepository.save(user);
    }

    /**
     * Convertit un User (entité Neo4j) en UserResponse (DTO).
     * Ne jamais exposer passwordHash !
     */
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
