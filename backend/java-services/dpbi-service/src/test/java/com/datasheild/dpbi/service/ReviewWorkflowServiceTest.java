package com.datasheild.dpbi.service;

import com.datasheild.dpbi.entity.DpbiForm;
import com.datasheild.dpbi.entity.FormReview;
import com.datasheild.dpbi.repository.DpbiFormRepository;
import com.datasheild.dpbi.repository.FormReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewWorkflowServiceTest {

    @Mock
    private DpbiFormRepository formRepository;
    @Mock
    private FormReviewRepository reviewRepository;

    private ReviewWorkflowService service;

    @BeforeEach
    void setUp() {
        service = new ReviewWorkflowService(formRepository, reviewRepository);
    }

    @Test
    void shouldSubmitForReview() {
        when(formRepository.findById(1L)).thenReturn(Optional.of(DpbiForm.builder().id(1L).build()));
        when(formRepository.save(any(DpbiForm.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewRepository.save(any(FormReview.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(service.submitForReview(1L, "dpo-user", "please review").getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldApproveReview() {
        when(reviewRepository.findById(2L)).thenReturn(Optional.of(FormReview.builder().id(2L).status("PENDING").build()));
        when(reviewRepository.save(any(FormReview.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(service.approveForm(2L, "ok").getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void shouldRejectReview() {
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(FormReview.builder().id(3L).status("PENDING").build()));
        when(reviewRepository.save(any(FormReview.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(service.rejectForm(3L, "needs changes").getStatus()).isEqualTo("REJECTED");
    }
}
