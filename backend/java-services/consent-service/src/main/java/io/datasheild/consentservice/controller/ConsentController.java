package io.datasheild.consentservice.controller;

import io.datasheild.consentservice.dto.ConsentResponse;
import io.datasheild.consentservice.dto.GrantConsentRequest;
import io.datasheild.consentservice.dto.PurposeResponse;
import io.datasheild.consentservice.dto.WithdrawConsentRequest;
import io.datasheild.consentservice.service.ConsentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/consent")
@RequiredArgsConstructor
@Slf4j
public class ConsentController {

    private final ConsentService consentService;

    @PostMapping("/records")
    public ResponseEntity<ConsentResponse> grantConsent(
            @RequestHeader(value = "X-Tenant-ID", required = true) UUID tenantId,
            @RequestBody GrantConsentRequest request) {
        log.info("POST /v1/consent/records: tenant={}", tenantId);

        if (!request.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        ConsentResponse response = consentService.grantConsent(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/records/{consentId}")
    public ResponseEntity<ConsentResponse> withdrawConsent(
            @RequestHeader(value = "X-Tenant-ID", required = true) UUID tenantId,
            @PathVariable UUID consentId,
            @RequestBody WithdrawConsentRequest request) {
        log.info("DELETE /v1/consent/records/{}: tenant={}", consentId, tenantId);

        request.setConsentId(consentId);
        ConsentResponse response = consentService.withdrawConsent(tenantId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/records")
    public ResponseEntity<List<ConsentResponse>> getConsents(
            @RequestHeader(value = "X-Tenant-ID", required = true) UUID tenantId,
            @RequestParam(value = "dpId", required = true) UUID dataPrincipalId) {
        log.info("GET /v1/consent/records: tenant={} dp={}", tenantId, dataPrincipalId);

        List<ConsentResponse> records = consentService.getActiveConsents(tenantId, dataPrincipalId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/purposes")
    public ResponseEntity<List<PurposeResponse>> getPurposes(
            @RequestHeader(value = "X-Tenant-ID", required = true) UUID tenantId) {
        log.info("GET /v1/consent/purposes: tenant={}", tenantId);

        List<PurposeResponse> purposes = consentService.getActivePurposes(tenantId);
        return ResponseEntity.ok(purposes);
    }
}
