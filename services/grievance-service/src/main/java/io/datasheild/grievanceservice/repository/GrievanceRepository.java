package io.datasheild.grievanceservice.repository;

import io.datasheild.grievanceservice.entity.Grievance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface GrievanceRepository extends JpaRepository<Grievance, UUID> {

    @Query("SELECT g FROM Grievance g WHERE g.tenantId = :tenantId ORDER BY g.filedAt DESC")
    Page<Grievance> findByTenantId(@Param("tenantId") UUID tenantId, Pageable pageable);

    @Query("SELECT g FROM Grievance g WHERE g.status = io.datasheild.grievanceservice.entity.Grievance$GrievanceStatus.FILED")
    List<Grievance> findUnresolvedGrievances();

    @Query("SELECT g FROM Grievance g WHERE g.slaDeadline < :now AND " +
           "g.status NOT IN (io.datasheild.grievanceservice.entity.Grievance$GrievanceStatus.RESOLVED, " +
           "io.datasheild.grievanceservice.entity.Grievance$GrievanceStatus.REJECTED) " +
           "ORDER BY g.slaDeadline ASC")
    List<Grievance> findSLABreaches(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(g) FROM Grievance g WHERE g.status = io.datasheild.grievanceservice.entity.Grievance$GrievanceStatus.ESCALATED")
    Long countEscalatedGrievances();

    @Query("SELECT g FROM Grievance g WHERE g.dataPrincipalId = :dpId ORDER BY g.filedAt DESC")
    List<Grievance> findByDataPrincipalId(@Param("dpId") UUID dpId);
}
