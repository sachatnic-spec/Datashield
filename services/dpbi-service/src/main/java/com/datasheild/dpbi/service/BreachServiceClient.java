package com.datasheild.dpbi.service;

import lombok.Builder;
import org.springframework.stereotype.Service;

@Service
public class BreachServiceClient {

    public BreachDetail getBreachDetails(Long breachId) {
        return BreachDetail.builder()
                .breachId(breachId)
                .description("Breach incident #" + breachId)
                .impactedCount(250)
                .dataTypesAffected("[\"PII\",\"Financial\"]")
                .impactAssessment("Potential exposure of regulated personal data.")
                .remediationPlan("Isolate affected systems, rotate credentials, notify impacted principals.")
                .build();
    }

    @Builder
    public record BreachDetail(
            Long breachId,
            String description,
            Integer impactedCount,
            String dataTypesAffected,
            String impactAssessment,
            String remediationPlan
    ) {
    }
}
