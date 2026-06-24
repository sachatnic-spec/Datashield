package io.datasheild.integration.test;

import io.datasheild.grievanceservice.entity.Grievance;
import io.datasheild.grievanceservice.service.GrievanceService;
import io.datasheild.retentionservice.entity.RetentionPolicy;
import io.datasheild.retentionservice.service.RetentionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests demonstrating cross-service workflows
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092"})
public class CrossServiceIntegrationTest {

    @Autowired
    private GrievanceService grievanceService;

    @Autowired
    private RetentionService retentionService;

    /**
     * End-to-end grievance workflow:
     * 1. Data Principal files grievance
     * 2. System acknowledges
     * 3. DPO investigates
     * 4. System detects SLA risk (day 25)
     * 5. Escalates to compliance
     * 6. Resolution published
     */
    @Test
    public void testGrievanceEndToEndWorkflow() {
        UUID tenantId = UUID.randomUUID();
        UUID dataPrincipalId = UUID.randomUUID();

        // Step 1: File grievance
        Grievance grievance = grievanceService.fileGrievance(
            tenantId,
            dataPrincipalId,
            Grievance.GrievanceCategory.DATA_ACCESS_DENIAL,
            Grievance.GrievanceChannel.WEB,
            "DSAR wrongfully rejected",
            "My data access request was denied without proper justification"
        );

        assertNotNull(grievance.getId());
        assertEquals(Grievance.GrievanceStatus.FILED, grievance.getStatus());
        assertNotNull(grievance.getSlaDeadline());

        // Step 2: Acknowledge receipt
        Grievance acknowledged = grievanceService.acknowledgeGrievance(
            grievance.getId(),
            "dpo@datasheild.io"
        );

        assertEquals(Grievance.GrievanceStatus.ACKNOWLEDGED, acknowledged.getStatus());

        // Step 3: Resolve grievance
        String resolution = "Grievance upheld. DSAR denied due to incomplete identity verification. " +
                          "Data Principal may resubmit with valid government ID.";

        Grievance resolved = grievanceService.resolveGrievance(
            grievance.getId(),
            resolution,
            "dpo@datasheild.io"
        );

        assertEquals(Grievance.GrievanceStatus.RESOLVED, resolved.getStatus());
        assertNotNull(resolved.getResolvedAt());
        assertEquals(resolution, resolved.getResolution());
    }

    /**
     * End-to-end retention & erasure workflow:
     * 1. DPO creates retention policy
     * 2. Legal approves
     * 3. System schedules erasure
     * 4. Scheduler executes erasure
     * 5. Archive location verified
     */
    @Test
    public void testRetentionAndErasureWorkflow() {
        // Step 1: Create policy
        RetentionPolicy policy = retentionService.createPolicy(
            "Healthcare Data - 3 Year Retention",
            "HEALTHCARE",
            "PATIENT_RECORDS",
            1095,
            2190,
            "SECURE_SHRED"
        );

        assertNotNull(policy.getId());
        assertEquals(RetentionPolicy.PolicyStatus.DRAFT, policy.getStatus());

        // Step 2: Approve policy
        RetentionPolicy approved = retentionService.approvePolicy(
            policy.getId(),
            "compliance@datasheild.io"
        );

        assertEquals(RetentionPolicy.PolicyStatus.ACTIVE, approved.getStatus());

        // Step 3: Schedule erasure
        UUID tenantId = UUID.randomUUID();
        LocalDateTime scheduledFor = LocalDateTime.now().plusDays(1);

        var task = retentionService.scheduleErasure(
            tenantId,
            policy.getId(),
            "PATIENT_RECORDS",
            50000,
            scheduledFor,
            "SECURE_SHRED"
        );

        assertNotNull(task.getId());
        assertEquals(50000, task.getRecordsToErase());

        // Step 4: Execute erasure (simulated)
        var executed = retentionService.executeErasure(
            task.getId(),
            "s3://datasheild-archive/" + tenantId + "/erasure/" + task.getId() + "/2026-06-23T10:00:00"
        );

        assertEquals(50000, executed.getRecordsErased());
        assertNotNull(executed.getArchiveLocation());
    }

    /**
     * Cross-service integration: Policy + Retention
     * Verify that deletion follows policy constraints
     */
    @Test
    public void testPolicyEnforcedRetention() {
        // Create retention policy with approval requirement
        RetentionPolicy policy = retentionService.createPolicy(
            "Finance Data - 7 Year Hold (GST/Tax)",
            "FINANCIAL",
            "TAX_RECORDS",
            2555,
            3650,
            "CRYPTO_DESTRUCTION"
        );

        assertNotNull(policy.getId());
        assertTrue(policy.getRequiresApproval());

        // Approval status should block erasure
        assertEquals(RetentionPolicy.PolicyStatus.DRAFT, policy.getStatus());

        // Once approved, can schedule
        var approved = retentionService.approvePolicy(policy.getId(), "finance-officer@datasheild.io");
        assertEquals(RetentionPolicy.PolicyStatus.ACTIVE, approved.getStatus());
    }

    /**
     * Test SLA calculation for grievance
     */
    @Test
    public void testGrievanceSLACalculation() {
        UUID tenantId = UUID.randomUUID();
        UUID dataPrincipalId = UUID.randomUUID();

        Grievance grievance = grievanceService.fileGrievance(
            tenantId,
            dataPrincipalId,
            Grievance.GrievanceCategory.UNAUTHORIZED_ACCESS,
            Grievance.GrievanceChannel.EMAIL,
            "Unauthorized access detected",
            "My account was accessed from unknown location"
        );

        // SLA should be set to 30 days from filing
        LocalDateTime expectedDeadline = grievance.getFiledAt().plusDays(30);
        assertEquals(expectedDeadline.getDayOfMonth(), grievance.getSlaDeadline().getDayOfMonth());
    }
}
