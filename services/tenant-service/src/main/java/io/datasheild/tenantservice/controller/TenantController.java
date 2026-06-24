package io.datasheild.tenantservice.controller;

import io.datasheild.tenantservice.dto.*;
import io.datasheild.tenantservice.entity.Tenant;
import io.datasheild.tenantservice.entity.TenantProvisioningHistory;
import io.datasheild.tenantservice.service.FeatureFlagService;
import io.datasheild.tenantservice.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/tenants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tenant Management", description = "Tenant provisioning, configuration, and lifecycle management")
public class TenantController {

    private final TenantService tenantService;
    private final FeatureFlagService featureFlagService;

    @PostMapping
    @Operation(summary = "Create a new tenant", description = "Create and initialize a new tenant with specified tier and configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tenant created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "409", description = "Tenant or schema already exists")
    })
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        log.info("POST /v1/tenants - Creating tenant: {}", request.getName());
        Tenant tenant = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TenantResponse.fromEntity(tenant));
    }

    @GetMapping("/{tenantId}")
    @Operation(summary = "Get tenant details", description = "Retrieve detailed information about a specific tenant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tenant found"),
        @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    public ResponseEntity<TenantResponse> getTenant(
        @Parameter(description = "Tenant UUID") @PathVariable UUID tenantId) {
        log.info("GET /v1/tenants/{} - Retrieving tenant", tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);
        return ResponseEntity.ok(TenantResponse.fromEntity(tenant));
    }

    @PutMapping("/{tenantId}")
    @Operation(summary = "Update tenant configuration", description = "Update tenant settings (name, contact, limits, etc)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tenant updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "404", description = "Tenant not found"),
        @ApiResponse(responseCode = "409", description = "Tenant name already exists")
    })
    public ResponseEntity<TenantResponse> updateTenant(
        @Parameter(description = "Tenant UUID") @PathVariable UUID tenantId,
        @Valid @RequestBody UpdateTenantRequest request) {
        log.info("PUT /v1/tenants/{} - Updating tenant", tenantId);
        Tenant tenant = tenantService.updateTenant(tenantId, request);
        return ResponseEntity.ok(TenantResponse.fromEntity(tenant));
    }

    @PostMapping("/{tenantId}/provision")
    @Operation(summary = "Provision tenant schema and infrastructure", 
               description = "Trigger schema creation, table setup, and initialization for a tenant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Provisioning initiated"),
        @ApiResponse(responseCode = "404", description = "Tenant not found"),
        @ApiResponse(responseCode = "500", description = "Provisioning failed")
    })
    public ResponseEntity<Void> provisionTenant(
        @Parameter(description = "Tenant UUID") @PathVariable UUID tenantId,
        @Parameter(description = "User initiating provisioning") @RequestParam(required = false) String executedBy) {
        log.info("POST /v1/tenants/{}/provision - Provisioning tenant", tenantId);
        tenantService.provisionTenant(tenantId, executedBy != null ? executedBy : "SYSTEM");
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{tenantId}/provisioning-history")
    @Operation(summary = "Get tenant provisioning history", description = "Retrieve detailed history of all provisioning attempts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Provisioning history retrieved"),
        @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    public ResponseEntity<Page<ProvisioningHistoryResponse>> getProvisioningHistory(
        @Parameter(description = "Tenant UUID") @PathVariable UUID tenantId,
        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.info("GET /v1/tenants/{}/provisioning-history - Retrieving provisioning history", tenantId);

        // Validate tenant exists
        tenantService.getTenantById(tenantId);

        Pageable pageable = PageRequest.of(page, size);
        Page<TenantProvisioningHistory> history = tenantService.getProvisioningHistoryPaginated(tenantId, pageable);

        Page<ProvisioningHistoryResponse> response = history.map(ProvisioningHistoryResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tier/{tier}/features")
    @Operation(summary = "List tier-specific features", description = "Get all available features for a tenant tier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Features retrieved"),
        @ApiResponse(responseCode = "400", description = "Invalid tier")
    })
    public ResponseEntity<List<FeatureFlagResponse>> getTierFeatures(
        @Parameter(description = "Tenant tier (STARTER, PROFESSIONAL, ENTERPRISE, GOVERNMENT)") @PathVariable String tier) {
        log.info("GET /v1/tenants/tier/{}/features - Retrieving features", tier);

        try {
            Tenant.TenantTier tierEnum = Tenant.TenantTier.valueOf(tier.toUpperCase());
            List<FeatureFlagResponse> features = featureFlagService.getAllFeaturesByTier(tierEnum)
                .stream()
                .map(FeatureFlagResponse::fromEntity)
                .collect(Collectors.toList());
            return ResponseEntity.ok(features);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid tier requested: {}", tier);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/active")
    @Operation(summary = "List active tenants", description = "Retrieve all tenants with ACTIVE subscription status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active tenants retrieved")
    })
    public ResponseEntity<Page<TenantResponse>> getActiveTenants(
        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.info("GET /v1/tenants/active - Retrieving active tenants");

        Pageable pageable = PageRequest.of(page, size);
        Page<Tenant> tenants = tenantService.getActiveTenants(pageable);

        Page<TenantResponse> response = tenants.map(TenantResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{tenantId}/archive")
    @Operation(summary = "Archive a tenant", description = "Mark tenant as archived and disable access")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Tenant archived successfully"),
        @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    public ResponseEntity<Void> archiveTenant(
        @Parameter(description = "Tenant UUID") @PathVariable UUID tenantId,
        @Parameter(description = "User initiating archival") @RequestParam(required = false) String executedBy) {
        log.info("POST /v1/tenants/{}/archive - Archiving tenant", tenantId);
        tenantService.archiveTenant(tenantId, executedBy != null ? executedBy : "SYSTEM");
        return ResponseEntity.noContent().build();
    }
}
