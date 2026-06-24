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
public class PolicyActivatedEvent {
    private UUID policyId;
    private String policyName;
    private String category;
    private LocalDateTime activatedAt;
    private String correlationId;
}
