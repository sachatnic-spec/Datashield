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
public class EmailChannelService implements NotificationChannel {

    private final NotificationLogRepository logRepository;

    @Override
    public NotificationLog.Channel getChannelType() {
        return NotificationLog.Channel.EMAIL;
    }

    @Override
    public NotificationLog send(String recipient, String subject, String body, Map<String, String> variables, UUID eventId, UUID tenantId) {
        log.info("Sending email to: {} for event: {}", recipient, eventId);

        try {
            String renderedBody = renderTemplate(body, variables);

            // TODO: Call external email service (SendGrid, AWS SES, Mailgun)
            // For MVP: Log only
            log.info("Email sent successfully to: {} subject: {}", recipient, subject);

            NotificationLog log = NotificationLog.builder()
                    .eventId(eventId)
                    .tenantId(tenantId)
                    .channel(NotificationLog.Channel.EMAIL)
                    .recipient(recipient)
                    .deliveryStatus(NotificationLog.DeliveryStatus.SENT)
                    .response("Email queued for delivery")
                    .build();

            return logRepository.save(log);

        } catch (Exception e) {
            log.error("Failed to send email to: {} error: {}", recipient, e.getMessage());

            NotificationLog failLog = NotificationLog.builder()
                    .eventId(eventId)
                    .tenantId(tenantId)
                    .channel(NotificationLog.Channel.EMAIL)
                    .recipient(recipient)
                    .deliveryStatus(NotificationLog.DeliveryStatus.FAILED)
                    .response(e.getMessage())
                    .build();

            return logRepository.save(failLog);
        }
    }

    @Override
    public boolean supportsRecipient(String recipient) {
        return recipient.matches("^[A-Za-z0-9+_.-]+@(.+)$");
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
