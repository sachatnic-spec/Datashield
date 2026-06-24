package io.datasheild.rightsservice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.datasheild.rightsservice.repository.DPROutboxRepository;
import io.datasheild.rightsservice.entity.DPROutbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final DPROutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1000, initialDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<DPROutbox> unpublished = outboxRepository.findUnpublishedEventsBatch();

        for (DPROutbox event : unpublished) {
            try {
                String topic = "dpr-events";
                kafkaTemplate.send(topic, event.getId().toString(), event.getEventPayload());

                event.setPublished(true);
                event.setPublishedAt(LocalDateTime.now());
                outboxRepository.save(event);

                log.info("Event published to Kafka: eventId={} eventType={}", event.getId(), event.getEventType());

            } catch (Exception e) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(e.getMessage());

                if (event.getRetryCount() >= 5) {
                    log.error("Event exhausted retries: eventId={} error={}", event.getId(), e.getMessage());
                } else {
                    log.warn("Retrying event: eventId={} retry={}", event.getId(), event.getRetryCount());
                }

                outboxRepository.save(event);
            }
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void archivePublishedEvents() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<DPROutbox> oldEvents = outboxRepository.findPublishedEventsBefore(sevenDaysAgo);

        log.info("Archiving {} old published events", oldEvents.size());
        outboxRepository.deleteAll(oldEvents);
    }
}
