package com.datasheild.aianalysis.repository;

import com.datasheild.aianalysis.entity.TrendForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrendForecastRepository extends JpaRepository<TrendForecast, UUID> {
    
    @Query("SELECT t FROM TrendForecast t WHERE t.tenantId = :tenantId ORDER BY t.generatedAt DESC")
    List<TrendForecast> findByTenantId(UUID tenantId);
    
    @Query("SELECT t FROM TrendForecast t WHERE t.tenantId = :tenantId AND t.metricType = :metricType ORDER BY t.generatedAt DESC")
    List<TrendForecast> findByTenantIdAndMetricType(UUID tenantId, TrendForecast.MetricType metricType);
}
