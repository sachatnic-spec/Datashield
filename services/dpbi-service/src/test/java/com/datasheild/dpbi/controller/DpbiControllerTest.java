package com.datasheild.dpbi.controller;

import com.datasheild.dpbi.dto.DpbiFormRequest;
import com.datasheild.dpbi.dto.FormReviewRequest;
import com.datasheild.dpbi.entity.BreachNotification;
import com.datasheild.dpbi.entity.DpbiForm;
import com.datasheild.dpbi.entity.FormReview;
import com.datasheild.dpbi.entity.FormSubmission;
import com.datasheild.dpbi.repository.BreachNotificationRepository;
import com.datasheild.dpbi.repository.DpbiFormRepository;
import com.datasheild.dpbi.service.FormGenerationService;
import com.datasheild.dpbi.service.FormSubmissionService;
import com.datasheild.dpbi.service.ReviewWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DpbiControllerTest {

    @Mock
    private BreachNotificationRepository notificationRepository;
    @Mock
    private DpbiFormRepository formRepository;
    @Mock
    private FormGenerationService formGenerationService;
    @Mock
    private ReviewWorkflowService reviewWorkflowService;
    @Mock
    private FormSubmissionService formSubmissionService;

    private DpbiController controller;

    @BeforeEach
    void setUp() {
        controller = new DpbiController(notificationRepository, formRepository, formGenerationService, reviewWorkflowService, formSubmissionService);
    }

    @Test
    void shouldCreateNotificationAndForm() {
        DpbiFormRequest request = new DpbiFormRequest();
        request.setBreachId(7L);
        request.setDiscoveryDate(LocalDate.now());
        when(notificationRepository.save(any(BreachNotification.class))).thenAnswer(invocation -> {
            BreachNotification notification = invocation.getArgument(0);
            notification.setId(10L);
            return notification;
        });
        when(formGenerationService.generateFromBreach(any(BreachNotification.class))).thenReturn(DpbiForm.builder().id(1L).build());
        assertThat(controller.createNotification(request, "tenant-a").getBody().getId()).isEqualTo(1L);
    }

    @Test
    void shouldListNotifications() {
        when(notificationRepository.findByTenantId(eq("tenant-a"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(BreachNotification.builder().tenantId("tenant-a").build())));
        assertThat(controller.listNotifications("tenant-a", 0, 10).getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldSubmitApprovedForm() {
        when(formSubmissionService.submitForm(2L, "system")).thenReturn(FormSubmission.builder().formId(2L).status("SUBMITTED").build());
        assertThat(controller.submitForm(2L, "system").getBody().getStatus()).isEqualTo("SUBMITTED");
    }
}
