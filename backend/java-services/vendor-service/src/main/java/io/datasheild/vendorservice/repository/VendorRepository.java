package io.datasheild.vendorservice.repository;

import io.datasheild.vendorservice.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID> {

    @Query("SELECT v FROM Vendor v WHERE v.status = io.datasheild.vendorservice.entity.Vendor$VendorStatus.ACTIVE ORDER BY v.name")
    List<Vendor> findActiveVendors();

    @Query("SELECT v FROM Vendor v WHERE v.vendorType = :type ORDER BY v.createdAt DESC")
    Page<Vendor> findByType(@Param("type") String type, Pageable pageable);

    @Query("SELECT v FROM Vendor v WHERE v.riskLevel = :riskLevel ORDER BY v.riskScore DESC")
    List<Vendor> findByRiskLevel(@Param("riskLevel") Vendor.RiskLevel riskLevel);

    @Query("SELECT v FROM Vendor v WHERE v.hasDPA = false AND v.status = io.datasheild.vendorservice.entity.Vendor$VendorStatus.ACTIVE")
    List<Vendor> findVendorsWithoutDPA();

    @Query("SELECT COUNT(v) FROM Vendor v WHERE v.riskLevel = io.datasheild.vendorservice.entity.Vendor$RiskLevel.CRITICAL")
    Long countCriticalRiskVendors();
}
