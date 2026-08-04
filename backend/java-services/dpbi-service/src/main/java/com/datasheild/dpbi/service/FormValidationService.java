package com.datasheild.dpbi.service;

import com.datasheild.dpbi.entity.BreachNotification;
import com.datasheild.dpbi.entity.DpbiForm;
import com.datasheild.dpbi.exception.DpbiException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class FormValidationService {

    public void validateDeadline(BreachNotification notification) {
        LocalDateTime deadline = notification.getNotificationDueDate().atStartOfDay();
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new DpbiException("72-hour DPBI deadline exceeded");
        }
    }

    public void validateForm(DpbiForm form) {
        if (!StringUtils.hasText(form.getIncidentSummary()) || !StringUtils.hasText(form.getImpactAssessment()) ||
                !StringUtils.hasText(form.getRemediationPlan())) {
            throw new DpbiException("DPBI form is incomplete");
        }
    }
}
