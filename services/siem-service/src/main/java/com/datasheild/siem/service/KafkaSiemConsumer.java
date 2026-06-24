package com.datasheild.siem.service;

import com.datasheild.siem.entity.SiemAlert;
import com.datasheild.siem.repository.SiemAlertRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KafkaSiemConsumer {

    private final SiemAlertRepository alertRepository;
    private final SplunkConnectorService splunkService;
    private final QRadarConnectorService qradarService;
    private final AzureSentinelConnectorService sentinelService;
    private final IncidentAutoCreationService incidentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"anomaly.detected", "breach.incident.created", "audit.entry.created"}, groupId = "siem-group")
    public void onPlatformEvent(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        SiemAlert alert = parseMessage(message, topic);
        alert = alertRepository.save(alert);
        splunkService.postEvent(alert);
        qradarService.postEvent(alert);
        sentinelService.postEvent(alert);
        incidentService.createAutoIncident(alert);
    }

    SiemAlert parseMessage(String message, String topic) {
        try {
            JsonNode node = objectMapper.readTree(message);
            Double anomalyScore = node.has("anomalyScore") ? node.get("anomalyScore").asDouble() : null;
            String severity = determineSeverity(topic, anomalyScore);
            return SiemAlert.builder()
                    .tenantId(node.path("tenantId").asText("default-tenant"))
                    .alertType(topic)
                    .severity(severity)
                    .sourceSystem(sourceSystemFor(topic))
                    .message(node.path("message").asText(message))
                    .externalIncidentId(node.path("incidentId").asText(null))
                    .anomalyScore(anomalyScore)
                    .status("NEW")
                    .build();
        } catch (Exception ex) {
            log.warn("Failed to parse event payload, falling back to raw message", ex);
            return SiemAlert.builder()
                    .tenantId("default-tenant")
                    .alertType(topic)
                    .severity(determineSeverity(topic, null))
                    .sourceSystem(sourceSystemFor(topic))
                    .message(message)
                    .status("NEW")
                    .build();
        }
    }

    private String determineSeverity(String topic, Double anomalyScore) {
        if ("anomaly.detected".equals(topic)) {
            return anomalyScore != null && anomalyScore > 0.9 ? "CRITICAL" : "HIGH";
        }
        if ("breach.incident.created".equals(topic)) {
            return "CRITICAL";
        }
        return "MEDIUM";
    }

    private String sourceSystemFor(String topic) {
        return switch (topic) {
            case "anomaly.detected" -> "SPLUNK";
            case "breach.incident.created" -> "QRADAR";
            default -> "SENTINEL";
        };
    }
}
