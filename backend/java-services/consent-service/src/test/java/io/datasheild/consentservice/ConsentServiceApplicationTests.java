package io.datasheild.consentservice;

import io.datasheild.consentservice.dto.GrantConsentRequest;
import io.datasheild.consentservice.entity.ConsentRecord;
import io.datasheild.consentservice.repository.ConsentRecordRepository;
import io.datasheild.consentservice.service.ConsentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class ConsentServiceApplicationTests {

    @Autowired
    private ConsentService consentService;

    @MockBean
    private ConsentRecordRepository consentRecordRepository;

    private UUID tenantId;
    private UUID dataPrincipalId;
    private UUID purposeId;

    @BeforeEach
    public void setUp() {
        tenantId = UUID.randomUUID();
        dataPrincipalId = UUID.randomUUID();
        purposeId = UUID.randomUUID();
    }

    @Test
    public void testGrantConsentSuccess() {
        GrantConsentRequest request = GrantConsentRequest.builder()
                .dataPrincipalId(dataPrincipalId)
                .purposeId(purposeId)
                .ipAddress("127.0.0.1")
                .channel("WEB")
                .build();

        assertNotNull(request);
        assertTrue(request.isValid());
    }

    @Test
    public void testGrantConsentValidation() {
        GrantConsentRequest request = GrantConsentRequest.builder()
                .dataPrincipalId(null)
                .purposeId(purposeId)
                .channel("WEB")
                .build();

        assertFalse(request.isValid());
    }
}
