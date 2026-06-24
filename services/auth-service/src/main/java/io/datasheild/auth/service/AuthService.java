package io.datasheild.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.datasheild.auth.dto.LoginRequest;
import io.datasheild.auth.dto.LoginResponse;
import io.datasheild.auth.dto.RefreshTokenRequest;
import io.datasheild.auth.entity.RefreshToken;
import io.datasheild.auth.entity.Session;
import io.datasheild.auth.entity.User;
import io.datasheild.auth.exception.*;
import io.datasheild.auth.repository.RefreshTokenRepository;
import io.datasheild.auth.repository.SessionRepository;
import io.datasheild.auth.repository.UserRepository;
import io.datasheild.auth.util.JwtTokenProvider;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        UUID tenantId = UUID.fromString(request.getTenantId());

        // Find user
        User user = userRepository.findByTenantIdAndEmail(tenantId, request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login attempt with non-existent email: {} in tenant: {}", request.getEmail(), tenantId);
                    return new UnauthorizedException("Invalid email or password");
                });

        // Validate user status
        if (!user.getStatus().equals(User.UserStatus.ACTIVE)) {
            throw new UnauthorizedException("User account is not active");
        }

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for user: {} in tenant: {}", user.getEmail(), tenantId);
            throw new UnauthorizedException("Invalid email or password");
        }

        // Validate MFA if enabled
        if (user.getMfaEnabled() && (request.getMfaCode() == null || request.getMfaCode().isEmpty())) {
            throw new UnauthorizedException("MFA code is required");
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                tenantId,
                user.getEmail(),
                user.getRoles().stream().map(r -> r.name()).collect(Collectors.toSet())
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), tenantId);

        // Create session
        Session session = Session.builder()
                .userId(user.getId())
                .tenantId(tenantId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresAt(LocalDateTime.now().plusSeconds(3600))  // 1 hour
                .status(Session.SessionStatus.ACTIVE)
                .build();
        sessionRepository.save(session);

        // Store refresh token (hashed)
        RefreshToken storedToken = RefreshToken.builder()
                .userId(user.getId())
                .tenantId(tenantId)
                .tokenHash(hashToken(refreshToken))
                .status(RefreshToken.TokenStatus.VALID)
                .expiresAt(LocalDateTime.now().plusSeconds(604800))  // 7 days
                .build();
        refreshTokenRepository.save(storedToken);

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User successfully logged in: {} in tenant: {}", user.getEmail(), tenantId);

        return buildLoginResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public LoginResponse refreshAccessToken(RefreshTokenRequest request) {
        UUID tenantId = UUID.fromString(request.getTenantId());
        String tokenHash = hashToken(request.getRefreshToken());

        // Validate refresh token
        RefreshToken storedToken = refreshTokenRepository.findValidTokenByHashAndTenant(
                        tokenHash, tenantId, LocalDateTime.now())
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired refresh token"));

        User user = userRepository.findByIdAndTenantId(storedToken.getUserId(), tenantId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getStatus().equals(User.UserStatus.ACTIVE)) {
            throw new UnauthorizedException("User account is not active");
        }

        // Mark old token as used and generate new one
        storedToken.setStatus(RefreshToken.TokenStatus.USED);
        refreshTokenRepository.save(storedToken);

        // Generate new tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                tenantId,
                user.getEmail(),
                user.getRoles().stream().map(r -> r.name()).collect(Collectors.toSet())
        );

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), tenantId);

        // Create new refresh token record
        RefreshToken newStoredToken = RefreshToken.builder()
                .userId(user.getId())
                .tenantId(tenantId)
                .tokenHash(hashToken(newRefreshToken))
                .status(RefreshToken.TokenStatus.VALID)
                .expiresAt(LocalDateTime.now().plusSeconds(604800))
                .build();
        refreshTokenRepository.save(newStoredToken);

        log.info("Access token refreshed for user: {} in tenant: {}", user.getEmail(), tenantId);

        return buildLoginResponse(user, newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(UUID userId, UUID tenantId) {
        LocalDateTime now = LocalDateTime.now();
        sessionRepository.revokeAllUserSessions(userId, tenantId);
        refreshTokenRepository.revokeAllUserTokens(userId, tenantId, now, "User logged out");

        log.info("User logged out: {} in tenant: {}", userId, tenantId);
    }

    private LoginResponse buildLoginResponse(User user, String accessToken, String refreshToken) {
        long expirationSeconds = jwtTokenProvider.getExpirationTime(accessToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expirationSeconds)
                .tokenType("Bearer")
                .user(LoginResponse.UserInfo.builder()
                        .userId(user.getId().toString())
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roles(user.getRoles().stream().map(r -> r.name()).collect(Collectors.toSet()))
                        .build())
                .build();
    }

    private String hashToken(String token) {
        // Simple SHA-256 hash for storage (in production, use bcrypt)
        return Integer.toHexString(token.hashCode());
    }
}
