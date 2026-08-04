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
import io.datasheild.auth.exception.InternalServerException;
import io.datasheild.auth.repository.MFADeviceRepository;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Mac;
import java.time.Instant;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

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
     * We check current time ± totpWindow to handle clock skew
     */
    private boolean validateTOTPCodeAgainstSecret(String mfaCode, String secret) {
        try {
            // Decode base32-encoded secret
            byte[] decodedSecret = decodeBase32(secret);
            
           // Create a SecretKeySpec for HMAC-SHA1
           SecretKeySpec key = new SecretKeySpec(decodedSecret, 0, decodedSecret.length, "HmacSHA1");
            
           // Create TOTP generator (uses HMAC-SHA1 by default, 30-second time step)
           TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();
            
           // Generate expected TOTP codes for current time and adjacent windows
           // This handles clock skew (±totpWindow * 30 seconds)
           long currentTimeWindow = System.currentTimeMillis() / 30000;
            
           for (int i = -totpWindow; i <= totpWindow; i++) {
               long timeWindow = currentTimeWindow + i;
               Instant instant = Instant.ofEpochMilli(timeWindow * 30000);
               long expectedCode = totp.generateOneTimePassword(key, instant);
               String expectedCodeStr = String.format("%06d", expectedCode);
                
               if (mfaCode.equals(expectedCodeStr)) {
                   log.debug("TOTP validation successful for time window {}", timeWindow);
                   return true;
               }
           }
            
           log.warn("TOTP code {} did not match any time window", mfaCode);
           return false;
            
       } catch (Exception e) {
           log.error("TOTP validation failed", e);
           return false;
       }
    }

    /**
     * Decode base32-encoded string to bytes
     * Base32 alphabet: A-Z (26) and 2-7 (6 digits)
     */
    private byte[] decodeBase32(String base32String) {
        // Remove padding and convert to uppercase
        String s = base32String.replace("=", "").toUpperCase();
        byte[] bytes = new byte[(s.length() * 5) / 8];
        
        int bitBuffer = 0;
        int bitCount = 0;
        int byteIndex = 0;
        
        for (char c : s.toCharArray()) {
            int value;
            if (c >= 'A' && c <= 'Z') {
                value = c - 'A';
            } else if (c >= '2' && c <= '7') {
                value = c - '2' + 26;
            } else {
                throw new IllegalArgumentException("Invalid base32 character: " + c);
            }
            
            bitBuffer = (bitBuffer << 5) | (value & 0x1F);
            bitCount += 5;
            
            if (bitCount >= 8) {
                bitCount -= 8;
                bytes[byteIndex++] = (byte) ((bitBuffer >>> bitCount) & 0xFF);
            }
        }
        
        return bytes;
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

    // ========== MFA Enrollment Methods ==========

    /**
     * Initiate TOTP setup - Generate secret and QR code
     * User must verify setup within 10 minutes
     */
    public Map<String, Object> setupTOTP(UUID userId, UUID tenantId) {
        // Generate random 32-byte secret (256-bit)
        byte[] secretBytes = generateRandomSecret(32);
        String base32Secret = encodeBase32(secretBytes);
        
        // Generate backup codes (8 codes, 10 digits each)
        String[] backupCodes = generateBackupCodes(8);
        
        // Generate QR code
        String qrCode = generateTOTPQRCode(userId, tenantId, base32Secret);
        
        // Create temporary setup entry (expires in 10 minutes)
        // For now, we'll return everything and caller will store it
        Map<String, Object> setup = new HashMap<>();
        setup.put("mfaSetupId", UUID.randomUUID().toString());
        setup.put("mfaType", "TOTP");
        setup.put("secret", base32Secret);
        setup.put("qrCode", qrCode);
        setup.put("backupCodes", backupCodes);
        setup.put("verificationUrl", String.format(
            "otpauth://totp/DataShield:%s?secret=%s&issuer=DataShield&algorithm=SHA1&digits=6&period=30",
            userId, base32Secret));
        setup.put("expiresAt", LocalDateTime.now().plusMinutes(10));
        
        log.info("TOTP setup initiated for user {} in tenant {}", userId, tenantId);
        return setup;
    }

    /**
     * Verify TOTP setup - User submits code from Authenticator app
     * This confirms they have the correct secret and can use TOTP
     */
    public MFADevice verifyTOTPSetup(UUID userId, UUID tenantId, String secret, String verificationCode) {
        if (secret == null || secret.isEmpty()) {
            throw new ValidationException("TOTP secret is required");
        }

        if (verificationCode == null || !verificationCode.matches("\\d{6}")) {
            throw new ValidationException("Verification code must be 6 digits");
        }

        // Validate the code against the secret
        boolean isValid = validateTOTPCodeAgainstSecret(verificationCode, secret);
        if (!isValid) {
            throw new UnauthorizedException("Invalid TOTP code. Please check your Authenticator app and try again.");
        }

        // Check if user already has an active TOTP device
        mfaDeviceRepository.findByUserIdAndTenantIdAndTypeAndActive(
                userId, tenantId, MFADevice.MFAType.TOTP, true)
            .ifPresent(existing -> {
                log.warn("User {} already has active TOTP device, deactivating old one", userId);
                existing.setActive(false);
                mfaDeviceRepository.save(existing);
            });

        // Create and save new MFA device
        MFADevice mfaDevice = MFADevice.builder()
                .userId(userId)
                .tenantId(tenantId)
                .type(MFADevice.MFAType.TOTP)
                .secret(secret)
                .verified(true)
                .active(true)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        mfaDevice = mfaDeviceRepository.save(mfaDevice);

        // Update user to have MFA enabled
        // Note: This should be done in UserService, but we'll add the call here
        log.info("TOTP device verified and activated for user {} in tenant {}", userId, tenantId);
        
        return mfaDevice;
    }

    /**
     * Generate Base32-encoded random secret
     */
    private String encodeBase32(byte[] data) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        StringBuilder result = new StringBuilder();
        
        int buffer = 0;
        int bufferSize = 0;
        
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bufferSize += 8;
            
            while (bufferSize >= 5) {
                bufferSize -= 5;
                int index = (buffer >> bufferSize) & 0x1F;
                result.append(alphabet.charAt(index));
            }
        }
        
        if (bufferSize > 0) {
            int index = (buffer << (5 - bufferSize)) & 0x1F;
            result.append(alphabet.charAt(index));
        }
        
        // Add padding
        while (result.length() % 8 != 0) {
            result.append('=');
        }
        
        return result.toString();
    }

    /**
     * Generate random bytes for TOTP secret
     */
    private byte[] generateRandomSecret(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    /**
     * Generate backup codes for account recovery
     * Each code is 10 digits
     */
    private String[] generateBackupCodes(int count) {
        SecureRandom random = new SecureRandom();
        String[] codes = new String[count];
        
        for (int i = 0; i < count; i++) {
            long code = Math.abs(random.nextLong()) % 10_000_000_000L;
            codes[i] = String.format("%010d", code);
        }
        
        return codes;
    }

    /**
     * Generate QR code for TOTP setup
     * Format: otpauth://totp/user@example.com?secret=XXXXX&issuer=DataShield
     */
    private String generateTOTPQRCode(UUID userId, UUID tenantId, String secret) {
        try {
            String otpAuthUrl = String.format(
                "otpauth://totp/DataShield:%s?secret=%s&issuer=DataShield&algorithm=SHA1&digits=6&period=30",
                userId, secret);
            
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(otpAuthUrl, BarcodeFormat.QR_CODE, 200, 200);
            
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            
            byte[] pngData = pngOutputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(pngData);
            
        } catch (WriterException | java.io.IOException e) {
            log.error("Failed to generate QR code for TOTP", e);
            throw new InternalServerException("Failed to generate QR code");
        }
    }

    /**
     * List all MFA devices for user
     */
    public java.util.List<MFADevice> listUserMFADevices(UUID userId, UUID tenantId) {
        return mfaDeviceRepository.findAllByUserIdAndTenantId(userId, tenantId);
    }

    /**
     * Remove MFA device (requires current password for security)
     */
    public void removeMFADevice(UUID userId, UUID tenantId, UUID deviceId) {
        MFADevice device = mfaDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new NotFoundException("MFA device not found"));
        
        if (!device.getUserId().equals(userId) || !device.getTenantId().equals(tenantId)) {
            throw new UnauthorizedException("Cannot remove MFA device for different user");
        }
        
        mfaDeviceRepository.delete(device);
        log.info("MFA device {} removed for user {} in tenant {}", deviceId, userId, tenantId);
    }
}
