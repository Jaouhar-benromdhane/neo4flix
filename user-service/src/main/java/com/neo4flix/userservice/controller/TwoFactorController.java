package com.neo4flix.userservice.controller;

import com.neo4flix.userservice.dto.ApiResponse;
import com.neo4flix.userservice.service.TwoFactorService;
import dev.samstevens.totp.exceptions.QrGenerationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints pour la double authentification (2FA TOTP).
 *
 * POST /auth/2fa/setup    → génère le secret + QR code (JWT requis)
 * POST /auth/2fa/enable   → active la 2FA après scan du QR (JWT requis, body: {code})
 * POST /auth/2fa/disable  → désactive la 2FA (JWT requis, body: {code})
 * GET  /auth/2fa/status   → statut 2FA du compte connecté
 */
@RestController
@RequestMapping("/auth/2fa")
@RequiredArgsConstructor
@Tag(name = "2FA", description = "Double authentification TOTP (Google Authenticator)")
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    /**
     * Génère un secret TOTP et un QR code à scanner avec Google Authenticator.
     * Stocke le secret sur le compte mais n'active pas encore la 2FA.
     */
    @PostMapping("/setup")
    @Operation(summary = "Configurer la 2FA", description = "Génère un QR code à scanner avec Google Authenticator")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Map<String, String>>> setup(Authentication auth) throws QrGenerationException {
        Map<String, String> result = twoFactorService.setupTwoFactor(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * Active la 2FA après vérification du code TOTP scanné.
     */
    @PostMapping("/enable")
    @Operation(summary = "Activer la 2FA", description = "Vérifie le code et active la 2FA sur le compte")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> enable(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String code = body.get("code");
        twoFactorService.enableTwoFactor(auth.getName(), code);
        return ResponseEntity.ok(ApiResponse.ok("2FA activée avec succès", null));
    }

    /**
     * Désactive la 2FA (nécessite de fournir un code TOTP valide).
     */
    @PostMapping("/disable")
    @Operation(summary = "Désactiver la 2FA", description = "Désactive la 2FA (code TOTP requis)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> disable(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String code = body.get("code");
        twoFactorService.disableTwoFactor(auth.getName(), code);
        return ResponseEntity.ok(ApiResponse.ok("2FA désactivée", null));
    }

    /**
     * Retourne le statut 2FA du compte connecté.
     */
    @GetMapping("/status")
    @Operation(summary = "Statut 2FA")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> status(Authentication auth) {
        boolean enabled = twoFactorService.isTwoFactorEnabled(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("twoFactorEnabled", enabled)));
    }
}
