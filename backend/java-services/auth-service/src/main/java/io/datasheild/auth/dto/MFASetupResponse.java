package io.datasheild.auth.dto;

import lombok.*;

/**
 * Response from TOTP setup - contains QR code and backup codes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFASetupResponse {
    private String mfaSetupId;      // Temporary ID to link verification
    private String mfaType;         // TOTP, SMS, EMAIL, WEBAUTHN
    private String secret;          // Base32 encoded secret for TOTP
    private String qrCode;          // Base64 encoded QR code image for TOTP
    private String[] backupCodes;   // 8 backup codes for recovery
    private String verificationUrl; // For manual entry (if QR fails)
}
