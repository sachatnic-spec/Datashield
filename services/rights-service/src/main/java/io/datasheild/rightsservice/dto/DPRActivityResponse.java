package io.datasheild.rightsservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.datasheild.rightsservice.entity.DPRActivity;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DPRActivityResponse {

    private UUID id;

    private DPRActivity.ActivityType activityType;

    private String activityDescription;

    private String actor;

    private LocalDateTime createdAt;

    private String auditTraceId;
}
