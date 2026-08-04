package com.datasheild.dpbi.service;

import com.datasheild.dpbi.entity.BreachNotification;
import com.datasheild.dpbi.repository.BreachNotificationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KafkaBreachConsumer {

    private final BreachNotificationRepository repository;
    private final FormGenerationService formGenerationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "breach.incident.created", groupId = "dpbi-breach-group")
    public void onBreachCreated(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            BreachNotification notification = repository.save(BreachNotification.builder()
                    .tenantId(node.path("tenantId").asText("default-tenant"))
                    .breachId(node.path("breachId").asLong())
                    .discoveryDate(LocalDate.now())
                    .notificationDueDate(LocalDate.now().plusDays(3))
                    .status("DRAFT")
                    .build());
            formGenerationService.generateFromBreach(notification);
        } catch (Exception ex) {
            log.error("Failed to process breach event", ex);
        }
    }
}
