package io.datasheild.common.event;

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
public class DPRSubmittedEvent {
    private UUID dprRequestId;
    private UUID dataPrincipalId;
    private String requestType;
    private LocalDateTime submittedAt;
    private String correlationId;
}
