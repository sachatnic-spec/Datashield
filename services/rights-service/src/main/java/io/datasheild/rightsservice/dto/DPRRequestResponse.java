package io.datasheild.rightsservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.datasheild.rightsservice.entity.DPRRequest;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DPRRequestResponse {

    private UUID id;

    private DPRRequest.DPRType requestType;

    private DPRRequest.DPRStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime slaDeadline;

    private LocalDateTime verifiedAt;

    private LocalDateTime completedAt;

    private Boolean identityVerified;

    private String channel;

    private Integer activityCount;

    private String rejectionReason;
}
