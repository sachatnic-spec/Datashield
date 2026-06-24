package io.datasheild.auditservice;

import io.datasheild.auditservice.entity.AuditEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AuditServiceApplicationTests {

    @Test
    public void testAuditEventValidation() {
        AuditEvent event = AuditEvent.builder()
                .tenantId(UUID.randomUUID())
                .correlationId("corr-001")
                .sourceService("consent-service")
                .entityType("ConsentRecord")
                .eventType("CONSENT_GRANTED")
                .entityId(UUID.randomUUID())
                .eventPayload("{\"data\": \"test\"}")
                .actorId("user-001")
                .build();

        assertTrue(event.isValid());
    }

    @Test
    public void testAuditEventInvalidation() {
        AuditEvent event = AuditEvent.builder()
                .tenantId(null)
                .correlationId("corr-001")
                .sourceService("consent-service")
                .entityType("ConsentRecord")
                .eventType("CONSENT_GRANTED")
                .entityId(UUID.randomUUID())
                .actorId("user-001")
                .build();

        assertFalse(event.isValid());
    }

    @Test
    public void testHashGeneration() {
        String input = "test-payload";
        String hash1 = generateHash(input);
        String hash2 = generateHash(input);

        assertEquals(hash1, hash2, "Hash should be deterministic");
        assertEquals(64, hash1.length(), "SHA-256 hash should be 64 hex characters");
    }

    private String generateHash(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
