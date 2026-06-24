package io.datasheild.rightsservice.dto;

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
public class VerifyIdentityRequest {

    private UUID requestId;

    private String otpCode;

    public boolean isValid() {
        return requestId != null && otpCode != null && otpCode.length() == 6;
    }
}
