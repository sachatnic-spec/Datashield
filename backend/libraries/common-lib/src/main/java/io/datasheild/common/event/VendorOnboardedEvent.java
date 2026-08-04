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
public class VendorOnboardedEvent {
    private UUID vendorId;
    private String vendorName;
    private String vendorType;
    private LocalDateTime onboardedAt;
    private String correlationId;
}
