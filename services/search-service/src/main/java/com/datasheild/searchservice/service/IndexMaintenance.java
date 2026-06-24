package com.datasheild.searchservice.service;

import com.datasheild.searchservice.repository.AuditLogIndexRepository;
import com.datasheild.searchservice.repository.SearchIndexRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndexMaintenance {

    private final SearchIndexRepository searchIndexRepository;
    private final AuditLogIndexRepository auditLogIndexRepository;
    private final ElasticsearchService elasticsearchService;

    @Value("${search.retention-days:90}")
    private long retentionDays;

    @Scheduled(cron = "${search.maintenance.cleanup-cron:0 0 2 * * *}")
    @Transactional
    public void cleanupExpiredIndexes() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(retentionDays));
        long deletedSearchDocs = searchIndexRepository.deleteByCreatedAtBefore(cutoff);
        long deletedAuditDocs = auditLogIndexRepository.deleteByCreatedAtBefore(cutoff);
        log.info("Search cleanup completed: searchDocs={}, auditDocs={}, cutoff={}", deletedSearchDocs, deletedAuditDocs, cutoff);
    }

    @Scheduled(cron = "${search.maintenance.rollover-cron:0 0 0 * * *}")
    public void rolloverMonthlyIndexes() {
        Instant since = Instant.now().minus(Duration.ofDays(31));
        searchIndexRepository.findDistinctTenantIdsSince(since)
                .forEach(tenantId -> elasticsearchService.ensureIndex(elasticsearchService.resolveIndexName(tenantId, Instant.now())));
    }
}
