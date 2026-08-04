package io.datasheild.auth.dto;

import lombok.*;

/**
 * Response after MFA verification - device is now active
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFAVerifyResponse {
    private String mfaDeviceId;    // ID of the registered device
    private String mfaType;        // TOTP, SMS, EMAIL, WEBAUTHN
    private String status;         // VERIFIED, ACTIVE
    private String message;        // Confirmation message
}
