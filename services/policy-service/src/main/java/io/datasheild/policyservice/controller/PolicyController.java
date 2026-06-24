package io.datasheild.policyservice.controller;

import io.datasheild.policyservice.entity.Policy;
import io.datasheild.policyservice.entity.PolicyRule;
import io.datasheild.policyservice.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/policies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Policy Management", description = "DPDP compliance policy management and enforcement")
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping
    @Operation(summary = "Create a new policy")
    public ResponseEntity<Policy> createPolicy(
        @RequestParam String name,
        @RequestParam String category,
        @RequestParam String content,
        @RequestParam(required = false) String dpdpSection,
        @RequestParam String createdBy) {
        log.info("POST /v1/policies - Creating policy: {}", name);
        Policy policy = policyService.createPolicy(name, category, content, dpdpSection, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(policy);
    }

    @PostMapping("/{policyId}/approve")
    @Operation(summary = "Approve a policy")
    public ResponseEntity<Policy> approvePolicy(
        @PathVariable UUID policyId,
        @RequestParam String approvedBy) {
        log.info("POST /v1/policies/{}/approve - Approving policy", policyId);
        Policy policy = policyService.approvePolicy(policyId, approvedBy);
        return ResponseEntity.ok(policy);
    }

    @PostMapping("/{policyId}/rules")
    @Operation(summary = "Add rule to policy")
    public ResponseEntity<PolicyRule> addRule(
        @PathVariable UUID policyId,
        @RequestParam String ruleName,
        @RequestParam String conditionType,
        @RequestParam String expression,
        @RequestParam String action,
        @RequestParam(defaultValue = "1") Integer priority) {
        log.info("POST /v1/policies/{}/rules - Adding rule: {}", policyId, ruleName);
        PolicyRule rule = policyService.addRule(policyId, ruleName, conditionType, expression, action, priority);
        return ResponseEntity.status(HttpStatus.CREATED).body(rule);
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active policies")
    public ResponseEntity<List<Policy>> getActivePolicies() {
        log.info("GET /v1/policies/active - Retrieving active policies");
        List<Policy> policies = policyService.getActivePolicies();
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/{policyId}/rules")
    @Operation(summary = "Get rules for policy")
    public ResponseEntity<List<PolicyRule>> getPolicyRules(@PathVariable UUID policyId) {
        log.info("GET /v1/policies/{}/rules - Retrieving rules", policyId);
        List<PolicyRule> rules = policyService.getRulesForPolicy(policyId);
        return ResponseEntity.ok(rules);
    }
}
