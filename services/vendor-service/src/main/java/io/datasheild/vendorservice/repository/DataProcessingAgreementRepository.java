package io.datasheild.vendorservice.repository;

import io.datasheild.vendorservice.entity.DataProcessingAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DataProcessingAgreementRepository extends JpaRepository<DataProcessingAgreement, UUID> {

    @Query("SELECT d FROM DataProcessingAgreement d WHERE d.vendorId = :vendorId ORDER BY d.version DESC")
    List<DataProcessingAgreement> findByVendorId(@Param("vendorId") UUID vendorId);

    @Query("SELECT d FROM DataProcessingAgreement d WHERE d.status = io.datasheild.vendorservice.entity.DataProcessingAgreement$DPAStatus.EXECUTED ORDER BY d.expiryDate ASC")
    List<DataProcessingAgreement> findExecutedAgreements();

    @Query("SELECT d FROM DataProcessingAgreement d WHERE d.expiryDate IS NOT NULL AND d.expiryDate < :now AND " +
           "d.status = io.datasheild.vendorservice.entity.DataProcessingAgreement$DPAStatus.EXECUTED")
    List<DataProcessingAgreement> findExpiredAgreements(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(d) FROM DataProcessingAgreement d WHERE d.status = io.datasheild.vendorservice.entity.DataProcessingAgreement$DPAStatus.EXECUTED")
    Long countExecutedAgreements();
}
