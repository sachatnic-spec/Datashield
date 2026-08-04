package io.datasheild.notificationservice.channel;

import io.datasheild.notificationservice.entity.NotificationLog;
import io.datasheild.notificationservice.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InAppChannelService implements NotificationChannel {

    private final NotificationLogRepository logRepository;

    @Override
    public NotificationLog.Channel getChannelType() {
        return NotificationLog.Channel.INAPP;
    }

    @Override
    public NotificationLog send(String recipient, String subject, String body, Map<String, String> variables, UUID eventId, UUID tenantId) {
        log.info("Creating in-app notification for: {} for event: {}", recipient, eventId);

        try {
            String renderedBody = renderTemplate(body, variables);

            // In-app notifications stored directly (no external call needed)
            log.info("In-app notification created for: {}", recipient);

            NotificationLog log = NotificationLog.builder()
                    .eventId(eventId)
                    .tenantId(tenantId)
                    .channel(NotificationLog.Channel.INAPP)
                    .recipient(recipient)
                    .deliveryStatus(NotificationLog.DeliveryStatus.DELIVERED)
                    .response("Notification stored in-app: " + subject)
                    .build();

            return logRepository.save(log);

        } catch (Exception e) {
            log.error("Failed to create in-app notification for: {} error: {}", recipient, e.getMessage());

            NotificationLog failLog = NotificationLog.builder()
                    .eventId(eventId)
                    .tenantId(tenantId)
                    .channel(NotificationLog.Channel.INAPP)
                    .recipient(recipient)
                    .deliveryStatus(NotificationLog.DeliveryStatus.FAILED)
                    .response(e.getMessage())
                    .build();

            return logRepository.save(failLog);
        }
    }

    @Override
    public boolean supportsRecipient(String recipient) {
        return true; // In-app supports any recipient
    }

    private String renderTemplate(String template, Map<String, String> variables) {
        String rendered = template;
        if (variables != null) {
            for (Map.Entry<String, String> var : variables.entrySet()) {
                rendered = rendered.replace("{{" + var.getKey() + "}}", var.getValue());
            }
        }
        return rendered;
    }
}
