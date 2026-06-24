package com.datasheild.connector.controller;

import com.datasheild.connector.dto.ConnectorRequest;
import com.datasheild.connector.dto.ConnectorResponse;
import com.datasheild.connector.entity.ConnectorLog;
import com.datasheild.connector.service.ConnectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectorControllerTest {

    @Mock
    private ConnectorService connectorService;

    private ConnectorController controller;

    @BeforeEach
    void setUp() {
        controller = new ConnectorController(connectorService);
    }

    @Test
    void shouldCreateConnector() {
        ConnectorRequest request = new ConnectorRequest();
        when(connectorService.createConnector(any(), eq("tenant-a"))).thenReturn(sampleResponse());
        assertThat(controller.createConnector(request, "tenant-a").getBody().tenantId()).isEqualTo("tenant-a");
    }

    @Test
    void shouldListConnectors() {
        when(connectorService.listConnectors(eq("tenant-a"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse())));
        assertThat(controller.listConnectors("tenant-a", 0, 10).getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldReturnLogs() {
        when(connectorService.getLogs(eq(1L), eq("tenant-a"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(ConnectorLog.builder().message("ok").build())));
        assertThat(controller.getLogs(1L, "tenant-a", 0, 10).getBody().getContent()).hasSize(1);
    }

    private ConnectorResponse sampleResponse() {
        return ConnectorResponse.builder()
                .id(1L)
                .tenantId("tenant-a")
                .name("sample")
                .connectorType("POSTGRESQL")
                .sourceType("DB")
                .targetType("LAKE")
                .endpoint("jdbc")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
