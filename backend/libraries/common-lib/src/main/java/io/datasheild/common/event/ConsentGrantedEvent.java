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
public class ConsentGrantedEvent {
    private UUID consentRecordId;
    private UUID dataPrincipalId;
    private String purpose;
    private LocalDateTime grantedAt;
    private String source;
    private String correlationId;
}
