package com.datasheild.searchservice.service;

import com.datasheild.searchservice.entity.AuditLogIndex;
import com.datasheild.searchservice.entity.SearchIndex;
import com.datasheild.searchservice.exception.SearchOperationException;
import com.datasheild.searchservice.repository.AuditLogIndexRepository;
import com.datasheild.searchservice.repository.SearchIndexRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaAuditConsumer {

    private final AuditLogIndexRepository auditLogIndexRepository;
    private final SearchIndexRepository searchIndexRepository;
    private final ElasticsearchService elasticsearchService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${search.kafka.audit-topic:audit.entry.created}",
            groupId = "${spring.kafka.consumer.group-id}",
            autoStartup = "${search.kafka.enabled:true}")
    @Transactional
    public void consume(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            Instant eventTime = parseInstant(root.path("createdAt").asText(null));
            String tenantId = text(root, "tenantId", "default");
            String eventId = text(root, "eventId", UUID.randomUUID().toString());
            String eventType = text(root, "eventType", "audit.entry.created");
            BigDecimal complianceScore = root.hasNonNull("complianceScore")
                    ? new BigDecimal(root.path("complianceScore").asText())
                    : null;
            String indexName = elasticsearchService.resolveIndexName(tenantId, eventTime);

            AuditLogIndex auditLog = AuditLogIndex.builder()
                    .tenantId(tenantId)
                    .eventId(eventId)
                    .indexName(indexName)
                    .eventType(eventType)
                    .status("ACTIVE")
                    .complianceScore(complianceScore)
                    .payloadJson(payload)
                    .build();
            auditLogIndexRepository.save(auditLog);

            SearchIndex searchIndex = SearchIndex.builder()
                    .tenantId(tenantId)
                    .indexName(indexName)
                    .documentId(eventId)
                    .status("ACTIVE")
                    .documentType("audit_log")
                    .complianceScore(complianceScore)
                    .eventTimestamp(eventTime)
                    .payloadJson(payload)
                    .build();
            searchIndexRepository.save(searchIndex);
            elasticsearchService.indexDocument(searchIndex);
        } catch (Exception ex) {
            log.error("Unable to consume audit entry", ex);
            throw new SearchOperationException("Unable to consume audit entry", ex);
        }
    }

    private Instant parseInstant(String value) {
        try {
            return value == null ? Instant.now() : Instant.parse(value);
        } catch (Exception ex) {
            return Instant.now();
        }
    }

    private String text(JsonNode root, String field, String defaultValue) {
        return root.hasNonNull(field) ? root.path(field).asText() : defaultValue;
    }
}
