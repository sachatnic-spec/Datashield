package com.datasheild.searchservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.datasheild.searchservice.dto.SearchQueryRequest;
import com.datasheild.searchservice.dto.SearchQueryResponse;
import com.datasheild.searchservice.exception.GlobalExceptionHandler;
import com.datasheild.searchservice.service.SearchOrchestrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SearchController.class)
@Import(GlobalExceptionHandler.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SearchOrchestrationService searchOrchestrationService;

    @Test
    void shouldSubmitSearchQuery() throws Exception {
        UUID queryId = UUID.randomUUID();
        SearchQueryResponse response = new SearchQueryResponse(queryId, "COMPLETED", 1, false,
                List.of(Map.of("eventId", "evt-1")));
        when(searchOrchestrationService.submitQuery(any())).thenReturn(response);

        SearchQueryRequest request = new SearchQueryRequest("tenant-a", null, "consent", null, null, 0, 10);

        mockMvc.perform(post("/search/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryId").value(queryId.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.results[0].eventId").value("evt-1"));
    }

    @Test
    void shouldRejectInvalidSearchQuery() throws Exception {
        SearchQueryRequest request = new SearchQueryRequest("", null, "consent", null, null, 0, 10);

        mockMvc.perform(post("/search/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"));
    }

    @Test
    void shouldRequireTenantIdForAggregation() throws Exception {
        mockMvc.perform(get("/search/agg/event-counts"))
                .andExpect(status().isBadRequest());
    }
}
