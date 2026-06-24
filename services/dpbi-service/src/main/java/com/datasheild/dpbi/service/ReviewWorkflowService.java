package com.datasheild.dpbi.service;

import com.datasheild.dpbi.entity.DpbiForm;
import com.datasheild.dpbi.entity.FormReview;
import com.datasheild.dpbi.repository.DpbiFormRepository;
import com.datasheild.dpbi.repository.FormReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewWorkflowService {

    private final DpbiFormRepository formRepository;
    private final FormReviewRepository reviewRepository;

    public FormReview submitForReview(Long formId, String dpoUserId, String comments) {
        DpbiForm form = formRepository.findById(formId).orElseThrow(() -> new java.util.NoSuchElementException("Form not found"));
        form.setLastUpdatedAt(LocalDateTime.now());
        formRepository.save(form);
        return reviewRepository.save(FormReview.builder()
                .formId(formId)
                .reviewedBy(dpoUserId)
                .status("PENDING")
                .comments(comments)
                .build());
    }

    public FormReview approveForm(Long reviewId, String comments) {
        FormReview review = reviewRepository.findById(reviewId).orElseThrow(() -> new java.util.NoSuchElementException("Review not found"));
        review.setStatus("APPROVED");
        review.setComments(comments);
        review.setReviewedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    public FormReview rejectForm(Long reviewId, String comments) {
        FormReview review = reviewRepository.findById(reviewId).orElseThrow(() -> new java.util.NoSuchElementException("Review not found"));
        review.setStatus("REJECTED");
        review.setComments(comments);
        review.setReviewedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }
}
