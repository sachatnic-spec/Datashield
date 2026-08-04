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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Base64;

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

    @Autowired
    private MFAService mfaService;

    @Autowired
    private DeviceAnomalyService anomalyService;

    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
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
        if (user.getMfaEnabled()) {
            if (request.getMfaCode() == null || request.getMfaCode().isEmpty()) {
                throw new UnauthorizedException("MFA code is required");
            }
            // Validate MFA code (TOTP, SMS, Email, WebAuthn)
            boolean mfaValid = mfaService.validateTOTPCode(user.getId(), tenantId, request.getMfaCode());
            if (!mfaValid) {
                log.warn("Failed MFA validation for user: {} in tenant: {}", user.getEmail(), tenantId);
                throw new UnauthorizedException("Invalid MFA code");
            }
            log.info("MFA validation successful for user: {}", user.getEmail());
        }

        // Detect device anomalies (IP change, device change, impossible travel)
        try {
            DeviceAnomalyService.AnomalyRiskLevel riskLevel = anomalyService.detectAnomalies(
                    user.getId(), tenantId, ipAddress, userAgent);
            
            if (riskLevel == DeviceAnomalyService.AnomalyRiskLevel.HIGH) {
                log.warn("High-risk login detected for user: {} in tenant: {} from IP: {}", 
                    user.getEmail(), tenantId, ipAddress);
                // In production: Trigger step-up authentication
                // For now: Log the anomaly and allow login (can be gated by feature flag)
                anomalyService.logAnomaly(user.getId(), tenantId, riskLevel, 
                    "High-risk login: impossible travel or multiple anomalies");
            } else if (riskLevel == DeviceAnomalyService.AnomalyRiskLevel.MEDIUM) {
                log.info("Medium-risk login detected for user: {} - IP or device change", user.getEmail());
            }
        } catch (Exception e) {
            log.error("Device anomaly detection failed", e);
            // Don't block login if anomaly detection fails
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                tenantId,
                user.getEmail(),
                user.getRoles().stream().map(r -> r.name()).collect(Collectors.toSet())
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), tenantId);

        // Create session with device tracking
        Session session = Session.builder()
                .userId(user.getId())
                .tenantId(tenantId)
                .accessToken(accessToken)
                .tokenHash(secureHashToken(accessToken))
                .refreshToken(refreshToken)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(LocalDateTime.now().plusSeconds(3600))  // 1 hour
                .status(Session.SessionStatus.ACTIVE)
                .build();
        sessionRepository.save(session);

        // Store refresh token (hashed) with device context
        RefreshToken storedToken = RefreshToken.builder()
                .userId(user.getId())
                .tenantId(tenantId)
                .tokenHash(secureHashToken(refreshToken))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .status(RefreshToken.TokenStatus.VALID)
                .expiresAt(LocalDateTime.now().plusSeconds(604800))  // 7 days
                .build();
        refreshTokenRepository.save(storedToken);

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User successfully logged in: {} in tenant: {} from IP: {}", user.getEmail(), tenantId, ipAddress);

        return buildLoginResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public LoginResponse refreshAccessToken(RefreshTokenRequest request, String ipAddress, String userAgent) {
        UUID tenantId = UUID.fromString(request.getTenantId());
        String tokenHash = secureHashToken(request.getRefreshToken());

        // Validate refresh token
        RefreshToken storedToken = refreshTokenRepository.findValidTokenByHashAndTenant(
                        tokenHash, tenantId, LocalDateTime.now())
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired refresh token"));

        User user = userRepository.findByIdAndTenantId(storedToken.getUserId(), tenantId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getStatus().equals(User.UserStatus.ACTIVE)) {
            throw new UnauthorizedException("User account is not active");
        }

        // Check for IP change (optional step-up auth trigger)
        if (storedToken.getIpAddress() != null && !storedToken.getIpAddress().equals(ipAddress)) {
            log.warn("IP address change detected for user {} - Old: {}, New: {}", 
                    user.getEmail(), storedToken.getIpAddress(), ipAddress);
            // In production: trigger step-up authentication
        }

        // Detect device anomalies during refresh (IP change, device change)
        try {
            DeviceAnomalyService.AnomalyRiskLevel riskLevel = anomalyService.detectAnomalies(
                    user.getId(), tenantId, ipAddress, userAgent);
            
            if (riskLevel == DeviceAnomalyService.AnomalyRiskLevel.HIGH) {
                log.warn("High-risk token refresh detected for user: {} in tenant: {} from IP: {}", 
                    user.getEmail(), tenantId, ipAddress);
                // In production: Trigger step-up authentication or block refresh
            } else if (riskLevel == DeviceAnomalyService.AnomalyRiskLevel.MEDIUM) {
                log.info("Medium-risk token refresh for user: {} - IP or device change detected", user.getEmail());
            }
        } catch (Exception e) {
            log.error("Device anomaly detection failed during refresh", e);
            // Don't block refresh if anomaly detection fails
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

        // Create new refresh token record with device context
        RefreshToken newStoredToken = RefreshToken.builder()
                .userId(user.getId())
                .tenantId(tenantId)
                .tokenHash(secureHashToken(newRefreshToken))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .status(RefreshToken.TokenStatus.VALID)
                .expiresAt(LocalDateTime.now().plusSeconds(604800))
                .build();
        refreshTokenRepository.save(newStoredToken);

        log.info("Access token refreshed for user: {} in tenant: {} from IP: {}", user.getEmail(), tenantId, ipAddress);

        return buildLoginResponse(user, newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(UUID userId, UUID tenantId) {
        LocalDateTime now = LocalDateTime.now();
        sessionRepository.revokeAllUserSessions(userId, tenantId, now);
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

    private String secureHashToken(String token) {
        try {
            // Use SHA-256 for secure hashing (not cryptographic key material)
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new InternalServerException("Token hashing failed");
        }
    }
}
