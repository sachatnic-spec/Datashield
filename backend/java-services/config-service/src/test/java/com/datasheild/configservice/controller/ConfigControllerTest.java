package com.datasheild.configservice.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.datasheild.configservice.dto.TenantConfigRequest;
import com.datasheild.configservice.dto.TenantConfigResponse;
import com.datasheild.configservice.exception.GlobalExceptionHandler;
import com.datasheild.configservice.service.ConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ConfigController.class)
@Import(GlobalExceptionHandler.class)
class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConfigService configService;

    @Test
    void shouldReturnTenantConfig() throws Exception {
        when(configService.getConfig(eq("tenant-a"))).thenReturn(new TenantConfigResponse(
                UUID.randomUUID(),
                "tenant-a",
                "OPT_IN",
                365,
                "dpo@tenant-a.com",
                "ACTIVE",
                LocalDateTime.now(),
                LocalDateTime.now()));

        mockMvc.perform(get("/config/tenant-a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenant-a"))
                .andExpect(jsonPath("$.consentModel").value("OPT_IN"));
    }

    @Test
    void shouldReturnProblemDetailForInvalidPayload() throws Exception {
        TenantConfigRequest request = new TenantConfigRequest("", 0, "not-an-email");

        mockMvc.perform(post("/config/tenant-a")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }
}
