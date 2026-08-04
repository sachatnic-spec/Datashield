package io.datasheild.retentionservice.repository;

import io.datasheild.retentionservice.entity.RetentionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RetentionPolicyRepository extends JpaRepository<RetentionPolicy, UUID> {

    @Query("SELECT rp FROM RetentionPolicy rp WHERE rp.status = io.datasheild.retentionservice.entity.RetentionPolicy$PolicyStatus.ACTIVE")
    List<RetentionPolicy> findActivePolicies();

    @Query("SELECT rp FROM RetentionPolicy rp WHERE rp.dataCategory = :category AND rp.status = io.datasheild.retentionservice.entity.RetentionPolicy$PolicyStatus.ACTIVE")
    List<RetentionPolicy> findByCategory(@Param("category") String category);

    @Query("SELECT rp FROM RetentionPolicy rp WHERE rp.sector = :sector ORDER BY rp.retentionDaysDefault DESC")
    List<RetentionPolicy> findBySector(@Param("sector") String sector);

    @Query("SELECT COUNT(rp) FROM RetentionPolicy rp WHERE rp.status = io.datasheild.retentionservice.entity.RetentionPolicy$PolicyStatus.ACTIVE")
    Long countActivePolicies();
}
