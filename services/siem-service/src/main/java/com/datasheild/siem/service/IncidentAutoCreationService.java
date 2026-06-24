package com.datasheild.siem.service;

import com.datasheild.siem.config.SiemProperties;
import com.datasheild.siem.entity.IncidentAutoCreation;
import com.datasheild.siem.entity.SiemAlert;
import com.datasheild.siem.repository.IncidentAutoCreationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class IncidentAutoCreationService {

    private final IncidentAutoCreationRepository repository;
    private final SiemProperties properties;

    public IncidentAutoCreation createAutoIncident(SiemAlert alert) {
        if (!shouldAutoCreate(alert.getSeverity())) {
            return null;
        }
        return repository.save(IncidentAutoCreation.builder()
                .alertId(alert.getId())
                .incidentId("INC-" + alert.getId())
                .status("CREATED")
                .notes("Auto-created from alert severity " + alert.getSeverity())
                .build());
    }

    boolean shouldAutoCreate(String severity) {
        return rank(severity) >= rank(properties.getAutoCreateThreshold());
    }

    private int rank(String severity) {
        return switch (severity) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            default -> 1;
        };
    }
}
