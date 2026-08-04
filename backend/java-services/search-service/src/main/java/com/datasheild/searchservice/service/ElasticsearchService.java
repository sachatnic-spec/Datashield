package com.datasheild.searchservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.datasheild.searchservice.config.ElasticsearchProperties;
import com.datasheild.searchservice.dto.SearchExecutionResult;
import com.datasheild.searchservice.dto.SearchQueryRequest;
import com.datasheild.searchservice.entity.SearchIndex;
import com.datasheild.searchservice.repository.SearchIndexRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };
    private static final DateTimeFormatter INDEX_MONTH = DateTimeFormatter.ofPattern("yyyyMM");

    private final SearchIndexRepository searchIndexRepository;
    private final ObjectMapper objectMapper;
    private final ElasticsearchProperties properties;

    @Autowired(required = false)
    private ElasticsearchClient elasticsearchClient;

    public SearchExecutionResult search(SearchQueryRequest request) {
        if (properties.isEnabled() && elasticsearchClient != null) {
            try {
                return remoteSearch(request);
            } catch (Exception ex) {
                log.warn("Elasticsearch unavailable, using local persistence fallback: {}", ex.getMessage());
            }
        }
        return localSearch(request, true);
    }

    public void indexDocument(SearchIndex document) {
        if (!properties.isEnabled() || elasticsearchClient == null) {
            return;
        }
        try {
            ensureIndex(document.getIndexName());
            elasticsearchClient.index(request -> request
                    .index(document.getIndexName())
                    .id(document.getDocumentId())
                    .document(toDocumentMap(document)));
        } catch (Exception ex) {
            log.warn("Unable to index document {} remotely: {}", document.getDocumentId(), ex.getMessage());
        }
    }

    public void ensureIndex(String indexName) {
        if (!properties.isEnabled() || elasticsearchClient == null) {
            return;
        }
        try {
            boolean exists = elasticsearchClient.indices().exists(request -> request.index(indexName)).value();
            if (!exists) {
                elasticsearchClient.indices().create(request -> request.index(indexName));
            }
        } catch (Exception ex) {
            log.debug("Unable to ensure Elasticsearch index {}: {}", indexName, ex.getMessage());
        }
    }

    public boolean deleteIndex(String indexName) {
        if (!properties.isEnabled() || elasticsearchClient == null) {
            return false;
        }
        try {
            boolean exists = elasticsearchClient.indices().exists(request -> request.index(indexName)).value();
            if (exists) {
                elasticsearchClient.indices().delete(request -> request.index(indexName));
                return true;
            }
        } catch (Exception ex) {
            log.warn("Unable to delete Elasticsearch index {}: {}", indexName, ex.getMessage());
        }
        return false;
    }

    public void reindexDocuments(List<SearchIndex> documents, String targetIndexName) {
        ensureIndex(targetIndexName);
        documents.forEach(document -> indexDocument(document.toBuilder().indexName(targetIndexName).build()));
    }

    public boolean isRemoteAvailable() {
        if (!properties.isEnabled() || elasticsearchClient == null) {
            return false;
        }
        try {
            return elasticsearchClient.ping().value();
        } catch (Exception ex) {
            return false;
        }
    }

    public String resolveIndexName(String tenantId, Instant timestamp) {
        String sanitizedTenant = tenantId.toLowerCase().replaceAll("[^a-z0-9]+", "_");
        String month = INDEX_MONTH.format(YearMonth.from(timestamp.atZone(ZoneOffset.UTC)));
        return "audit_logs_" + sanitizedTenant + "_" + month;
    }

    private SearchExecutionResult remoteSearch(SearchQueryRequest request) throws IOException {
        SearchResponse<Map> response = elasticsearchClient.search(search -> search
                        .index(resolveIndexPattern(request.tenantId(), request.indexName()))
                        .from(request.resolvedPage() * request.resolvedSize())
                        .size(request.resolvedSize())
                        .query(query -> query.bool(bool -> {
                            bool.filter(filter -> filter.term(term -> term.field("tenantId").value(request.tenantId())));
                            if (StringUtils.hasText(request.searchTerm())) {
                                bool.must(must -> must.simpleQueryString(simple -> simple
                                        .query(request.searchTerm())
                                        .fields("payloadJson", "eventType", "documentType")));
                            }
                            if (request.filters() != null) {
                                request.filters().forEach((key, value) ->
                                        bool.filter(filter -> filter.term(term -> term.field(key).value(value))));
                            }
                            return bool;
                        })), Map.class);

        List<Map<String, Object>> results = response.hits().hits().stream().map(hit -> {
            Map<String, Object> result = hit.source() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(hit.source());
            result.putIfAbsent("_id", hit.id());
            result.putIfAbsent("_index", hit.index());
            return result;
        }).toList();

        long totalHits = response.hits().total() == null ? results.size() : response.hits().total().value();
        return new SearchExecutionResult(results, totalHits, false);
    }

    private SearchExecutionResult localSearch(SearchQueryRequest request, boolean degraded) {
        Page<SearchIndex> page = searchIndexRepository.search(
                request.tenantId(),
                StringUtils.hasText(request.indexName()) ? request.indexName() : null,
                StringUtils.hasText(request.searchTerm()) ? request.searchTerm() : null,
                PageRequest.of(request.resolvedPage(), request.resolvedSize()));

        List<Map<String, Object>> results = page.getContent().stream().map(this::toDocumentMap).toList();
        return new SearchExecutionResult(results, page.getTotalElements(), degraded);
    }

    private String resolveIndexPattern(String tenantId, String explicitIndexName) {
        return StringUtils.hasText(explicitIndexName) ? explicitIndexName : "audit_logs_" + tenantId.toLowerCase().replaceAll("[^a-z0-9]+", "_") + "_*";
    }

    private Map<String, Object> toDocumentMap(SearchIndex document) {
        Map<String, Object> payload = parseJson(document.getPayloadJson());
        payload.putIfAbsent("tenantId", document.getTenantId());
        payload.putIfAbsent("indexName", document.getIndexName());
        payload.putIfAbsent("documentId", document.getDocumentId());
        payload.putIfAbsent("documentType", document.getDocumentType());
        payload.putIfAbsent("status", document.getStatus());
        payload.putIfAbsent("payloadJson", document.getPayloadJson());
        payload.putIfAbsent("complianceScore", document.getComplianceScore());
        payload.putIfAbsent("eventTimestamp", document.getEventTimestamp());
        payload.putIfAbsent("createdAt", document.getCreatedAt());
        return payload;
    }

    private Map<String, Object> parseJson(String json) {
        if (!StringUtils.hasText(json)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception ex) {
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("payload", json);
            return fallback;
        }
    }
}
