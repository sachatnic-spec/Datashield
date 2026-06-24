package com.datasheild.siem.service;

import com.datasheild.siem.config.SiemProperties;
import com.datasheild.siem.entity.SiemAlert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SplunkConnectorService {

    private final SiemProperties properties;
    private final RestTemplate restTemplate;

    public void postEvent(SiemAlert alert) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Splunk " + properties.getSplunk().getHecToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            restTemplate.postForEntity(properties.getSplunk().getHecUrl() + "/services/collector",
                    new HttpEntity<>(formatCEF(alert), headers), String.class);
        } catch (Exception ex) {
            log.error("Splunk post failed", ex);
        }
    }

    String formatCEF(SiemAlert alert) {
        return "CEF:0|datasheild|platform|1.0|" + alert.getAlertType() + "|" + alert.getSeverity() + "|10|msg=" + alert.getMessage();
    }
}
