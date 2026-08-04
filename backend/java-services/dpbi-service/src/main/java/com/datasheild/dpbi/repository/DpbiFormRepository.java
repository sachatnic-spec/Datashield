package com.datasheild.dpbi.repository;

import com.datasheild.dpbi.entity.DpbiForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DpbiFormRepository extends JpaRepository<DpbiForm, Long> {
    Page<DpbiForm> findAllByBreachNotificationIdNotNull(Pageable pageable);
    Optional<DpbiForm> findByBreachNotificationId(Long breachNotificationId);
}
