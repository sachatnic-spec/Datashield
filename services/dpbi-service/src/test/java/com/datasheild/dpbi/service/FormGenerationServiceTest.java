package com.datasheild.dpbi.service;

import com.datasheild.dpbi.entity.BreachNotification;
import com.datasheild.dpbi.entity.DpbiForm;
import com.datasheild.dpbi.repository.DpbiFormRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormGenerationServiceTest {

    @Mock
    private DpbiFormRepository repository;

    private FormGenerationService service;

    @BeforeEach
    void setUp() {
        service = new FormGenerationService(repository, new BreachServiceClient());
    }

    @Test
    void shouldGenerateFormFromBreach() {
        when(repository.save(any(DpbiForm.class))).thenAnswer(invocation -> invocation.getArgument(0));
        DpbiForm form = service.generateFromBreach(BreachNotification.builder().id(1L).breachId(5L).build());
        assertThat(form.getIncidentSummary()).contains("Breach incident");
    }

    @Test
    void shouldPopulateDataCategories() {
        when(repository.save(any(DpbiForm.class))).thenAnswer(invocation -> invocation.getArgument(0));
        DpbiForm form = service.generateFromBreach(BreachNotification.builder().id(1L).breachId(6L).build());
        assertThat(form.getDataCategories()).contains("PII");
    }
}
