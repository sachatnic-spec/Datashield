package com.datasheild.dpbi.service;

import com.datasheild.dpbi.entity.BreachNotification;
import com.datasheild.dpbi.entity.DpbiForm;
import com.datasheild.dpbi.exception.DpbiException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormValidationServiceTest {

    private final FormValidationService service = new FormValidationService();

    @Test
    void shouldAllowFutureDeadline() {
        BreachNotification notification = BreachNotification.builder().notificationDueDate(LocalDate.now().plusDays(1)).build();
        assertThatCode(() -> service.validateDeadline(notification)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectExpiredDeadline() {
        BreachNotification notification = BreachNotification.builder().notificationDueDate(LocalDate.now().minusDays(1)).build();
        assertThatThrownBy(() -> service.validateDeadline(notification)).isInstanceOf(DpbiException.class);
    }

    @Test
    void shouldRejectIncompleteForm() {
        assertThatThrownBy(() -> service.validateForm(DpbiForm.builder().incidentSummary("summary").build()))
                .isInstanceOf(DpbiException.class);
    }
}
