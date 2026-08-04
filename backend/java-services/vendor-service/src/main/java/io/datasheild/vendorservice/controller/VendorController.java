package io.datasheild.vendorservice.controller;

import io.datasheild.vendorservice.entity.Vendor;
import io.datasheild.vendorservice.entity.RiskAssessment;
import io.datasheild.vendorservice.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/vendors")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vendor Management", description = "Vendor lifecycle, DPA tracking, risk assessment")
public class VendorController {

    private final VendorService vendorService;

    @PostMapping
    @Operation(summary = "Register a new vendor")
    public ResponseEntity<Vendor> createVendor(
        @RequestParam String name,
        @RequestParam String type,
        @RequestParam String processorRole) {
        log.info("POST /v1/vendors - Creating vendor: {}", name);
        Vendor vendor = vendorService.createVendor(name, type, processorRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(vendor);
    }

    @PostMapping("/{vendorId}/assess-risk")
    @Operation(summary = "Assess vendor risk")
    public ResponseEntity<RiskAssessment> assessRisk(
        @PathVariable UUID vendorId,
        @RequestParam Integer securityScore,
        @RequestParam Integer complianceScore,
        @RequestParam Integer operationalScore) {
        log.info("POST /v1/vendors/{}/assess-risk - Assessing risk", vendorId);
        RiskAssessment assessment = vendorService.assessVendorRisk(vendorId, securityScore, complianceScore, operationalScore);
        return ResponseEntity.status(HttpStatus.CREATED).body(assessment);
    }

    @GetMapping("/active")
    @Operation(summary = "List active vendors")
    public ResponseEntity<List<Vendor>> getActiveVendors() {
        log.info("GET /v1/vendors/active - Retrieving active vendors");
        List<Vendor> vendors = vendorService.getActiveVendors();
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/without-dpa")
    @Operation(summary = "List vendors without DPA")
    public ResponseEntity<List<Vendor>> getVendorsWithoutDPA() {
        log.info("GET /v1/vendors/without-dpa - Retrieving vendors without DPA");
        List<Vendor> vendors = vendorService.getVendorsWithoutDPA();
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/critical-risk")
    @Operation(summary = "Get count of critical risk vendors")
    public ResponseEntity<Long> getCriticalRiskCount() {
        log.info("GET /v1/vendors/critical-risk - Counting critical risk vendors");
        Long count = vendorService.countCriticalRiskVendors();
        return ResponseEntity.ok(count);
    }
}
