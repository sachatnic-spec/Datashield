package com.datasheild.siem.repository;

import com.datasheild.siem.entity.IncidentAutoCreation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentAutoCreationRepository extends JpaRepository<IncidentAutoCreation, Long> {
}
