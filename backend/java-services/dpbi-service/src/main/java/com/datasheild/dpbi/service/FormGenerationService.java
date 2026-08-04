package com.datasheild.dpbi.service;

import com.datasheild.dpbi.entity.BreachNotification;
import com.datasheild.dpbi.entity.DpbiForm;
import com.datasheild.dpbi.repository.DpbiFormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class FormGenerationService {

    private final DpbiFormRepository repository;
    private final BreachServiceClient breachServiceClient;

    public DpbiForm generateFromBreach(BreachNotification notification) {
        BreachServiceClient.BreachDetail breach = breachServiceClient.getBreachDetails(notification.getBreachId());
        DpbiForm form = DpbiForm.builder()
                .breachNotificationId(notification.getId())
                .incidentSummary(breach.description())
                .impactAssessment(breach.impactAssessment())
                .remediationPlan(breach.remediationPlan())
                .affectedDataSubjects(breach.impactedCount())
                .dataCategories(breach.dataTypesAffected())
                .generatedAt(LocalDateTime.now())
                .lastUpdatedAt(LocalDateTime.now())
                .build();
        return repository.save(form);
    }
}
