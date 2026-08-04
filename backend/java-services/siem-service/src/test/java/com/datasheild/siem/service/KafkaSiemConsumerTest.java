package com.datasheild.siem.service;

import com.datasheild.siem.entity.SiemAlert;
import com.datasheild.siem.repository.SiemAlertRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class KafkaSiemConsumerTest {

    @Mock
    private SiemAlertRepository repository;
    @Mock
    private SplunkConnectorService splunkConnectorService;
    @Mock
    private QRadarConnectorService qRadarConnectorService;
    @Mock
    private AzureSentinelConnectorService azureSentinelConnectorService;
    @Mock
    private IncidentAutoCreationService incidentAutoCreationService;

    private KafkaSiemConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new KafkaSiemConsumer(repository, splunkConnectorService, qRadarConnectorService,
                azureSentinelConnectorService, incidentAutoCreationService, new ObjectMapper());
    }

    @Test
    void shouldParseCriticalAnomaly() {
        SiemAlert alert = consumer.parseMessage("{\"tenantId\":\"tenant-a\",\"anomalyScore\":0.95}", "anomaly.detected");
        assertThat(alert.getSeverity()).isEqualTo("CRITICAL");
    }

    @Test
    void shouldParseBreachAsCritical() {
        SiemAlert alert = consumer.parseMessage("{\"tenantId\":\"tenant-a\"}", "breach.incident.created");
        assertThat(alert.getSeverity()).isEqualTo("CRITICAL");
    }

    @Test
    void shouldForwardPersistedAlert() {
        when(repository.save(any(SiemAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));
        consumer.onPlatformEvent("{\"tenantId\":\"tenant-a\",\"message\":\"test\"}", "audit.entry.created");
        verify(splunkConnectorService).postEvent(any());
        verify(qRadarConnectorService).postEvent(any());
        verify(azureSentinelConnectorService).postEvent(any());
    }
}
