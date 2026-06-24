package com.datasheild.dpbi.service;

import com.datasheild.dpbi.config.DpbiProperties;
import com.datasheild.dpbi.entity.BreachNotification;
import com.datasheild.dpbi.entity.DpbiForm;
import com.datasheild.dpbi.entity.FormSubmission;
import com.datasheild.dpbi.repository.BreachNotificationRepository;
import com.datasheild.dpbi.repository.DpbiFormRepository;
import com.datasheild.dpbi.repository.FormReviewRepository;
import com.datasheild.dpbi.repository.FormSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class FormSubmissionService {

    private final DpbiFormRepository formRepository;
    private final BreachNotificationRepository notificationRepository;
    private final FormReviewRepository reviewRepository;
    private final FormSubmissionRepository submissionRepository;
    private final FormValidationService validationService;
    private final DpbiProperties properties;
    private final RestTemplate restTemplate;

    public FormSubmission submitForm(Long formId, String submittedBy) {
        DpbiForm form = formRepository.findById(formId).orElseThrow(() -> new java.util.NoSuchElementException("Form not found"));
        validationService.validateForm(form);
        reviewRepository.findTopByFormIdOrderByIdDesc(formId)
                .filter(review -> "APPROVED".equals(review.getStatus()))
                .orElseThrow(() -> new IllegalArgumentException("Approved review is required before submission"));

        BreachNotification notification = notificationRepository.findById(form.getBreachNotificationId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Notification not found"));
        validationService.validateDeadline(notification);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String response = restTemplate.postForObject(properties.getApi().getBaseUrl(), new HttpEntity<>(Map.of(
                "notificationId", notification.getId(),
                "formId", form.getId(),
                "summary", form.getIncidentSummary()
        ), headers), String.class);

        notification.setStatus("SUBMITTED");
        notificationRepository.save(notification);

        return submissionRepository.save(FormSubmission.builder()
                .formId(formId)
                .submittedBy(submittedBy)
                .externalReference("DPBI-" + formId)
                .status("SUBMITTED")
                .submittedAt(LocalDateTime.now())
                .responsePayload(response)
                .build());
    }
}
