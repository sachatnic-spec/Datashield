package com.datasheild.connector.controller;

import com.datasheild.connector.dto.ConnectorRequest;
import com.datasheild.connector.dto.ConnectorResponse;
import com.datasheild.connector.entity.ConnectorLog;
import com.datasheild.connector.service.ConnectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/connectors")
@RequiredArgsConstructor
public class ConnectorController {

    private final ConnectorService connectorService;

    @PostMapping
    public ResponseEntity<ConnectorResponse> createConnector(@Valid @RequestBody ConnectorRequest request,
                                                             @RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId) {
        return ResponseEntity.ok(connectorService.createConnector(request, tenantId));
    }

    @GetMapping
    public ResponseEntity<Page<ConnectorResponse>> listConnectors(@RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(connectorService.listConnectors(tenantId, PageRequest.of(page, size)));
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<ConnectorResponse> testConnection(@PathVariable Long id,
                                                            @RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId) {
        return ResponseEntity.ok(connectorService.testConnection(id, tenantId));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<ConnectorResponse> syncConnector(@PathVariable Long id,
                                                           @RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId) {
        return ResponseEntity.accepted().body(connectorService.syncConnector(id, tenantId));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<Page<ConnectorLog>> getLogs(@PathVariable Long id,
                                                      @RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(connectorService.getLogs(id, tenantId, PageRequest.of(page, size)));
    }
}
