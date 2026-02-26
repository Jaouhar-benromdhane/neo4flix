package com.neo4flix.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Corps de la requête POST /auth/login
 */
@Data
public class LoginRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    /**
     * Code TOTP à 6 chiffres — obligatoire seulement si la 2FA est activée sur le compte.
     * Laisser null ou vide si la 2FA n'est pas configurée.
     */
    private String twoFactorCode;
}
