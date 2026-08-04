package io.datasheild.auth.dto;

import lombok.*;

/**
 * Request to initiate TOTP setup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFASetupRequest {
    private String tenantId;
    private String mfaType;  // TOTP, SMS, EMAIL, WEBAUTHN
    private String phoneNumber;  // For SMS
    private String email;  // For Email (optional, defaults to user email)
}
