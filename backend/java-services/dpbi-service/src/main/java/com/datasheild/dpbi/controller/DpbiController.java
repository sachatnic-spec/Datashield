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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/dpbi")
@RequiredArgsConstructor
public class DpbiController {

    private final BreachNotificationRepository notificationRepository;
    private final DpbiFormRepository formRepository;
    private final FormGenerationService formGenerationService;
    private final ReviewWorkflowService reviewWorkflowService;
    private final FormSubmissionService formSubmissionService;

    @PostMapping("/notifications")
    public ResponseEntity<DpbiForm> createNotification(@Valid @RequestBody DpbiFormRequest request,
                                                       @RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId) {
        BreachNotification notification = notificationRepository.save(BreachNotification.builder()
                .tenantId(tenantId)
                .breachId(request.getBreachId())
                .discoveryDate(request.getDiscoveryDate() == null ? LocalDate.now() : request.getDiscoveryDate())
                .notificationDueDate((request.getDiscoveryDate() == null ? LocalDate.now() : request.getDiscoveryDate()).plusDays(3))
                .status("DRAFT")
                .build());
        return ResponseEntity.ok(formGenerationService.generateFromBreach(notification));
    }

    @GetMapping("/notifications")
    public ResponseEntity<Page<BreachNotification>> listNotifications(@RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationRepository.findByTenantId(tenantId, PageRequest.of(page, size)));
    }

    @GetMapping("/forms")
    public ResponseEntity<Page<DpbiForm>> listForms(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(formRepository.findAllByBreachNotificationIdNotNull(PageRequest.of(page, size)));
    }

    @PutMapping("/forms/{id}")
    public ResponseEntity<DpbiForm> updateForm(@PathVariable Long id, @RequestBody DpbiFormRequest request) {
        DpbiForm form = formRepository.findById(id).orElseThrow(() -> new java.util.NoSuchElementException("Form not found"));
        form.setIncidentSummary(request.getIncidentSummary());
        form.setImpactAssessment(request.getImpactAssessment());
        form.setRemediationPlan(request.getRemediationPlan());
        form.setAffectedDataSubjects(request.getAffectedDataSubjects());
        form.setDataCategories(request.getDataCategories());
        return ResponseEntity.ok(formRepository.save(form));
    }

    @PostMapping("/forms/{id}/review")
    public ResponseEntity<FormReview> submitForReview(@PathVariable Long id, @Valid @RequestBody FormReviewRequest request) {
        return ResponseEntity.accepted().body(reviewWorkflowService.submitForReview(id, request.getReviewedBy(), request.getComments()));
    }

    @PostMapping("/reviews/{id}/approve")
    public ResponseEntity<FormReview> approve(@PathVariable Long id, @RequestBody(required = false) FormReviewRequest request) {
        return ResponseEntity.ok(reviewWorkflowService.approveForm(id, request == null ? null : request.getComments()));
    }

    @PostMapping("/reviews/{id}/reject")
    public ResponseEntity<FormReview> reject(@PathVariable Long id, @RequestBody(required = false) FormReviewRequest request) {
        return ResponseEntity.ok(reviewWorkflowService.rejectForm(id, request == null ? null : request.getComments()));
    }

    @PostMapping("/forms/{id}/submit")
    public ResponseEntity<FormSubmission> submitForm(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "system") String userId) {
        return ResponseEntity.accepted().body(formSubmissionService.submitForm(id, userId));
    }
}
