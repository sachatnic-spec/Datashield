package com.datasheild.searchservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.datasheild.searchservice.dto.AggregationResponse;
import com.datasheild.searchservice.dto.SearchExecutionResult;
import com.datasheild.searchservice.dto.SearchQueryRequest;
import com.datasheild.searchservice.dto.SearchQueryResponse;
import com.datasheild.searchservice.entity.SearchQuery;
import com.datasheild.searchservice.repository.AuditLogIndexRepository;
import com.datasheild.searchservice.repository.SearchIndexRepository;
import com.datasheild.searchservice.repository.SearchQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchOrchestrationServiceTest {

    @Mock
    private SearchQueryRepository searchQueryRepository;

    @Mock
    private SearchIndexRepository searchIndexRepository;

    @Mock
    private AuditLogIndexRepository auditLogIndexRepository;

    @Mock
    private ElasticsearchService elasticsearchService;

    private SearchOrchestrationService searchOrchestrationService;

    @BeforeEach
    void setUp() {
        searchOrchestrationService = new SearchOrchestrationService(
                searchQueryRepository,
                searchIndexRepository,
                auditLogIndexRepository,
                elasticsearchService,
                new ObjectMapper());
    }

    @Test
    void shouldPersistCompletedSearchQuery() {
        when(searchQueryRepository.save(any(SearchQuery.class))).thenAnswer(invocation -> {
            SearchQuery query = invocation.getArgument(0);
            if (query.getId() == null) {
                query.setId(UUID.randomUUID());
            }
            return query;
        });
        when(elasticsearchService.search(any(SearchQueryRequest.class))).thenReturn(
                new SearchExecutionResult(List.of(Map.of("eventId", "evt-1")), 1, true));

        SearchQueryResponse response = searchOrchestrationService.submitQuery(
                new SearchQueryRequest("tenant-a", null, "evt-1", null, null, 0, 10));

        assertThat(response.queryId()).isNotNull();
        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.degraded()).isTrue();
        assertThat(response.totalHits()).isEqualTo(1);
    }

    @Test
    void shouldAggregateComplianceScores() {
        when(elasticsearchService.isRemoteAvailable()).thenReturn(false);
        when(auditLogIndexRepository.averageComplianceScore("tenant-a")).thenReturn(new BigDecimal("91.50"));
        when(auditLogIndexRepository.complianceScoreRange("tenant-a"))
                .thenReturn(java.util.Collections.singletonList(new Object[]{new BigDecimal("75.00"), new BigDecimal("99.00")}));
        when(auditLogIndexRepository.countByTenantId("tenant-a")).thenReturn(4L);

        AggregationResponse response = searchOrchestrationService.aggregate("tenant-a", "compliance-scores");

        assertThat(response.degraded()).isTrue();
        assertThat(response.metrics()).containsEntry("eventsIndexed", 4L);
        assertThat(response.metrics()).containsEntry("average", new BigDecimal("91.50"));
    }
}
