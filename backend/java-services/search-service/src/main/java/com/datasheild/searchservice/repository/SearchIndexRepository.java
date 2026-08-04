package com.datasheild.searchservice.repository;

import com.datasheild.searchservice.entity.SearchIndex;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchIndexRepository extends JpaRepository<SearchIndex, UUID> {

    @Query("""
            select s from SearchIndex s
            where s.tenantId = :tenantId
              and s.status <> 'DELETED'
              and (:indexName is null or s.indexName = :indexName)
              and (:searchTerm is null or s.payloadJson like concat('%', :searchTerm, '%'))
            order by s.createdAt desc
            """)
    Page<SearchIndex> search(@Param("tenantId") String tenantId,
                             @Param("indexName") String indexName,
                             @Param("searchTerm") String searchTerm,
                             Pageable pageable);

    List<SearchIndex> findByTenantIdAndIndexName(String tenantId, String indexName);

    long deleteByIndexName(String indexName);

    long deleteByCreatedAtBefore(Instant cutoff);

    @Query("select distinct s.tenantId from SearchIndex s where s.createdAt >= :cutoff")
    List<String> findDistinctTenantIdsSince(@Param("cutoff") Instant cutoff);
}
