package io.datasheild.consentservice.dto;

import io.datasheild.consentservice.entity.ConsentRecord;
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
public class GrantConsentRequest {

    private UUID dataPrincipalId;  // Consent grantor

    private UUID purposeId;  // Single purpose (no bundling per FR-CM-001)

    private String ipAddress;

    private String deviceFingerprint;

    private String channel;  // WEB, MOBILE, EMAIL, WHATSAPP

    private String metadata;  // JSON: {userAgent, locale, timestamp, etc}

    public boolean isValid() {
        return dataPrincipalId != null && purposeId != null && !channel.isEmpty();
    }
}
