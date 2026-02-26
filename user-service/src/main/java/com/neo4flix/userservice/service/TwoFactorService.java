package com.neo4flix.userservice.service;

import com.neo4flix.userservice.entity.User;
import com.neo4flix.userservice.exception.UserNotFoundException;
import com.neo4flix.userservice.repository.UserRepository;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;

/**
 * Service pour la double authentification (2FA) via TOTP.
 *
 * Flux :
 *   1. POST /auth/2fa/setup        → génère un secret + QR code (scan avec Google Authenticator)
 *   2. POST /auth/2fa/enable       → vérifie le code TOTP et active la 2FA
 *   3. POST /auth/2fa/disable      → désactive la 2FA (code TOTP requis)
 *
 * Au login :
 *   - Si 2FA activée et code absent  → retourne { requires2FA: true }
 *   - Si 2FA activée et code fourni  → vérifie le code avant de délivrer le JWT
 */
@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final UserRepository userRepository;

    private static final String ISSUER = "Neo4flix";

    /**
     * Génère un nouveau secret TOTP et un QR code à scanner.
     * Le secret est stocké temporairement (non encore activé).
     *
     * @return Map contenant "secret" et "qrCodeBase64"
     */
    public Map<String, String> setupTwoFactor(String email) throws QrGenerationException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        // Générer un nouveau secret
        SecretGenerator secretGenerator = new DefaultSecretGenerator(32);
        String secret = secretGenerator.generate();

        // Sauvegarder le secret (2FA pas encore activée)
        user.setTwoFactorSecret(secret);
        userRepository.save(user);

        // Générer les données QR
        QrData qrData = new QrData.Builder()
                .label(user.getEmail())
                .secret(secret)
                .issuer(ISSUER)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        // Générer l'image QR en base64 (à afficher dans le frontend)
        QrGenerator qrGenerator = new ZxingPngQrGenerator();
        byte[] qrBytes = qrGenerator.generate(qrData);
        String qrBase64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(qrBytes);

        return Map.of("secret", secret, "qrCodeBase64", qrBase64);
    }

    /**
     * Vérifie le code TOTP et active la 2FA sur le compte.
     */
    public void enableTwoFactor(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        if (user.getTwoFactorSecret() == null) {
            throw new IllegalStateException("2FA non configurée. Appelez d'abord /auth/2fa/setup");
        }

        if (!verifyCode(user.getTwoFactorSecret(), code)) {
            throw new IllegalArgumentException("Code TOTP invalide");
        }

        user.setTwoFactorEnabled(true);
        userRepository.save(user);
    }

    /**
     * Désactive la 2FA après vérification du code.
     */
    public void disableTwoFactor(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        if (!user.isTwoFactorEnabled()) {
            throw new IllegalStateException("La 2FA n'est pas activée sur ce compte");
        }

        if (!verifyCode(user.getTwoFactorSecret(), code)) {
            throw new IllegalArgumentException("Code TOTP invalide");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);
    }

    /**
     * Vérifie un code TOTP. Utilisé lors du login si 2FA est activée.
     */
    public boolean verifyCode(String secret, String code) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6);
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        return verifier.isValidCode(secret, code);
    }

    /**
     * Retourne le statut 2FA de l'utilisateur.
     */
    public boolean isTwoFactorEnabled(String email) {
        return userRepository.findByEmail(email)
                .map(User::isTwoFactorEnabled)
                .orElse(false);
    }
}
