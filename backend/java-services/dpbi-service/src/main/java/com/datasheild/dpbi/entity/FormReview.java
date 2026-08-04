package com.datasheild.dpbi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "form_review", schema = "dpbi", indexes = {
        @Index(name = "idx_form_review_form_id", columnList = "form_id"),
        @Index(name = "idx_form_review_reviewed_at", columnList = "reviewed_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id", nullable = false)
    private Long formId;

    @Column(name = "reviewed_by", nullable = false)
    private String reviewedBy;

    @Column(nullable = false)
    private String status;

    @Column(length = 2000)
    private String comments;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
