package com.datasheild.siem.service;

import com.datasheild.siem.config.SiemProperties;
import com.datasheild.siem.entity.SiemAlert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SplunkConnectorServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private SplunkConnectorService service;

    @BeforeEach
    void setUp() {
        SiemProperties properties = new SiemProperties();
        properties.getSplunk().setHecUrl("http://splunk");
        properties.getSplunk().setHecToken("token");
        service = new SplunkConnectorService(properties, restTemplate);
    }

    @Test
    void shouldFormatCefPayload() {
        String payload = service.formatCEF(SiemAlert.builder().alertType("anomaly").severity("HIGH").message("body").build());
        assertThat(payload).contains("CEF:0|datasheild|platform").contains("anomaly");
    }

    @Test
    void shouldSwallowRestErrors() {
        doThrow(new RuntimeException("down")).when(restTemplate).postForEntity(eq("http://splunk/services/collector"), any(), eq(String.class));
        service.postEvent(SiemAlert.builder().alertType("anomaly").severity("HIGH").message("body").build());
        verify(restTemplate).postForEntity(eq("http://splunk/services/collector"), any(), eq(String.class));
    }
}
