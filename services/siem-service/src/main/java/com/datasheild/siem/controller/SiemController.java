package com.datasheild.siem.controller;

import com.datasheild.siem.dto.SiemAlertRequest;
import com.datasheild.siem.entity.SiemAlert;
import com.datasheild.siem.repository.SiemAlertRepository;
import com.datasheild.siem.service.AzureSentinelConnectorService;
import com.datasheild.siem.service.IncidentAutoCreationService;
import com.datasheild.siem.service.QRadarConnectorService;
import com.datasheild.siem.service.SplunkConnectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/siem")
@RequiredArgsConstructor
public class SiemController {

    private final SiemAlertRepository alertRepository;
    private final SplunkConnectorService splunkConnectorService;
    private final QRadarConnectorService qradarConnectorService;
    private final AzureSentinelConnectorService sentinelConnectorService;
    private final IncidentAutoCreationService incidentAutoCreationService;

    @PostMapping("/alerts")
    public ResponseEntity<SiemAlert> createAlert(@Valid @RequestBody SiemAlertRequest request) {
        SiemAlert alert = alertRepository.save(SiemAlert.builder()
                .tenantId(request.getTenantId())
                .alertType(request.getAlertType())
                .severity(request.getSeverity())
                .sourceSystem(request.getSourceSystem())
                .externalIncidentId(request.getExternalIncidentId())
                .message(request.getMessage())
                .anomalyScore(request.getAnomalyScore())
                .status("NEW")
                .build());
        splunkConnectorService.postEvent(alert);
        qradarConnectorService.postEvent(alert);
        sentinelConnectorService.postEvent(alert);
        incidentAutoCreationService.createAutoIncident(alert);
        return ResponseEntity.accepted().body(alert);
    }

    @GetMapping("/alerts")
    public ResponseEntity<Page<SiemAlert>> listAlerts(@RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(alertRepository.findByTenantId(tenantId, PageRequest.of(page, size)));
    }

    @PostMapping("/alerts/{id}/replay")
    public ResponseEntity<SiemAlert> replayAlert(@PathVariable Long id) {
        SiemAlert alert = alertRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Alert not found"));
        splunkConnectorService.postEvent(alert);
        qradarConnectorService.postEvent(alert);
        sentinelConnectorService.postEvent(alert);
        return ResponseEntity.accepted().body(alert);
    }
}
