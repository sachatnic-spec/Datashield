package io.datasheild.retentionservice.repository;

import io.datasheild.retentionservice.entity.DataErasureTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DataErasureTaskRepository extends JpaRepository<DataErasureTask, UUID> {

    @Query("SELECT d FROM DataErasureTask d WHERE d.status = io.datasheild.retentionservice.entity.DataErasureTask$TaskStatus.SCHEDULED " +
           "AND d.scheduledFor <= :now ORDER BY d.scheduledFor ASC")
    List<DataErasureTask> findDueForExecution(@Param("now") LocalDateTime now);

    @Query("SELECT d FROM DataErasureTask d WHERE d.tenantId = :tenantId ORDER BY d.createdAt DESC")
    List<DataErasureTask> findByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT d FROM DataErasureTask d WHERE d.status = io.datasheild.retentionservice.entity.DataErasureTask$TaskStatus.COMPLETED ORDER BY d.executedAt DESC")
    List<DataErasureTask> findCompletedTasks();

    @Query("SELECT SUM(d.recordsErased) FROM DataErasureTask d WHERE d.status = io.datasheild.retentionservice.entity.DataErasureTask$TaskStatus.COMPLETED " +
           "AND d.tenantId = :tenantId")
    Long countTotalErasedRecords(@Param("tenantId") UUID tenantId);
}
