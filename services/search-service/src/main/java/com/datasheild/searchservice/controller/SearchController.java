package com.datasheild.searchservice.controller;

import com.datasheild.searchservice.dto.AggregationResponse;
import com.datasheild.searchservice.dto.ApiMessageResponse;
import com.datasheild.searchservice.dto.ReindexRequest;
import com.datasheild.searchservice.dto.SearchQueryRequest;
import com.datasheild.searchservice.dto.SearchQueryResponse;
import com.datasheild.searchservice.dto.SearchResultResponse;
import com.datasheild.searchservice.service.SearchOrchestrationService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchOrchestrationService searchOrchestrationService;

    @PostMapping("/query")
    public ResponseEntity<SearchQueryResponse> submitQuery(@Valid @RequestBody SearchQueryRequest request) {
        return ResponseEntity.ok(searchOrchestrationService.submitQuery(request));
    }

    @GetMapping("/results/{queryId}")
    public ResponseEntity<SearchResultResponse> getResults(@PathVariable UUID queryId) {
        return ResponseEntity.ok(searchOrchestrationService.getResults(queryId));
    }

    @GetMapping("/agg/{metricType}")
    public ResponseEntity<AggregationResponse> aggregate(@PathVariable String metricType, @RequestParam String tenantId) {
        return ResponseEntity.ok(searchOrchestrationService.aggregate(tenantId, metricType));
    }

    @DeleteMapping("/indexes/{indexName}")
    public ResponseEntity<ApiMessageResponse> deleteIndex(@PathVariable String indexName) {
        return ResponseEntity.ok(searchOrchestrationService.deleteIndex(indexName));
    }

    @PostMapping("/reindex")
    public ResponseEntity<ApiMessageResponse> reindex(@Valid @RequestBody ReindexRequest request) {
        return ResponseEntity.ok(searchOrchestrationService.reindex(request));
    }
}
