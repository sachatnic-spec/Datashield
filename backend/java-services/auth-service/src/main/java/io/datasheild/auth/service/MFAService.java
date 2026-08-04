package io.datasheild.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.datasheild.auth.entity.MFADevice;
import io.datasheild.auth.entity.User;
import io.datasheild.auth.exception.UnauthorizedException;
import io.datasheild.auth.exception.NotFoundException;
import io.datasheild.auth.exception.ValidationException;
import io.datasheild.auth.repository.MFADeviceRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MFA Service - Handles TOTP, SMS, and Email OTP validation
 * 
 * Defense-in-depth requirement: Password + MFA (TOTP or passkey) for initial login
 */
@Service
@Slf4j
public class MFAService {

    @Autowired
    private MFADeviceRepository mfaDeviceRepository;

    @Value("${datasheild.auth.mfa.totp-window:1}")
    private int totpWindow;  // Number of 30-second windows to check

    /**
     * Validate TOTP code for user
     * TOTP = Time-based One-Time Password (Google Authenticator, Authy, etc.)
     */
    public boolean validateTOTPCode(UUID userId, UUID tenantId, String mfaCode) {
        if (mfaCode == null || mfaCode.isEmpty()) {
            throw new ValidationException("MFA code is required");
        }

        if (!mfaCode.matches("\\d{6}")) {
            throw new ValidationException("MFA code must be 6 digits");
        }

        // Get active TOTP device for user
        MFADevice totpDevice = mfaDeviceRepository
                .findByUserIdAndTenantIdAndTypeAndActive(
                    userId, tenantId, MFADevice.MFAType.TOTP, true)
                .orElseThrow(() -> {
                    log.warn("TOTP device not found for user {} in tenant {}", userId, tenantId);
                    return new NotFoundException("TOTP device not configured");
                });

        if (!totpDevice.getVerified()) {
            throw new UnauthorizedException("TOTP device not verified");
        }

        // Validate TOTP code
        boolean isValid = validateTOTPCodeAgainstSecret(mfaCode, totpDevice.getSecret());

        if (!isValid) {
            // Increment failed attempts
            totpDevice.setFailedAttempts(totpDevice.getFailedAttempts() + 1);
            
            // Lock device after 5 failed attempts
            if (totpDevice.getFailedAttempts() >= 5) {
                totpDevice.setActive(false);
                log.warn("TOTP device locked after 5 failed attempts for user {}", userId);
            }
            
            mfaDeviceRepository.save(totpDevice);
            throw new UnauthorizedException("Invalid MFA code");
        }

        // Reset failed attempts on success
        totpDevice.setFailedAttempts(0);
        totpDevice.setLastUsedAt(LocalDateTime.now());
        mfaDeviceRepository.save(totpDevice);

        log.info("TOTP validation successful for user {} in tenant {}", userId, tenantId);
        return true;
    }

    /**
     * Validate SMS OTP code
     */
    public boolean validateSMSOTP(UUID userId, UUID tenantId, String mfaCode) {
        if (mfaCode == null || mfaCode.isEmpty()) {
            throw new ValidationException("MFA code is required");
        }

        if (!mfaCode.matches("\\d{6}")) {
            throw new ValidationException("MFA code must be 6 digits");
        }

        // Get active SMS device for user
        MFADevice smsDevice = mfaDeviceRepository
                .findByUserIdAndTenantIdAndTypeAndActive(
                    userId, tenantId, MFADevice.MFAType.SMS, true)
                .orElseThrow(() -> new NotFoundException("SMS device not configured"));

        if (!smsDevice.getVerified()) {
            throw new UnauthorizedException("SMS device not verified");
        }

        // In production: Compare against OTP sent to phone
        // This would typically involve:
        // 1. Lookup SMS OTP from cache/database (with expiration)
        // 2. Compare with provided code
        // 3. Mark SMS OTP as used
        // For now, we log the requirement
        
        log.info("SMS OTP validation required for user {} in tenant {} (implementation needed)", userId, tenantId);
        
        // TODO: Implement SMS OTP storage and validation
        throw new UnauthorizedException("SMS validation not yet implemented");
    }

    /**
     * Validate Email OTP code
     */
    public boolean validateEmailOTP(UUID userId, UUID tenantId, String mfaCode) {
        if (mfaCode == null || mfaCode.isEmpty()) {
            throw new ValidationException("MFA code is required");
        }

        if (!mfaCode.matches("\\d{6}")) {
            throw new ValidationException("MFA code must be 6 digits");
        }

        // Get active Email device for user
        MFADevice emailDevice = mfaDeviceRepository
                .findByUserIdAndTenantIdAndTypeAndActive(
                    userId, tenantId, MFADevice.MFAType.EMAIL, true)
                .orElseThrow(() -> new NotFoundException("Email device not configured"));

        if (!emailDevice.getVerified()) {
            throw new UnauthorizedException("Email device not verified");
        }

        // In production: Compare against OTP sent to email
        log.info("Email OTP validation required for user {} in tenant {} (implementation needed)", userId, tenantId);
        
        // TODO: Implement Email OTP storage and validation
        throw new UnauthorizedException("Email validation not yet implemented");
    }

    /**
     * Validate WebAuthn credential
     */
    public boolean validateWebAuthn(UUID userId, UUID tenantId, String credential) {
        // Get active WebAuthn device for user
        MFADevice webauthnDevice = mfaDeviceRepository
                .findByUserIdAndTenantIdAndTypeAndActive(
                    userId, tenantId, MFADevice.MFAType.WEBAUTHN, true)
                .orElseThrow(() -> new NotFoundException("WebAuthn device not configured"));

        if (!webauthnDevice.getVerified()) {
            throw new UnauthorizedException("WebAuthn device not verified");
        }

        // In production: Validate WebAuthn response
        log.info("WebAuthn validation required for user {} in tenant {} (implementation needed)", userId, tenantId);
        
        // TODO: Implement WebAuthn validation using FIDO2 library
        throw new UnauthorizedException("WebAuthn validation not yet implemented");
    }

    /**
     * Internal: Validate TOTP code against secret using time-based window
     * 
     * TOTP Algorithm:
     * 1. Current time / 30 seconds = index
     * 2. HMAC-SHA1(secret, index) = hash
     * 3. Extract 6 digits from hash = code
     * 
     * We check current time ± tolpWindow to handle clock skew
     */
    private boolean validateTOTPCodeAgainstSecret(String mfaCode, String secret) {
        try {
            // In production, use a library like:
            // - com.eatthepath:java-otp for TOTP
            // - org.jboss.aerogear:aerogear-otp-java
            // For now, we log the requirement
            
            log.debug("TOTP validation against secret (using library needed)");
            
            // TODO: Implement TOTP validation using Java-OTP library
            // Current implementation: Always fail until implemented
            // This prevents accidental bypass if someone forgets to implement
            
            return false;
        } catch (Exception e) {
            log.error("TOTP validation failed", e);
            return false;
        }
    }

    /**
     * Check if user has MFA enabled
     */
    public boolean isMFAEnabled(User user) {
        return user.getMfaEnabled();
    }

    /**
     * Get user's preferred MFA method
     */
    public User.MFAMethod getUserPreferredMFAMethod(User user) {
        return user.getPreferredMfaMethod();
    }

    /**
     * Check if user has any active MFA devices
     */
    public boolean hasActiveMFADevice(UUID userId, UUID tenantId, MFADevice.MFAType type) {
        return mfaDeviceRepository
                .findByUserIdAndTenantIdAndTypeAndActive(userId, tenantId, type, true)
                .isPresent();
    }

    /**
     * Disable MFA for user (for testing only, should require additional auth in production)
     */
    public void disableMFA(UUID userId, UUID tenantId) {
        mfaDeviceRepository.findAllByUserIdAndTenantId(userId, tenantId)
                .forEach(device -> {
                    device.setActive(false);
                    mfaDeviceRepository.save(device);
                });
        log.warn("All MFA devices disabled for user {} in tenant {}", userId, tenantId);
    }
}
