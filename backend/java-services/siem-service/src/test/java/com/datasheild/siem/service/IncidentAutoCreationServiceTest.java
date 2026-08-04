package com.datasheild.siem.service;

import com.datasheild.siem.config.SiemProperties;
import com.datasheild.siem.entity.SiemAlert;
import com.datasheild.siem.repository.IncidentAutoCreationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentAutoCreationServiceTest {

    @Mock
    private IncidentAutoCreationRepository repository;

    private IncidentAutoCreationService service;

    @BeforeEach
    void setUp() {
        SiemProperties properties = new SiemProperties();
        properties.setAutoCreateThreshold("HIGH");
        service = new IncidentAutoCreationService(repository, properties);
    }

    @Test
    void shouldCreateIncidentForCriticalAlert() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(service.createAutoIncident(SiemAlert.builder().id(5L).severity("CRITICAL").build())).isNotNull();
        verify(repository).save(any());
    }

    @Test
    void shouldSkipIncidentForLowAlert() {
        assertThat(service.createAutoIncident(SiemAlert.builder().id(5L).severity("LOW").build())).isNull();
    }

    @Test
    void shouldAutoCreateForHighWhenThresholdHigh() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(service.createAutoIncident(SiemAlert.builder().id(6L).severity("HIGH").build()).getStatus()).isEqualTo("CREATED");
    }
}
