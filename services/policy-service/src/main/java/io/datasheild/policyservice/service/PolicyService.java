package io.datasheild.policyservice.service;

import io.datasheild.policyservice.entity.Policy;
import io.datasheild.policyservice.entity.PolicyRule;
import io.datasheild.policyservice.repository.PolicyRepository;
import io.datasheild.policyservice.repository.PolicyRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyRuleRepository policyRuleRepository;

    @Transactional
    public Policy createPolicy(String name, String category, String content, String dpdpSection, String createdBy) {
        log.info("Creating policy: {} ({})", name, category);

        Policy policy = Policy.builder()
            .name(name)
            .category(category)
            .content(content)
            .dpdpSection(dpdpSection)
            .createdBy(createdBy)
            .enforcementLevel("MANDATORY")
            .applicableTiers("STARTER,PROFESSIONAL,ENTERPRISE,GOVERNMENT")
            .build();

        policy = policyRepository.save(policy);
        log.info("Policy created: {} (ID: {})", name, policy.getId());
        return policy;
    }

    @Transactional
    public Policy approvePolicy(UUID policyId, String approvedBy) {
        log.info("Approving policy: {}", policyId);

        Policy policy = policyRepository.findById(policyId)
            .orElseThrow(() -> new RuntimeException("Policy not found: " + policyId));

        policy.setStatus(Policy.PolicyStatus.ACTIVE);
        policy.setApprovedBy(approvedBy);
        policy.setEffectiveDate(LocalDateTime.now());
        policy = policyRepository.save(policy);

        log.info("Policy approved: {}", policyId);
        return policy;
    }

    @Transactional
    public PolicyRule addRule(UUID policyId, String ruleName, String conditionType, String expression, String action, Integer priority) {
        log.info("Adding rule to policy: {}", policyId);

        PolicyRule rule = PolicyRule.builder()
            .policyId(policyId)
            .ruleName(ruleName)
            .conditionType(conditionType)
            .conditionExpression(expression)
            .action(action)
            .priority(priority)
            .isActive(true)
            .build();

        rule = policyRuleRepository.save(rule);
        log.info("Rule added: {}", rule.getId());
        return rule;
    }

    @Transactional(readOnly = true)
    public List<Policy> getActivePolicies() {
        return policyRepository.findActivePolicies();
    }

    @Transactional(readOnly = true)
    public Page<Policy> getPoliciesByCategory(String category, Pageable pageable) {
        return policyRepository.findByCategory(category, pageable);
    }

    @Transactional(readOnly = true)
    public List<PolicyRule> getRulesForPolicy(UUID policyId) {
        return policyRuleRepository.findActiveRulesByPolicy(policyId);
    }

    @Transactional(readOnly = true)
    public boolean evaluatePolicy(UUID policyId, String conditionType, String contextData) {
        List<PolicyRule> rules = policyRuleRepository.findByConditionType(conditionType);
        return !rules.isEmpty();
    }
}
