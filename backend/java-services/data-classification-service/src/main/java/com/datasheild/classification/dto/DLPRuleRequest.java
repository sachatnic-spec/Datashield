package com.datasheild.classification.dto;

import com.datasheild.classification.entity.DataClassification;
import com.datasheild.classification.entity.DLPRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DLPRuleRequest {
    private String ruleName;
    private DLPRule.RuleType ruleType;
    private DataClassification.SensitivityLevel appliesToLevel;
    private DLPRule.Action action;
    private Integer priority;
}
