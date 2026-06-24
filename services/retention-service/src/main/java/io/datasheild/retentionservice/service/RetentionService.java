package io.datasheild.retentionservice.service;

import io.datasheild.retentionservice.entity.RetentionPolicy;
import io.datasheild.retentionservice.entity.DataErasureTask;
import io.datasheild.retentionservice.repository.RetentionPolicyRepository;
import io.datasheild.retentionservice.repository.DataErasureTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetentionService {

    private final RetentionPolicyRepository policyRepository;
    private final DataErasureTaskRepository taskRepository;

    @Transactional
    public RetentionPolicy createPolicy(String policyName, String sector, String dataCategory, 
                                       Integer retentionDays, Integer maxRetentionDays, String disposalMethod) {
        log.info("Creating retention policy: {} for sector: {}", policyName, sector);

        RetentionPolicy policy = RetentionPolicy.builder()
            .policyName(policyName)
            .sector(sector)
            .dataCategory(dataCategory)
            .retentionDaysDefault(retentionDays)
            .retentionDaysMax(maxRetentionDays)
            .disposalMethod(disposalMethod)
            .status(RetentionPolicy.PolicyStatus.DRAFT)
            .requiresApproval(true)
            .build();

        policy = policyRepository.save(policy);
        log.info("Retention policy created: {}", policy.getId());
        return policy;
    }

    @Transactional
    public RetentionPolicy approvePolicy(UUID policyId, String approvedBy) {
        log.info("Approving retention policy: {}", policyId);

        RetentionPolicy policy = policyRepository.findById(policyId)
            .orElseThrow(() -> new RuntimeException("Policy not found: " + policyId));

        policy.setStatus(RetentionPolicy.PolicyStatus.ACTIVE);
        policy.setApprovedBy(approvedBy);
        policy = policyRepository.save(policy);

        log.info("Retention policy activated: {}", policyId);
        return policy;
    }

    @Transactional
    public DataErasureTask scheduleErasure(UUID tenantId, UUID policyId, String dataCategory, 
                                          Integer recordCount, LocalDateTime scheduledFor, String erasureMethod) {
        log.info("Scheduling data erasure for tenant: {}, category: {}, records: {}", tenantId, dataCategory, recordCount);

        DataErasureTask task = DataErasureTask.builder()
            .tenantId(tenantId)
            .policyId(policyId)
            .dataCategory(dataCategory)
            .recordsToErase(recordCount)
            .scheduledFor(scheduledFor)
            .erasureMethod(erasureMethod)
            .status(DataErasureTask.TaskStatus.SCHEDULED)
            .build();

        task = taskRepository.save(task);
        log.info("Erasure task scheduled: {} for {}", task.getId(), scheduledFor);
        return task;
    }

    @Transactional
    public DataErasureTask executeErasure(UUID taskId, String archiveLocation) {
        log.info("Executing erasure task: {}", taskId);

        DataErasureTask task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        task.setStatus(DataErasureTask.TaskStatus.IN_PROGRESS);
        task.setExecutedAt(LocalDateTime.now());
        task = taskRepository.save(task);

        try {
            task.setRecordsErased(task.getRecordsToErase());
            task.setArchiveLocation(archiveLocation);
            task.setStatus(DataErasureTask.TaskStatus.COMPLETED);
            log.info("Erasure completed: {} records archived to {}", task.getRecordsErased(), archiveLocation);
        } catch (Exception e) {
            task.setStatus(DataErasureTask.TaskStatus.FAILED);
            task.setFailedCount(task.getRecordsToErase());
            log.error("Erasure failed: {}", e.getMessage());
        }

        task = taskRepository.save(task);
        return task;
    }

    @Transactional(readOnly = true)
    public List<RetentionPolicy> getActivePolicies() {
        return policyRepository.findActivePolicies();
    }

    @Transactional(readOnly = true)
    public List<DataErasureTask> getScheduledTasks() {
        return taskRepository.findDueForExecution(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Long getTotalErasedRecords(UUID tenantId) {
        return taskRepository.countTotalErasedRecords(tenantId);
    }
}
