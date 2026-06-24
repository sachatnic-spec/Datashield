package com.datasheild.searchservice.service;

import com.datasheild.searchservice.dto.AggregationResponse;
import com.datasheild.searchservice.dto.ApiMessageResponse;
import com.datasheild.searchservice.dto.ReindexRequest;
import com.datasheild.searchservice.dto.SearchExecutionResult;
import com.datasheild.searchservice.dto.SearchQueryRequest;
import com.datasheild.searchservice.dto.SearchQueryResponse;
import com.datasheild.searchservice.dto.SearchResultResponse;
import com.datasheild.searchservice.entity.SearchIndex;
import com.datasheild.searchservice.entity.SearchQuery;
import com.datasheild.searchservice.exception.ResourceNotFoundException;
import com.datasheild.searchservice.exception.SearchOperationException;
import com.datasheild.searchservice.repository.AuditLogIndexRepository;
import com.datasheild.searchservice.repository.SearchIndexRepository;
import com.datasheild.searchservice.repository.SearchQueryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SearchOrchestrationService {

    private static final TypeReference<List<Map<String, Object>>> LIST_TYPE = new TypeReference<>() { };

    private final SearchQueryRepository searchQueryRepository;
    private final SearchIndexRepository searchIndexRepository;
    private final AuditLogIndexRepository auditLogIndexRepository;
    private final ElasticsearchService elasticsearchService;
    private final ObjectMapper objectMapper;

    @Transactional
    public SearchQueryResponse submitQuery(SearchQueryRequest request) {
        SearchQuery query = SearchQuery.builder()
                .tenantId(request.tenantId())
                .status("RUNNING")
                .searchTerm(request.searchTerm())
                .metricType(request.metricType())
                .pageNumber(request.resolvedPage())
                .pageSize(request.resolvedSize())
                .requestJson(writeJson(request))
                .build();
        searchQueryRepository.save(query);

        try {
            SearchExecutionResult executionResult = elasticsearchService.search(request);
            query.setStatus("COMPLETED");
            query.setDegraded(executionResult.degraded());
            query.setTotalHits(executionResult.totalHits());
            query.setResultJson(writeJson(executionResult.results()));
            query.setCompletedAt(Instant.now());
            searchQueryRepository.save(query);
            return new SearchQueryResponse(query.getId(), query.getStatus(), query.getTotalHits(), query.isDegraded(), executionResult.results());
        } catch (Exception ex) {
            query.setStatus("FAILED");
            query.setCompletedAt(Instant.now());
            searchQueryRepository.save(query);
            throw new SearchOperationException("Unable to execute search query", ex);
        }
    }

    @Transactional(readOnly = true)
    public SearchResultResponse getResults(UUID queryId) {
        SearchQuery query = searchQueryRepository.findById(queryId)
                .orElseThrow(() -> new ResourceNotFoundException("Search query not found: " + queryId));
        return new SearchResultResponse(
                query.getId(),
                query.getTenantId(),
                query.getStatus(),
                query.isDegraded(),
                query.getTotalHits(),
                query.getCreatedAt(),
                query.getCompletedAt(),
                readResults(query.getResultJson()));
    }

    @Transactional(readOnly = true)
    public AggregationResponse aggregate(String tenantId, String metricType) {
        if (!StringUtils.hasText(tenantId)) {
            throw new IllegalArgumentException("tenantId is required");
        }

        String normalizedMetric = metricType.toLowerCase(Locale.ROOT);
        boolean degraded = !elasticsearchService.isRemoteAvailable();
        Map<String, Object> metrics = new LinkedHashMap<>();

        if (normalizedMetric.contains("compliance")) {
            List<Object[]> rangeRows = auditLogIndexRepository.complianceScoreRange(tenantId);
            Object[] range = rangeRows.isEmpty() ? new Object[]{BigDecimal.ZERO, BigDecimal.ZERO} : rangeRows.get(0);
            metrics.put("average", auditLogIndexRepository.averageComplianceScore(tenantId));
            metrics.put("minimum", range[0]);
            metrics.put("maximum", range[1]);
            metrics.put("eventsIndexed", auditLogIndexRepository.countByTenantId(tenantId));
            return new AggregationResponse(metricType, tenantId, degraded, metrics);
        }

        if (normalizedMetric.contains("event")) {
            Map<String, Object> counts = new LinkedHashMap<>();
            auditLogIndexRepository.countByEventType(tenantId)
                    .forEach(row -> counts.put(String.valueOf(row[0]), row[1]));
            metrics.put("counts", counts);
            metrics.put("total", auditLogIndexRepository.countByTenantId(tenantId));
            return new AggregationResponse(metricType, tenantId, degraded, metrics);
        }

        throw new IllegalArgumentException("Unsupported metricType: " + metricType);
    }

    @Transactional
    public ApiMessageResponse deleteIndex(String indexName) {
        if (!StringUtils.hasText(indexName)) {
            throw new IllegalArgumentException("indexName is required");
        }
        long deleted = searchIndexRepository.deleteByIndexName(indexName);
        boolean remoteDeleted = elasticsearchService.deleteIndex(indexName);
        return new ApiMessageResponse("Index deletion completed", Map.of(
                "indexName", indexName,
                "localDocumentsDeleted", deleted,
                "remoteDeleted", remoteDeleted));
    }

    @Transactional
    public ApiMessageResponse reindex(ReindexRequest request) {
        List<SearchIndex> existing = searchIndexRepository.findByTenantIdAndIndexName(request.tenantId(), request.sourceIndexName());
        if (existing.isEmpty()) {
            throw new ResourceNotFoundException("No local documents found for index: " + request.sourceIndexName());
        }

        List<SearchIndex> copies = existing.stream().map(document -> SearchIndex.builder()
                .tenantId(document.getTenantId())
                .indexName(request.targetIndexName())
                .documentId(document.getDocumentId())
                .status(document.getStatus())
                .documentType(document.getDocumentType())
                .complianceScore(document.getComplianceScore())
                .eventTimestamp(document.getEventTimestamp())
                .payloadJson(document.getPayloadJson())
                .build()).toList();

        searchIndexRepository.saveAll(copies);
        elasticsearchService.reindexDocuments(copies, request.targetIndexName());
        return new ApiMessageResponse("Reindex completed", Map.of(
                "tenantId", request.tenantId(),
                "sourceIndexName", request.sourceIndexName(),
                "targetIndexName", request.targetIndexName(),
                "documentsReindexed", copies.size()));
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new SearchOperationException("Unable to serialize search payload", ex);
        }
    }

    private List<Map<String, Object>> readResults(String resultJson) {
        if (!StringUtils.hasText(resultJson)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(resultJson, LIST_TYPE);
        } catch (Exception ex) {
            throw new SearchOperationException("Unable to read persisted search results", ex);
        }
    }
}
