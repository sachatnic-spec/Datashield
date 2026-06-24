package io.datasheild.grievanceservice.repository;

import io.datasheild.grievanceservice.entity.GrievanceActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GrievanceActivityRepository extends JpaRepository<GrievanceActivity, UUID> {

    @Query("SELECT ga FROM GrievanceActivity ga WHERE ga.grievanceId = :grievanceId ORDER BY ga.createdAt DESC")
    List<GrievanceActivity> findActivityByGrievanceId(@Param("grievanceId") UUID grievanceId);

    @Query("SELECT COUNT(ga) FROM GrievanceActivity ga WHERE ga.grievanceId = :grievanceId AND ga.activityType = :type")
    Long countActivitiesByType(@Param("grievanceId") UUID grievanceId, @Param("type") String type);
}
