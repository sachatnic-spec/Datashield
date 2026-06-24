package io.datasheild.rightsservice.service;

import io.datasheild.rightsservice.entity.IdentityVerificationOTP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@Slf4j
public class OTPService {

    private static final int OTP_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    public String generateOTP() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public String hashOTP(String otp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(otp.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error hashing OTP: {}", e.getMessage());
            throw new RuntimeException("OTP hashing failed");
        }
    }

    public boolean verifyOTP(IdentityVerificationOTP otp, String providedCode) {
        // Check expiry
        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            otp.setStatus(IdentityVerificationOTP.OTPStatus.EXPIRED);
            return false;
        }

        // Check attempts
        if (otp.getAttemptCount() >= 3) {
            otp.setStatus(IdentityVerificationOTP.OTPStatus.FAILED);
            return false;
        }

        // Verify code
        String hashedProvided = hashOTP(providedCode);
        return hashedProvided.equals(otp.getHashedOtp());
    }
}
