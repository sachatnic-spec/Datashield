package com.datasheild.dpbi.repository;

import com.datasheild.dpbi.entity.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormSubmissionRepository extends JpaRepository<FormSubmission, Long> {
}
