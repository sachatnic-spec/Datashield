package io.datasheild.breachservice.service;

import io.datasheild.breachservice.dto.BreachIncidentResponse;
import io.datasheild.breachservice.dto.ReportBreachRequest;
import io.datasheild.breachservice.dto.SeverityScoreResponse;
import io.datasheild.breachservice.entity.*;
import io.datasheild.breachservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BreachService {

    private final BreachIncidentRepository breachIncidentRepository;
    private final ContainmentActionRepository actionRepository;
    private final SeverityScoreRepository scoreRepository;
    private final BreachOutboxRepository outboxRepository;
    private final SeverityScoringService scoringService;

    @Transactional
    public BreachIncidentResponse reportBreach(UUID tenantId, ReportBreachRequest request) {
        log.info("Reporting breach incident: tenant={}", tenantId);

        LocalDateTime discoveredAt = LocalDateTime.now();

        BreachIncident incident = BreachIncident.builder()
                .tenantId(tenantId)
                .status(BreachIncident.BreachStatus.REPORTED)
                .incidentTitle(request.getIncidentTitle())
                .incidentDescription(request.getIncidentDescription())
                .affectedSystems(request.getAffectedSystems())
                .estimatedDataSubjects(request.getEstimatedDataSubjects())
                .estimatedRecords(request.getEstimatedRecords())
                .dataCategories(request.getDataCategories())
                .discoveredAt(discoveredAt)
                .reportedAt(LocalDateTime.now())
                .dpbiDeadline(discoveredAt.plusHours(72))
                .lossOfConfidentiality(request.getLossOfConfidentiality())
                .lossOfIntegrity(request.getLossOfIntegrity())
                .lossOfAvailability(request.getLossOfAvailability())
                .build();

        BreachIncident saved = breachIncidentRepository.save(incident);

        // Calculate severity
        SeverityScore score = scoringService.calculateSeverity(tenantId, saved);
        scoreRepository.save(score);

        // Initial severity assignment
        saved.setSeverity(score.getProposedSeverity());
        breachIncidentRepository.save(saved);

        // Publish event
        publishEvent(tenantId, "breach.reported", buildEventPayload(saved));

        log.info("Breach incident reported: id={} severity={}", saved.getId(), score.getProposedSeverity());
        return mapToResponse(saved);
    }

    @Transactional
    public BreachIncidentResponse containBreach(UUID tenantId, UUID incidentId, String containmentStrategy) {
        log.info("Containing breach incident: id={}", incidentId);

        BreachIncident incident = breachIncidentRepository.findByTenantAndId(tenantId, incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));

        incident.setStatus(BreachIncident.BreachStatus.CONTAINED);
        incident.setContainedAt(LocalDateTime.now());
        incident.setContainmentStrategy(containmentStrategy);

        BreachIncident updated = breachIncidentRepository.save(incident);

        // Publish event
        publishEvent(tenantId, "breach.contained", buildEventPayload(updated));

        log.info("Breach contained: id={}", updated.getId());
        return mapToResponse(updated);
    }

    @Transactional
    public BreachIncidentResponse notifyDPBI(UUID tenantId, UUID incidentId) {
        log.info("Notifying DPBI for incident: id={}", incidentId);

        BreachIncident incident = breachIncidentRepository.findByTenantAndId(tenantId, incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));

        incident.setStatus(BreachIncident.BreachStatus.NOTIFIED_DPBI);
        incident.setDpbiNotifiedAt(LocalDateTime.now());

        BreachIncident updated = breachIncidentRepository.save(incident);

        // Publish event
        publishEvent(tenantId, "breach.notified_dpbi", buildEventPayload(updated));

        log.info("DPBI notified: id={}", updated.getId());
        return mapToResponse(updated);
    }

    public List<BreachIncidentResponse> getIncidents(UUID tenantId) {
        log.debug("Fetching incidents for tenant={}", tenantId);

        List<BreachIncident> incidents = breachIncidentRepository.findActiveByTenant(tenantId);
        return incidents.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BreachIncidentResponse getIncident(UUID tenantId, UUID incidentId) {
        log.debug("Fetching incident: id={}", incidentId);

        BreachIncident incident = breachIncidentRepository.findByTenantAndId(tenantId, incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));

        return mapToResponse(incident);
    }

    public SeverityScoreResponse getSeverityScore(UUID tenantId, UUID incidentId) {
        log.debug("Fetching severity score for incident: id={}", incidentId);

        SeverityScore score = scoreRepository.findLatestByIncident(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Severity score not found"));

        return mapScoreToResponse(score);
    }

    @Transactional
    public void checkSLAViolations(UUID tenantId) {
        log.info("Checking SLA violations for tenant={}", tenantId);

        List<BreachIncident> violations = breachIncidentRepository.findSLAViolatingIncidents(tenantId, LocalDateTime.now());
        log.warn("Found {} SLA violations", violations.size());

        for (BreachIncident incident : violations) {
            log.warn("SLA violation for breach incident: id={} deadline={}", incident.getId(), incident.getDpbiDeadline());
        }
    }

    @Transactional
    public void addContainmentAction(UUID tenantId, UUID incidentId, String actionTitle, String description) {
        log.info("Adding containment action for incident: id={}", incidentId);

        BreachIncident incident = breachIncidentRepository.findByTenantAndId(tenantId, incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));

        ContainmentAction action = ContainmentAction.builder()
                .tenantId(tenantId)
                .breachIncidentId(incidentId)
                .actionTitle(actionTitle)
                .actionDescription(description)
                .status(ContainmentAction.ActionStatus.PLANNED)
                .build();

        actionRepository.save(action);
        log.info("Containment action added: id={}", action.getId());
    }

    private void publishEvent(UUID tenantId, String eventType, String payload) {
        BreachOutbox outbox = BreachOutbox.builder()
                .tenantId(tenantId)
                .eventType(eventType)
                .eventPayload(payload)
                .published(false)
                .retryCount(0)
                .build();

        outboxRepository.save(outbox);
        log.debug("Event published to outbox: eventType={}", eventType);
    }

    private String buildEventPayload(BreachIncident incident) {
        return "{\"incidentId\":\"" + incident.getId() + "\",\"severity\":\"" + incident.getSeverity() + 
               "\",\"status\":\"" + incident.getStatus() + "\"}";
    }

    private BreachIncidentResponse mapToResponse(BreachIncident incident) {
        return BreachIncidentResponse.builder()
                .id(incident.getId())
                .status(incident.getStatus())
                .severity(incident.getSeverity())
                .incidentTitle(incident.getIncidentTitle())
                .discoveredAt(incident.getDiscoveredAt())
                .reportedAt(incident.getReportedAt())
                .dpbiDeadline(incident.getDpbiDeadline())
                .dpbiNotifiedAt(incident.getDpbiNotifiedAt())
                .dpNotifiedAt(incident.getDpNotifiedAt())
                .containedAt(incident.getContainedAt())
                .estimatedDataSubjects(incident.getEstimatedDataSubjects())
                .lossOfConfidentiality(incident.getLossOfConfidentiality())
                .lossOfIntegrity(incident.getLossOfIntegrity())
                .lossOfAvailability(incident.getLossOfAvailability())
                .build();
    }

    private SeverityScoreResponse mapScoreToResponse(SeverityScore score) {
        return SeverityScoreResponse.builder()
                .id(score.getId())
                .baseScore(score.getBaseScore())
                .adjustedScore(score.getAdjustedScore())
                .proposedSeverity(score.getProposedSeverity())
                .scoringRationale(score.getScoringRationale())
                .calculatedAt(score.getCalculatedAt())
                .reviewedAt(score.getReviewedAt())
                .reviewedBy(score.getReviewedBy())
                .build();
    }
}
