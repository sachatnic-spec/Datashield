package io.datasheild.rightsservice;

import io.datasheild.rightsservice.dto.CreateDPRRequest;
import io.datasheild.rightsservice.entity.DPRRequest;
import io.datasheild.rightsservice.service.DPRService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RightsServiceApplicationTests {

    @Autowired
    private DPRService dprService;

    private UUID tenantId;
    private UUID dataPrincipalId;

    @BeforeEach
    public void setUp() {
        tenantId = UUID.randomUUID();
        dataPrincipalId = UUID.randomUUID();
    }

    @Test
    public void testCreateDPRRequestValidation() {
        CreateDPRRequest request = CreateDPRRequest.builder()
                .requestType(DPRRequest.DPRType.ACCESS)
                .channel("WEB")
                .requestDetails("I want access to my personal data")
                .build();

        assertTrue(request.isValid());
    }

    @Test
    public void testCreateDPRRequestInvalidation() {
        CreateDPRRequest request = CreateDPRRequest.builder()
                .requestType(null)
                .channel("WEB")
                .requestDetails("Request details")
                .build();

        assertFalse(request.isValid());
    }

    @Test
    public void testDPRRequestSLA() {
        DPRRequest request = DPRRequest.builder()
                .tenantId(tenantId)
                .dataPrincipalId(dataPrincipalId)
                .requestType(DPRRequest.DPRType.ACCESS)
                .channel("WEB")
                .build();

        assertNotNull(request.getSlaDeadline());
        assertEquals(DPRRequest.DPRStatus.RECEIVED, request.getStatus());
    }
}
