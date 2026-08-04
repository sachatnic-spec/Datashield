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
public class SMSChannelService implements NotificationChannel {

    private final NotificationLogRepository logRepository;

    @Override
    public NotificationLog.Channel getChannelType() {
        return NotificationLog.Channel.SMS;
    }

    @Override
    public NotificationLog send(String recipient, String subject, String body, Map<String, String> variables, UUID eventId, UUID tenantId) {
        log.info("Sending SMS to: {} for event: {}", recipient, eventId);

        try {
            String renderedBody = renderTemplate(body, variables);

            // Truncate to 160 chars for SMS
            String smsBody = renderedBody.length() > 160 ? renderedBody.substring(0, 157) + "..." : renderedBody;

            // TODO: Call SMS provider (Twilio, AWS SNS, Telcel)
            // For MVP: Log only
            log.info("SMS sent successfully to: {}", recipient);

            NotificationLog log = NotificationLog.builder()
                    .eventId(eventId)
                    .tenantId(tenantId)
                    .channel(NotificationLog.Channel.SMS)
                    .recipient(recipient)
                    .deliveryStatus(NotificationLog.DeliveryStatus.SENT)
                    .response("SMS queued: " + smsBody)
                    .build();

            return logRepository.save(log);

        } catch (Exception e) {
            log.error("Failed to send SMS to: {} error: {}", recipient, e.getMessage());

            NotificationLog failLog = NotificationLog.builder()
                    .eventId(eventId)
                    .tenantId(tenantId)
                    .channel(NotificationLog.Channel.SMS)
                    .recipient(recipient)
                    .deliveryStatus(NotificationLog.DeliveryStatus.FAILED)
                    .response(e.getMessage())
                    .build();

            return logRepository.save(failLog);
        }
    }

    @Override
    public boolean supportsRecipient(String recipient) {
        return recipient.matches("^\\+?[1-9]\\d{1,14}$");
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
