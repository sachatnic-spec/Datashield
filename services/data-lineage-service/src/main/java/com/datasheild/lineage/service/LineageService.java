package com.datasheild.lineage.service;

import com.datasheild.lineage.entity.DataFlow;
import com.datasheild.lineage.entity.LineageAudit;
import com.datasheild.lineage.repository.DataFlowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LineageService {
    private final DataFlowRepository dataFlowRepository;

    public DataFlow recordDataFlow(UUID tenantId, String sourceTable, String sourceDatabase,
                                   String targetTable, String targetDatabase,
                                   DataFlow.TransformationType transformationType,
                                   Boolean isThirdPartySharing, String thirdPartyName) {
        DataFlow flow = DataFlow.builder()
            .tenantId(tenantId)
            .sourceTable(sourceTable)
            .sourceDatabase(sourceDatabase)
            .targetTable(targetTable)
            .targetDatabase(targetDatabase)
            .transformationType(transformationType)
            .isThirdPartySharing(isThirdPartySharing != null && isThirdPartySharing)
            .thirdPartyName(thirdPartyName)
            .status(DataFlow.DataFlowStatus.ACTIVE)
            .recordsProcessed(0)
            .build();
        
        return dataFlowRepository.save(flow);
    }

    public List<DataFlow> getDownstreamLineage(UUID tenantId, String sourceTable) {
        return dataFlowRepository.findDownstreamFlows(sourceTable, tenantId);
    }

    public List<DataFlow> getUpstreamLineage(UUID tenantId, String targetTable) {
        return dataFlowRepository.findUpstreamFlows(targetTable, tenantId);
    }

    public List<DataFlow> getAllDataFlows(UUID tenantId) {
        return dataFlowRepository.findByTenantId(tenantId);
    }

    public Map<String, Object> getDataLineageGraph(UUID tenantId) {
        List<DataFlow> allFlows = dataFlowRepository.findByTenantId(tenantId);
        
        Map<String, Object> nodes = new HashMap<>();
        Map<String, Object> edges = new ArrayList<>();
        
        Set<String> uniqueTables = new HashSet<>();
        for (DataFlow flow : allFlows) {
            uniqueTables.add(flow.getSourceTable());
            uniqueTables.add(flow.getTargetTable());
        }
        
        for (String table : uniqueTables) {
            nodes.put(table, Map.of(
                "id", table,
                "label", table,
                "type", "table"
            ));
        }
        
        for (DataFlow flow : allFlows) {
            ((List<Object>) edges).add(Map.of(
                "source", flow.getSourceTable(),
                "target", flow.getTargetTable(),
                "transformation", flow.getTransformationType().toString(),
                "thirdPartySharing", flow.getIsThirdPartySharing()
            ));
        }
        
        return Map.of(
            "nodes", nodes,
            "edges", edges,
            "totalFlows", allFlows.size()
        );
    }

    public Map<String, Object> getComplianceImpactAnalysis(UUID tenantId) {
        List<DataFlow> thirdPartySharings = dataFlowRepository.findThirdPartySharings(tenantId);
        List<DataFlow> encryptionFlows = dataFlowRepository.findByTenantId(tenantId).stream()
            .filter(f -> f.getTransformationType() == DataFlow.TransformationType.ENCRYPT)
            .collect(Collectors.toList());
        
        return Map.of(
            "totalThirdPartySharings", thirdPartySharings.size(),
            "thirdPartyVendors", thirdPartySharings.stream()
                .map(DataFlow::getThirdPartyName)
                .distinct()
                .collect(Collectors.toList()),
            "encryptedFlows", encryptionFlows.size(),
            "complianceRisk", assessComplianceRisk(thirdPartySharings.size())
        );
    }

    private String assessComplianceRisk(int thirdPartySharings) {
        if (thirdPartySharings == 0) return "LOW";
        if (thirdPartySharings <= 3) return "MEDIUM";
        if (thirdPartySharings <= 10) return "HIGH";
        return "CRITICAL";
    }

    public List<String> getDataPrincipalsAffected(UUID tenantId, String table) {
        List<DataFlow> flows = dataFlowRepository.findByTenantId(tenantId).stream()
            .filter(f -> f.getSourceTable().equals(table) || f.getTargetTable().equals(table))
            .collect(Collectors.toList());
        
        // In production, integrate with Rights Service to get actual data principals
        return List.of("Principal_" + tenantId.toString().substring(0, 8));
    }
}
