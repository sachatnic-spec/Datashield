package io.datasheild.notificationservice.channel;

import io.datasheild.notificationservice.entity.NotificationLog;
import java.util.UUID;

public interface NotificationChannel {

    NotificationLog.Channel getChannelType();

    /**
     * Send notification via this channel
     * @param recipient Email/phone/webhook URL
     * @param subject Subject line (email) or title (SMS)
     * @param body Message body/content
     * @param variables Template variables for rendering
     * @return NotificationLog with delivery status
     */
    NotificationLog send(String recipient, String subject, String body, java.util.Map<String, String> variables, UUID eventId, UUID tenantId);

    /**
     * Check if channel supports this recipient format
     */
    boolean supportsRecipient(String recipient);
}
