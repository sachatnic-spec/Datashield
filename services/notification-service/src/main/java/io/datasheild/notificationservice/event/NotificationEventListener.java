package io.datasheild.notificationservice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.datasheild.notificationservice.dto.TriggerNotificationRequest;
import io.datasheild.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "consent-events",
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleConsentEvents(String message) {
        log.info("Received consent event: {}", message);
        try {
            // TODO: Parse event and trigger notification
            // For MVP: Log only
        } catch (Exception e) {
            log.error("Error processing consent event: {}", e.getMessage());
        }
    }

    @KafkaListener(
        topics = "dpr-events",
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDPREvents(String message) {
        log.info("Received DPR event: {}", message);
        try {
            // TODO: Parse event and trigger notification
            // For MVP: Log only
        } catch (Exception e) {
            log.error("Error processing DPR event: {}", e.getMessage());
        }
    }

    @KafkaListener(
        topics = "breach-events",
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleBreachEvents(String message) {
        log.info("Received breach event: {}", message);
        try {
            // TODO: Parse event and trigger notification
            // For MVP: Log only
        } catch (Exception e) {
            log.error("Error processing breach event: {}", e.getMessage());
        }
    }
}
