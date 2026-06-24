package io.datasheild.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.datasheild.auth.dto.LoginRequest;
import io.datasheild.auth.dto.LoginResponse;
import io.datasheild.auth.dto.RefreshTokenRequest;
import io.datasheild.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authentication", description = "OAuth2 JWT Authentication Endpoints")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login with credentials", description = "Authenticate user and return JWT access/refresh tokens")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for tenant: {}", request.getTenantId());
        LoginResponse response = authService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Use refresh token to get new access token")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request for tenant: {}", request.getTenantId());
        LoginResponse response = authService.refreshAccessToken(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoke all sessions and tokens for the user")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        UUID tenantId = (UUID) request.getAttribute("tenantId");

        if (userId == null || tenantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        authService.logout(userId, tenantId);
        log.info("User logged out: {} in tenant: {}", userId, tenantId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Simple health check endpoint")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is healthy");
    }
}
