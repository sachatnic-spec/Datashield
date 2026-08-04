package io.datasheild.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.datasheild.auth.entity.MFADevice;
import io.datasheild.auth.repository.MFADeviceRepository;
import io.datasheild.auth.exception.UnauthorizedException;
import io.datasheild.auth.exception.ValidationException;

import java.util.UUID;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests for MFA Service with Google Authenticator
 * Tests TOTP setup, verification, and device management
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("MFA Service Integration Tests")
public class MFAServiceIntegrationTest {

    @Autowired
    private MFAService mfaService;

    @Autowired
    private MFADeviceRepository mfaDeviceRepository;

    private UUID testUserId;
    private UUID testTenantId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testTenantId = UUID.randomUUID();
        
        mfaDeviceRepository.findAllByUserIdAndTenantId(testUserId, testTenantId)
                .forEach(device -> mfaDeviceRepository.delete(device));
    }

    @Test
    @DisplayName("Should generate TOTP secret and QR code on setup")
    void testTOTPSetupGeneratesSecretAndQRCode() {
        Map<String, Object> setup = mfaService.setupTOTP(testUserId, testTenantId);

        assertNotNull(setup, "Setup should not be null");
        assertTrue(setup.containsKey("mfaSetupId"), "Should have mfaSetupId");
        assertTrue(setup.containsKey("secret"), "Should have secret");
        assertTrue(setup.containsKey("qrCode"), "Should have qrCode");
        assertTrue(setup.containsKey("backupCodes"), "Should have backupCodes");
        
        assertEquals("TOTP", setup.get("mfaType"), "MFA type should be TOTP");
        
        String secret = (String) setup.get("secret");
        assertTrue(secret.matches("[A-Z2-7=]+"), "Secret should be Base32 encoded");
        assertTrue(secret.length() > 10, "Secret should be reasonably long");
        
        String qrCode = (String) setup.get("qrCode");
        assertTrue(qrCode.startsWith("data:image/png;base64,"), "QR code should be Base64 PNG");
        
        String[] backupCodes = (String[]) setup.get("backupCodes");
        assertEquals(8, backupCodes.length, "Should have 8 backup codes");
    }

    @Test
    @DisplayName("Should verify TOTP setup with valid code")
    void testTOTPVerificationWithValidCode() {
        Map<String, Object> setup = mfaService.setupTOTP(testUserId, testTenantId);
        String secret = (String) setup.get("secret");
        String validCode = generateTOTPCode(secret);

        MFADevice device = mfaService.verifyTOTPSetup(testUserId, testTenantId, secret, validCode);

        assertNotNull(device, "Device should be created");
        assertEquals(MFADevice.MFAType.TOTP, device.getType(), "Type should be TOTP");
        assertTrue(device.getVerified(), "Device should be verified");
        assertTrue(device.getActive(), "Device should be active");
    }

    @Test
    @DisplayName("Should reject TOTP verification with invalid code")
    void testTOTPVerificationWithInvalidCode() {
        Map<String, Object> setup = mfaService.setupTOTP(testUserId, testTenantId);
        String secret = (String) setup.get("secret");

        assertThrows(UnauthorizedException.class, () -> {
            mfaService.verifyTOTPSetup(testUserId, testTenantId, secret, "000000");
        });
    }

    @Test
    @DisplayName("Should validate TOTP code during login")
    void testTOTPCodeValidationDuringLogin() {
        Map<String, Object> setup = mfaService.setupTOTP(testUserId, testTenantId);
        String secret = (String) setup.get("secret");
        String validCode = generateTOTPCode(secret);
        mfaService.verifyTOTPSetup(testUserId, testTenantId, secret, validCode);

        String loginCode = generateTOTPCode(secret);
        boolean result = mfaService.validateTOTPCode(testUserId, testTenantId, loginCode);
        assertTrue(result, "Valid TOTP code should pass validation");
    }

    @Test
    @DisplayName("Should lock device after 5 failed attempts")
    void testTOTPDeviceLockoutAfter5Failures() {
        Map<String, Object> setup = mfaService.setupTOTP(testUserId, testTenantId);
        String secret = (String) setup.get("secret");
        String validCode = generateTOTPCode(secret);
        MFADevice device = mfaService.verifyTOTPSetup(testUserId, testTenantId, secret, validCode);

        for (int i = 1; i <= 5; i++) {
            try {
                mfaService.validateTOTPCode(testUserId, testTenantId, "000000");
                fail("Should throw exception");
            } catch (UnauthorizedException e) {
                // Expected
            }
        }

        MFADevice lockedDevice = mfaDeviceRepository.findById(device.getId()).orElse(null);
        assertFalse(lockedDevice.getActive(), "Device should be locked");
        assertEquals(5, lockedDevice.getFailedAttempts(), "Failed attempts should be 5");
    }

    @Test
    @DisplayName("Should list all MFA devices for user")
    void testListUserMFADevices() {
        Map<String, Object> setup1 = mfaService.setupTOTP(testUserId, testTenantId);
        String secret1 = (String) setup1.get("secret");
        String code1 = generateTOTPCode(secret1);
        mfaService.verifyTOTPSetup(testUserId, testTenantId, secret1, code1);

        Map<String, Object> setup2 = mfaService.setupTOTP(testUserId, testTenantId);
        String secret2 = (String) setup2.get("secret");
        String code2 = generateTOTPCode(secret2);
        mfaService.verifyTOTPSetup(testUserId, testTenantId, secret2, code2);

        java.util.List<MFADevice> devices = mfaService.listUserMFADevices(testUserId, testTenantId);
        assertEquals(2, devices.size(), "Should have 2 devices");
    }

    private String generateTOTPCode(String secret) {
        try {
            byte[] decodedSecret = decodeBase32(secret);
            javax.crypto.spec.SecretKeySpec key = new javax.crypto.spec.SecretKeySpec(
                    decodedSecret, 0, decodedSecret.length, "HmacSHA1");
            
            com.eatthepath.otp.TimeBasedOneTimePasswordGenerator totp = 
                    new com.eatthepath.otp.TimeBasedOneTimePasswordGenerator();
            
            long currentTimeWindow = System.currentTimeMillis() / 30000;
            java.time.Instant instant = java.time.Instant.ofEpochMilli(currentTimeWindow * 30000);
            long code = totp.generateOneTimePassword(key, instant);
            
            return String.format("%06d", code);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate TOTP code", e);
        }
    }

    private byte[] decodeBase32(String base32String) {
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
}
