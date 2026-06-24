package com.datasheild.webhook.service;

import com.datasheild.webhook.entity.WebhookEndpoint;
import com.datasheild.webhook.entity.WebhookEvent;
import com.datasheild.webhook.repository.WebhookEndpointRepository;
import com.datasheild.webhook.repository.WebhookEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KafkaWebhookConsumer {

    private final WebhookEndpointRepository endpointRepository;
    private final WebhookEventRepository eventRepository;
    private final WebhookDeliveryService deliveryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"connector.synced", "breach.incident.created", "anomaly.detected", "audit.entry.created"}, groupId = "webhook-platform-events")
    public void onPlatformEvent(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        String tenantId = extractTenantId(message);
        List<WebhookEndpoint> endpoints = endpointRepository.findByTenantIdAndIsActiveTrue(tenantId);
        endpoints.stream()
                .filter(endpoint -> isSubscribed(endpoint, topic))
                .map(endpoint -> eventRepository.save(WebhookEvent.builder()
                        .tenantId(tenantId)
                        .endpointId(endpoint.getId())
                        .eventType(topic)
                        .payload(message)
                        .status("PENDING")
                        .build()))
                .forEach(deliveryService::deliver);
    }

    private String extractTenantId(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            return node.path("tenantId").asText("default-tenant");
        } catch (Exception ex) {
            log.debug("Falling back to default tenant for payload parsing failure");
            return "default-tenant";
        }
    }

    private boolean isSubscribed(WebhookEndpoint endpoint, String topic) {
        return endpoint.getEventsSubscribed() != null && endpoint.getEventsSubscribed().contains(topic);
    }
}
