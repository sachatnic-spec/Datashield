package io.datasheild.rightsservice.controller;

import io.datasheild.rightsservice.dto.CreateDPRRequest;
import io.datasheild.rightsservice.dto.DPRActivityResponse;
import io.datasheild.rightsservice.dto.DPRRequestResponse;
import io.datasheild.rightsservice.dto.VerifyIdentityRequest;
import io.datasheild.rightsservice.service.DPRService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/dpr")
@RequiredArgsConstructor
@Slf4j
public class DPRController {

    private final DPRService dprService;

    @PostMapping("/requests")
    public ResponseEntity<DPRRequestResponse> createRequest(
            @RequestHeader(value = "X-Tenant-ID", required = true) UUID tenantId,
            @RequestHeader(value = "X-Data-Principal-ID", required = true) UUID dataPrincipalId,
            @RequestBody CreateDPRRequest request) {
        log.info("POST /v1/dpr/requests: tenant={} dp={} type={}", tenantId, dataPrincipalId, request.getRequestType());

        if (!request.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        DPRRequestResponse response = dprService.createDPRRequest(tenantId, dataPrincipalId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify-identity")
    public ResponseEntity<DPRRequestResponse> verifyIdentity(
            @RequestHeader(value = "X-Tenant-ID", required = true) UUID tenantId,
            @RequestBody VerifyIdentityRequest request) {
        log.info("POST /v1/dpr/verify-identity: tenant={} requestId={}", tenantId, request.getRequestId());

        if (!request.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        DPRRequestResponse response = dprService.verifyIdentity(tenantId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<DPRRequestResponse>> getRequests(
            @RequestHeader(value = "X-Tenant-ID", required = true) UUID tenantId,
            @RequestHeader(value = "X-Data-Principal-ID", required = true) UUID dataPrincipalId) {
        log.info("GET /v1/dpr/requests: tenant={} dp={}", tenantId, dataPrincipalId);

        List<DPRRequestResponse> responses = dprService.getRequestsByDataPrincipal(tenantId, dataPrincipalId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/requests/{requestId}")
    public ResponseEntity<DPRRequestResponse> getRequest(
            @RequestHeader(value = "X-Tenant-ID", required = true) UUID tenantId,
            @PathVariable UUID requestId) {
        log.info("GET /v1/dpr/requests/{}: tenant={}", requestId, tenantId);

        DPRRequestResponse response = dprService.getRequestDetails(tenantId, requestId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests/{requestId}/activities")
    public ResponseEntity<List<DPRActivityResponse>> getActivities(
            @RequestHeader(value = "X-Tenant-ID", required = true) UUID tenantId,
            @PathVariable UUID requestId) {
        log.info("GET /v1/dpr/requests/{}/activities: tenant={}", requestId, tenantId);

        List<DPRActivityResponse> activities = dprService.getRequestActivities(tenantId, requestId);
        return ResponseEntity.ok(activities);
    }
}
