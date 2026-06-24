package com.datasheild.dpbi.repository;

import com.datasheild.dpbi.entity.BreachNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BreachNotificationRepository extends JpaRepository<BreachNotification, Long> {
    Page<BreachNotification> findByTenantId(String tenantId, Pageable pageable);
    List<BreachNotification> findByStatusInAndNotificationDueDateBetween(List<String> statuses, LocalDate start, LocalDate end);
}
