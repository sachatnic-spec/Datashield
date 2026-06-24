package com.datasheild.connector.service;

import com.datasheild.connector.dto.ConnectorRequest;
import com.datasheild.connector.entity.Connector;
import com.datasheild.connector.entity.ConnectorLog;
import com.datasheild.connector.entity.DataTransfer;
import com.datasheild.connector.repository.ConnectorLogRepository;
import com.datasheild.connector.repository.ConnectorRepository;
import com.datasheild.connector.repository.DataTransferRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectorServiceTest {

    @Mock
    private ConnectorRepository connectorRepository;
    @Mock
    private DataTransferRepository dataTransferRepository;
    @Mock
    private ConnectorLogRepository connectorLogRepository;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ConnectorService connectorService;

    @BeforeEach
    void setUp() {
        connectorService = new ConnectorService(connectorRepository, dataTransferRepository, connectorLogRepository,
                new VaultService(), kafkaTemplate, new ObjectMapper());
    }

    @Test
    void shouldCreateConnectorWithEncryptedCredentials() {
        ConnectorRequest request = new ConnectorRequest();
        request.setName("Warehouse");
        request.setConnectorType("POSTGRESQL");
        request.setSourceType("DB");
        request.setTargetType("LAKE");
        request.setEndpoint("jdbc:postgresql://localhost/db");
        request.setCredentials("password");

        when(connectorRepository.save(any(Connector.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = connectorService.createConnector(request, "tenant-a");

        assertThat(response.tenantId()).isEqualTo("tenant-a");
        ArgumentCaptor<Connector> captor = ArgumentCaptor.forClass(Connector.class);
        verify(connectorRepository).save(captor.capture());
        assertThat(captor.getValue().getCredentialsEncrypted()).isNotEqualTo("password");
    }

    @Test
    void shouldRejectConnectionTestWhenCredentialsMissing() {
        Connector connector = Connector.builder().id(10L).tenantId("tenant-a").credentialsEncrypted(" ").build();
        when(connectorRepository.findByIdAndTenantId(10L, "tenant-a")).thenReturn(Optional.of(connector));

        assertThatThrownBy(() -> connectorService.testConnection(10L, "tenant-a"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void shouldSyncConnectorAndPersistTransfer() {
        Connector connector = Connector.builder()
                .id(12L)
                .tenantId("tenant-a")
                .connectorType("POSTGRESQL")
                .sourceType("DB")
                .targetType("LAKE")
                .credentialsEncrypted("c2VjcmV0")
                .build();
        when(connectorRepository.findByIdAndTenantId(12L, "tenant-a")).thenReturn(Optional.of(connector));
        when(dataTransferRepository.save(any(DataTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(connectorRepository.save(any(Connector.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(kafkaTemplate.executeInTransaction(any())).thenAnswer(invocation -> invocation.getArgument(0, org.springframework.kafka.core.KafkaOperations.OperationsCallback.class).doInOperations(kafkaTemplate));

        var response = connectorService.syncConnector(12L, "tenant-a");

        assertThat(response.lastSyncedAt()).isNotNull();
        verify(dataTransferRepository).save(any(DataTransfer.class));
        verify(connectorLogRepository).save(any(ConnectorLog.class));
    }

    @Test
    void shouldThrowWhenConnectorMissing() {
        when(connectorRepository.findByIdAndTenantId(99L, "tenant-a")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> connectorService.syncConnector(99L, "tenant-a"))
                .isInstanceOf(NoSuchElementException.class);
    }
}
