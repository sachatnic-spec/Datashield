package io.datasheild.consentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawConsentRequest {

    private UUID consentId;

    private String reason;  // User-provided reason for withdrawal

    public boolean isValid() {
        return consentId != null;
    }
}
