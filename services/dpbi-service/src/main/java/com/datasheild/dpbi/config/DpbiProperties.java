package com.datasheild.dpbi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "dpbi")
public class DpbiProperties {

    private Api api = new Api();
    private S3 s3 = new S3();

    @Data
    public static class Api {
        private String baseUrl = "http://localhost:8080/api/dpbi";
    }

    @Data
    public static class S3 {
        private String bucketName = "datasheild-dpbi-documents";
    }
}
