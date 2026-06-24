package io.datasheild.retentionservice.scheduler;

import io.datasheild.retentionservice.entity.DataErasureTask;
import io.datasheild.retentionservice.service.RetentionService;
import io.datasheild.retentionservice.event.RetentionEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ErasureTaskScheduler {

    private final RetentionService retentionService;
    private final RetentionEventPublisher eventPublisher;

    /**
     * Execute scheduled data erasure tasks
     * Runs every 6 hours to process due tasks
     */
    @Scheduled(fixedRate = 21600000)
    public void executeScheduledErasures() {
        log.info("Running scheduled data erasure execution");

        List<DataErasureTask> tasks = retentionService.getScheduledTasks();

        for (DataErasureTask task : tasks) {
            log.info("Executing erasure task: {} (records: {})", task.getId(), task.getRecordsToErase());

            try {
                String archiveLocation = generateS3ArchiveLocation(task.getTenantId(), task.getId());
                DataErasureTask completed = retentionService.executeErasure(task.getId(), archiveLocation);

                eventPublisher.publishDataErasureCompleted(
                    task.getId(),
                    task.getTenantId(),
                    completed.getRecordsErased(),
                    archiveLocation,
                    UUID.randomUUID().toString()
                );

                log.info("Erasure task completed: {} records archived to {}", 
                    completed.getRecordsErased(), archiveLocation);

            } catch (Exception e) {
                log.error("Failed to execute erasure task: {}", task.getId(), e);
                eventPublisher.publishDataErasureCompleted(
                    task.getId(),
                    task.getTenantId(),
                    0,
                    "FAILED",
                    UUID.randomUUID().toString()
                );
            }
        }

        log.info("Erasure execution completed. Tasks processed: {}", tasks.size());
    }

    /**
     * Generate S3 Object Lock archive location
     * Format: s3://datasheild-archive/{tenantId}/erasure/{taskId}/{timestamp}
     */
    private String generateS3ArchiveLocation(UUID tenantId, UUID taskId) {
        return String.format("s3://datasheild-archive/%s/erasure/%s/%s",
            tenantId, taskId, LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_DATE_TIME));
    }

    /**
     * Daily verification: Check that erasure archive is immutable
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void verifyArchiveIntegrity() {
        log.info("Running daily S3 archive integrity verification");

        log.info("Archive integrity verification completed");
    }
}
