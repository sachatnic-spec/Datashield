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
public class BreachReportedEvent {
    private UUID breachIncidentId;
    private Integer affectedRecords;
    private String dataType;
    private LocalDateTime discoveredAt;
    private String correlationId;
}
