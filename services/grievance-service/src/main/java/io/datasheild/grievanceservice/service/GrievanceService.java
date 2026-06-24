package io.datasheild.grievanceservice.service;

import io.datasheild.grievanceservice.entity.Grievance;
import io.datasheild.grievanceservice.entity.GrievanceActivity;
import io.datasheild.grievanceservice.repository.GrievanceRepository;
import io.datasheild.grievanceservice.repository.GrievanceActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrievanceService {

    private final GrievanceRepository grievanceRepository;
    private final GrievanceActivityRepository activityRepository;

    @Transactional
    public Grievance fileGrievance(UUID tenantId, UUID dataPrincipalId, Grievance.GrievanceCategory category,
                                  Grievance.GrievanceChannel channel, String subject, String description) {
        log.info("Filing grievance for tenant: {}, category: {}", tenantId, category);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime slaDeadline = now.plusDays(30);

        Grievance grievance = Grievance.builder()
            .tenantId(tenantId)
            .dataPrincipalId(dataPrincipalId)
            .category(category)
            .channel(channel)
            .subject(subject)
            .description(description)
            .status(Grievance.GrievanceStatus.FILED)
            .priority("MEDIUM")
            .filedAt(now)
            .slaDeadline(slaDeadline)
            .build();

        grievance = grievanceRepository.save(grievance);

        GrievanceActivity activity = GrievanceActivity.builder()
            .grievanceId(grievance.getId())
            .activityType("GRIEVANCE_FILED")
            .description("Grievance filed via " + channel)
            .statusBefore(null)
            .statusAfter("FILED")
            .performedBy("SYSTEM")
            .build();

        activityRepository.save(activity);
        log.info("Grievance filed: {} (SLA deadline: {})", grievance.getId(), slaDeadline);

        return grievance;
    }

    @Transactional
    public Grievance acknowledgeGrievance(UUID grievanceId, String acknowledgedBy) {
        log.info("Acknowledging grievance: {}", grievanceId);

        Grievance grievance = grievanceRepository.findById(grievanceId)
            .orElseThrow(() -> new RuntimeException("Grievance not found: " + grievanceId));

        GrievanceActivity activity = GrievanceActivity.builder()
            .grievanceId(grievanceId)
            .activityType("ACKNOWLEDGMENT")
            .description("Grievance acknowledged")
            .statusBefore(grievance.getStatus().toString())
            .statusAfter("ACKNOWLEDGED")
            .performedBy(acknowledgedBy)
            .build();

        activityRepository.save(activity);
        grievance.setStatus(Grievance.GrievanceStatus.ACKNOWLEDGED);
        grievance = grievanceRepository.save(grievance);

        log.info("Grievance acknowledged by: {}", acknowledgedBy);
        return grievance;
    }

    @Transactional
    public Grievance resolveGrievance(UUID grievanceId, String resolution, String resolvedBy) {
        log.info("Resolving grievance: {}", grievanceId);

        Grievance grievance = grievanceRepository.findById(grievanceId)
            .orElseThrow(() -> new RuntimeException("Grievance not found: " + grievanceId));

        grievance.setStatus(Grievance.GrievanceStatus.RESOLVED);
        grievance.setResolution(resolution);
        grievance.setResolvedAt(LocalDateTime.now());
        grievance = grievanceRepository.save(grievance);

        GrievanceActivity activity = GrievanceActivity.builder()
            .grievanceId(grievanceId)
            .activityType("RESOLUTION")
            .description("Grievance resolved")
            .statusBefore("INVESTIGATING")
            .statusAfter("RESOLVED")
            .performedBy(resolvedBy)
            .build();

        activityRepository.save(activity);
        log.info("Grievance resolved by: {}", resolvedBy);

        return grievance;
    }

    @Transactional
    public Grievance escalateGrievance(UUID grievanceId, String escalationReason, String escalatedBy) {
        log.info("Escalating grievance: {}", grievanceId);

        Grievance grievance = grievanceRepository.findById(grievanceId)
            .orElseThrow(() -> new RuntimeException("Grievance not found: " + grievanceId));

        grievance.setStatus(Grievance.GrievanceStatus.ESCALATED);
        grievance.setEscalationReason(escalationReason);
        grievance.setPriority("HIGH");
        grievance = grievanceRepository.save(grievance);

        GrievanceActivity activity = GrievanceActivity.builder()
            .grievanceId(grievanceId)
            .activityType("ESCALATION")
            .description("Escalated to DPO: " + escalationReason)
            .statusBefore(grievance.getStatus().toString())
            .statusAfter("ESCALATED")
            .performedBy(escalatedBy)
            .build();

        activityRepository.save(activity);
        log.info("Grievance escalated by: {}", escalatedBy);

        return grievance;
    }

    @Transactional(readOnly = true)
    public Page<Grievance> getGrievancesByTenant(UUID tenantId, Pageable pageable) {
        return grievanceRepository.findByTenantId(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Grievance> getSLABreaches() {
        return grievanceRepository.findSLABreaches(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<GrievanceActivity> getGrievanceHistory(UUID grievanceId) {
        return activityRepository.findActivityByGrievanceId(grievanceId);
    }

    @Transactional(readOnly = true)
    public Long countEscalatedGrievances() {
        return grievanceRepository.countEscalatedGrievances();
    }
}
