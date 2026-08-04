package com.datasheild.siem.controller;

import com.datasheild.siem.dto.SiemAlertRequest;
import com.datasheild.siem.entity.SiemAlert;
import com.datasheild.siem.repository.SiemAlertRepository;
import com.datasheild.siem.service.AzureSentinelConnectorService;
import com.datasheild.siem.service.IncidentAutoCreationService;
import com.datasheild.siem.service.QRadarConnectorService;
import com.datasheild.siem.service.SplunkConnectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiemControllerTest {

    @Mock
    private SiemAlertRepository repository;
    @Mock
    private SplunkConnectorService splunkConnectorService;
    @Mock
    private QRadarConnectorService qradarConnectorService;
    @Mock
    private AzureSentinelConnectorService sentinelConnectorService;
    @Mock
    private IncidentAutoCreationService incidentAutoCreationService;

    private SiemController controller;

    @BeforeEach
    void setUp() {
        controller = new SiemController(repository, splunkConnectorService, qradarConnectorService, sentinelConnectorService, incidentAutoCreationService);
    }

    @Test
    void shouldCreateAlert() {
        SiemAlertRequest request = new SiemAlertRequest();
        request.setTenantId("tenant-a");
        request.setAlertType("anomaly.detected");
        request.setSeverity("HIGH");
        request.setSourceSystem("SPLUNK");
        when(repository.save(any(SiemAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(controller.createAlert(request).getStatusCode().value()).isEqualTo(202);
    }

    @Test
    void shouldListAlerts() {
        when(repository.findByTenantId(eq("tenant-a"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(SiemAlert.builder().tenantId("tenant-a").build())));
        assertThat(controller.listAlerts("tenant-a", 0, 20).getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldReplayAlert() {
        when(repository.findById(3L)).thenReturn(Optional.of(SiemAlert.builder().id(3L).build()));
        assertThat(controller.replayAlert(3L).getBody().getId()).isEqualTo(3L);
    }
}
