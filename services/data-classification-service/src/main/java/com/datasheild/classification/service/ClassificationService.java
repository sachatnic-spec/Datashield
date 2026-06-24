package com.datasheild.classification.service;

import com.datasheild.classification.entity.DataClassification;
import com.datasheild.classification.entity.DLPRule;
import com.datasheild.classification.repository.DataClassificationRepository;
import com.datasheild.classification.repository.DLPRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassificationService {
    private final DataClassificationRepository classificationRepository;
    private final DLPRuleRepository dlpRuleRepository;

    public DataClassification classifyDataSet(UUID tenantId, String dataSetName, String tableName,
                                            Integer recordCount, Integer piiFieldCount,
                                            DataClassification.DataOwnershipType ownershipType) {
        // Auto-determine sensitivity level based on PII field count
        DataClassification.SensitivityLevel level = determineSensitivityLevel(piiFieldCount);
        
        DataClassification classification = DataClassification.builder()
            .tenantId(tenantId)
            .dataSetName(dataSetName)
            .tableName(tableName)
            .recordCount(recordCount)
            .piiFieldCount(piiFieldCount)
            .sensitivityLevel(level)
            .status(DataClassification.ClassificationStatus.CLASSIFIED)
            .ownershipType(ownershipType)
            .dlpEnforced(false)
            .build();
        
        return classificationRepository.save(classification);
    }

    private DataClassification.SensitivityLevel determineSensitivityLevel(Integer piiFieldCount) {
        if (piiFieldCount == 0) {
            return DataClassification.SensitivityLevel.PUBLIC;
        } else if (piiFieldCount <= 2) {
            return DataClassification.SensitivityLevel.INTERNAL;
        } else if (piiFieldCount <= 5) {
            return DataClassification.SensitivityLevel.CONFIDENTIAL;
        } else {
            return DataClassification.SensitivityLevel.RESTRICTED;
        }
    }

    public DLPRule createDLPRule(UUID tenantId, String ruleName, DLPRule.RuleType ruleType,
                                DataClassification.SensitivityLevel appliesTo,
                                DLPRule.Action action, Integer priority) {
        DLPRule rule = DLPRule.builder()
            .tenantId(tenantId)
            .ruleName(ruleName)
            .ruleType(ruleType)
            .appliesTo(appliesTo)
            .action(action)
            .priority(priority)
            .status(DLPRule.RuleStatus.ACTIVE)
            .enabled(true)
            .build();
        
        return dlpRuleRepository.save(rule);
    }

    public void enforceDLP(UUID classificationId, UUID tenantId) {
        DataClassification classification = classificationRepository.findById(classificationId)
            .orElseThrow(() -> new IllegalArgumentException("Classification not found"));
        
        List<DLPRule> applicableRules = dlpRuleRepository.findRulesByTenantAndLevel(
            tenantId, classification.getSensitivityLevel()
        );
        
        if (!applicableRules.isEmpty()) {
            classification.setDlpEnforced(true);
            classificationRepository.save(classification);
        }
    }

    public List<DLPRule> getApplicableRules(UUID tenantId, DataClassification.SensitivityLevel level) {
        return dlpRuleRepository.findRulesByTenantAndLevel(tenantId, level);
    }

    public Map<String, Object> getClassificationSummary(UUID tenantId) {
        List<DataClassification> allClassifications = classificationRepository.findByTenantId(tenantId);
        
        Map<DataClassification.SensitivityLevel, Long> sensitivityDistribution = 
            allClassifications.stream()
                .collect(Collectors.groupingBy(DataClassification::getSensitivityLevel, Collectors.counting()));
        
        long dlpEnforcedCount = allClassifications.stream()
            .filter(c -> c.getDlpEnforced() != null && c.getDlpEnforced()).count();
        
        Map<DataClassification.DataOwnershipType, Long> ownershipDistribution =
            allClassifications.stream()
                .collect(Collectors.groupingBy(DataClassification::getOwnershipType, Collectors.counting()));
        
        return Map.of(
            "totalDataSets", allClassifications.size(),
            "sensitivityDistribution", sensitivityDistribution,
            "dlpEnforced", dlpEnforcedCount,
            "ownershipDistribution", ownershipDistribution
        );
    }

    public List<DataClassification> getTenantClassifications(UUID tenantId) {
        return classificationRepository.findByTenantId(tenantId);
    }
}
