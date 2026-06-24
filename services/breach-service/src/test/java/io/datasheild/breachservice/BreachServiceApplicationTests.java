package io.datasheild.breachservice;

import io.datasheild.breachservice.dto.ReportBreachRequest;
import io.datasheild.breachservice.entity.BreachIncident;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class BreachServiceApplicationTests {

    @Test
    public void testReportBreachValidation() {
        ReportBreachRequest request = ReportBreachRequest.builder()
                .incidentTitle("Data breach in user database")
                .estimatedDataSubjects(5000)
                .build();

        assertTrue(request.isValid());
    }

    @Test
    public void testReportBreachInvalidation() {
        ReportBreachRequest request = ReportBreachRequest.builder()
                .incidentTitle(null)
                .estimatedDataSubjects(100)
                .build();

        assertFalse(request.isValid());
    }

    @Test
    public void testBreachIncidentSLA() {
        BreachIncident incident = BreachIncident.builder()
                .incidentTitle("Test breach")
                .discoveredAt(java.time.LocalDateTime.now())
                .build();

        assertNotNull(incident.getDpbiDeadline());
        assertEquals(BreachIncident.BreachStatus.REPORTED, incident.getStatus());
    }
}
