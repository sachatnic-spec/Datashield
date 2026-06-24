package com.datasheild.siem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "siem")
public class SiemProperties {

    private Channel splunk = new Channel();
    private Channel qradar = new Channel();
    private Channel sentinel = new Channel();
    private String autoCreateThreshold = "CRITICAL";

    @Data
    public static class Channel {
        private String hecUrl = "http://localhost:8088";
        private String hecToken = "change-me";
    }
}
