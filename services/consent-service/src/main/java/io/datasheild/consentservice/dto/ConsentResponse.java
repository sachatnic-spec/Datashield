package io.datasheild.consentservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsentResponse {

    private UUID id;

    private UUID dataPrincipalId;

    private UUID purposeId;

    private String status;

    private LocalDateTime grantedAt;

    private LocalDateTime withdrawnAt;

    private LocalDateTime expiresAt;

    private String channel;

    private String ipAddress;

    private Boolean auditLogged;

    private String withdrawalReason;
}
