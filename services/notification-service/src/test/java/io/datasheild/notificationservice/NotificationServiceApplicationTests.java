package io.datasheild.notificationservice;

import io.datasheild.notificationservice.dto.TriggerNotificationRequest;
import io.datasheild.notificationservice.entity.NotificationTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class NotificationServiceApplicationTests {

    @Test
    public void testTriggerNotificationValidation() {
        TriggerNotificationRequest request = TriggerNotificationRequest.builder()
                .correlationId("corr-001")
                .eventType("CONSENT_GRANTED")
                .recipients(java.util.List.of("user@example.com"))
                .templateCode("consent_granted")
                .language("en")
                .channels(java.util.List.of("EMAIL", "INAPP"))
                .build();

        assertTrue(request.isValid());
    }

    @Test
    public void testTriggerNotificationInvalidation() {
        TriggerNotificationRequest request = TriggerNotificationRequest.builder()
                .correlationId(null)
                .eventType("CONSENT_GRANTED")
                .recipients(java.util.List.of("user@example.com"))
                .templateCode("consent_granted")
                .channels(java.util.List.of("EMAIL"))
                .build();

        assertFalse(request.isValid());
    }

    @Test
    public void testEmailChannelValidation() {
        String validEmail = "test@example.com";
        String invalidEmail = "not-an-email";

        assertTrue(validEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$"));
        assertFalse(invalidEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$"));
    }
}
