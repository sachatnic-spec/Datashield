package io.datasheild.auth.dto;

import lombok.*;

/**
 * Request to verify MFA setup (confirm TOTP code matches secret)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFAVerifyRequest {
    private String tenantId;
    private String mfaSetupId;     // From setup response
    private String verificationCode; // TOTP code, SMS code, or Email code
}
