package io.datasheild.vendorservice.service;

import io.datasheild.vendorservice.entity.Vendor;
import io.datasheild.vendorservice.entity.RiskAssessment;
import io.datasheild.vendorservice.repository.VendorRepository;
import io.datasheild.vendorservice.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorService {

    private final VendorRepository vendorRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;

    @Transactional
    public Vendor createVendor(String name, String vendorType, String processorRole) {
        log.info("Creating vendor: {} ({})", name, vendorType);

        Vendor vendor = Vendor.builder()
            .name(name)
            .vendorType(vendorType)
            .dataProcessorRole(processorRole)
            .status(Vendor.VendorStatus.PROSPECT)
            .riskScore(50)
            .riskLevel(Vendor.RiskLevel.MEDIUM)
            .build();

        vendor = vendorRepository.save(vendor);
        log.info("Vendor created: {} (ID: {})", name, vendor.getId());
        return vendor;
    }

    @Transactional
    public RiskAssessment assessVendorRisk(UUID vendorId, Integer securityScore, Integer complianceScore, Integer operationalScore) {
        log.info("Assessing risk for vendor: {}", vendorId);

        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new RuntimeException("Vendor not found: " + vendorId));

        int overallScore = (securityScore + complianceScore + operationalScore) / 3;

        Vendor.RiskLevel riskLevel = overallScore >= 80 ? Vendor.RiskLevel.LOW :
                                     overallScore >= 60 ? Vendor.RiskLevel.MEDIUM :
                                     overallScore >= 40 ? Vendor.RiskLevel.HIGH :
                                     Vendor.RiskLevel.CRITICAL;

        vendor.setRiskScore(overallScore);
        vendor.setRiskLevel(riskLevel);
        vendor.setRiskLastAssessed(LocalDateTime.now());
        vendorRepository.save(vendor);

        RiskAssessment assessment = RiskAssessment.builder()
            .vendorId(vendorId)
            .assessmentType("COMPREHENSIVE")
            .overallScore(overallScore)
            .securityScore(securityScore)
            .complianceScore(complianceScore)
            .operationalScore(operationalScore)
            .riskLevel(riskLevel)
            .assessmentDate(LocalDateTime.now())
            .nextAssessmentDate(LocalDateTime.now().plusDays(365))
            .build();

        assessment = riskAssessmentRepository.save(assessment);
        log.info("Risk assessment completed: {}, score: {}", vendorId, overallScore);

        return assessment;
    }

    @Transactional(readOnly = true)
    public List<Vendor> getActiveVendors() {
        return vendorRepository.findActiveVendors();
    }

    @Transactional(readOnly = true)
    public List<Vendor> getVendorsWithoutDPA() {
        return vendorRepository.findVendorsWithoutDPA();
    }

    @Transactional(readOnly = true)
    public List<RiskAssessment> getCriticalRiskAssessments() {
        return riskAssessmentRepository.findCriticalAssessments();
    }

    @Transactional(readOnly = true)
    public Long countCriticalRiskVendors() {
        return vendorRepository.countCriticalRiskVendors();
    }
}
