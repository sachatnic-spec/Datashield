package com.datasheild.discovery.service;

import com.datasheild.discovery.entity.PIIFinding;
import com.datasheild.discovery.entity.PIIScan;
import com.datasheild.discovery.repository.PIIFindingRepository;
import com.datasheild.discovery.repository.PIIScanRepository;
import com.datasheild.discovery.util.PIIDetectionPatterns;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DiscoveryService {
    private final PIIScanRepository piIScanRepository;
    private final PIIFindingRepository piiFindingRepository;
    private final JdbcTemplate jdbcTemplate;

    public PIIScan initiateScan(UUID tenantId, String scanName, PIIScan.ScanType scanType, 
                               String targetDatabase, String targetTable) {
        PIIScan scan = PIIScan.builder()
            .tenantId(tenantId)
            .scanName(scanName)
            .scanType(scanType)
            .status(PIIScan.ScanStatus.SCHEDULED)
            .targetDatabase(targetDatabase)
            .targetTable(targetTable)
            .totalRecordsScanned(0)
            .piiRecordsFound(0)
            .criticalCount(0)
            .highCount(0)
            .mediumCount(0)
            .build();
        
        return piIScanRepository.save(scan);
    }

    public void executeScan(UUID scanId) {
        PIIScan scan = piIScanRepository.findById(scanId)
            .orElseThrow(() -> new IllegalArgumentException("Scan not found"));
        
        scan.setStatus(PIIScan.ScanStatus.IN_PROGRESS);
        scan.setScanStartedAt(LocalDateTime.now());
        piIScanRepository.save(scan);
        
        try {
            List<Map<String, Object>> records = fetchRecordsToScan(scan);
            scanRecords(scan, records);
            
            scan.setStatus(PIIScan.ScanStatus.COMPLETED);
            scan.setScanCompletedAt(LocalDateTime.now());
        } catch (Exception e) {
            scan.setStatus(PIIScan.ScanStatus.FAILED);
        }
        
        piIScanRepository.save(scan);
    }

    private List<Map<String, Object>> fetchRecordsToScan(PIIScan scan) {
        String query = buildScanQuery(scan);
        return jdbcTemplate.queryForList(query);
    }

    private String buildScanQuery(PIIScan scan) {
        String schema = "discovery"; // Tenant-specific schema
        if (PIIScan.ScanType.CUSTOM_QUERY.equals(scan.getScanType())) {
            return scan.getScanQuery();
        } else if (PIIScan.ScanType.TABLE_SPECIFIC.equals(scan.getScanType())) {
            return String.format("SELECT * FROM %s.%s LIMIT 10000", schema, scan.getTargetTable());
        } else {
            return String.format("SELECT * FROM %s.* LIMIT 10000", schema);
        }
    }

    private void scanRecords(PIIScan scan, List<Map<String, Object>> records) {
        int totalScanned = records.size();
        Set<UUID> recordsWithPII = new HashSet<>();
        int criticalCount = 0, highCount = 0, mediumCount = 0;

        for (Map<String, Object> record : records) {
            for (Map.Entry<String, Object> entry : record.entrySet()) {
                if (entry.getValue() == null) continue;
                
                String value = entry.getValue().toString();
                PIIDetectionPatterns.DetectionResult result = PIIDetectionPatterns.detectPII(value);
                
                if (result != null) {
                    recordsWithPII.add(scan.getId());
                    
                    PIIFinding.Severity severity = determineSeverity(result.piiType, result.confidenceScore);
                    
                    PIIFinding finding = PIIFinding.builder()
                        .scanId(scan.getId())
                        .tenantId(scan.getTenantId())
                        .piiType(PIIFinding.PIIType.valueOf(result.piiType))
                        .severity(severity)
                        .tableName(scan.getTargetTable() != null ? scan.getTargetTable() : "unknown")
                        .columnName(entry.getKey())
                        .matchedValue(maskSensitiveData(value))
                        .context(value.length() > 100 ? value.substring(0, 100) : value)
                        .detectionMethod(PIIFinding.DetectionMethod.REGEX_PATTERN)
                        .confidenceScore(result.confidenceScore)
                        .build();
                    
                    piiFindingRepository.save(finding);
                    
                    switch (severity) {
                        case CRITICAL -> criticalCount++;
                        case HIGH -> highCount++;
                        case MEDIUM -> mediumCount++;
                    }
                }
            }
        }

        scan.setTotalRecordsScanned(totalScanned);
        scan.setPiiRecordsFound(recordsWithPII.size());
        scan.setCriticalCount(criticalCount);
        scan.setHighCount(highCount);
        scan.setMediumCount(mediumCount);
    }

    private PIIFinding.Severity determineSeverity(String piiType, double confidenceScore) {
        if ("AADHAAR".equals(piiType) || "PAN".equals(piiType) || "CREDIT_CARD".equals(piiType)) {
            return PIIFinding.Severity.CRITICAL;
        }
        if ("PASSPORT".equals(piiType) || "DRIVER_LICENSE".equals(piiType) || "BANK_ACCOUNT".equals(piiType)) {
            return PIIFinding.Severity.HIGH;
        }
        if (confidenceScore >= 0.90) {
            return PIIFinding.Severity.HIGH;
        }
        return confidenceScore >= 0.75 ? PIIFinding.Severity.MEDIUM : PIIFinding.Severity.LOW;
    }

    private String maskSensitiveData(String value) {
        if (value.length() <= 4) return "****";
        return value.substring(0, 2) + "*".repeat(value.length() - 4) + value.substring(value.length() - 2);
    }

    public List<PIIScan> getTenantScans(UUID tenantId) {
        return piIScanRepository.findByTenantId(tenantId);
    }

    public List<PIIFinding> getScanFindings(UUID scanId) {
        return piiFindingRepository.findByScanId(scanId);
    }

    public Map<String, Object> generateHotspotReport(UUID tenantId) {
        List<PIIFinding> allFindings = piiFindingRepository.findAllByTenantId(tenantId);
        
        Map<String, Long> tableHotspots = allFindings.stream()
            .collect(Collectors.groupingBy(PIIFinding::getTableName, Collectors.counting()));
        
        Map<PIIFinding.PIIType, Long> piiTypeDistribution = allFindings.stream()
            .collect(Collectors.groupingBy(PIIFinding::getPiiType, Collectors.counting()));
        
        long criticalCount = allFindings.stream()
            .filter(f -> f.getSeverity() == PIIFinding.Severity.CRITICAL).count();
        
        return Map.of(
            "tableHotspots", tableHotspots,
            "piiTypeDistribution", piiTypeDistribution,
            "totalFindings", allFindings.size(),
            "criticalFindings", criticalCount
        );
    }
}
