package com.datasheild.dpbi.repository;

import com.datasheild.dpbi.entity.FormReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FormReviewRepository extends JpaRepository<FormReview, Long> {
    Optional<FormReview> findTopByFormIdOrderByIdDesc(Long formId);
}
