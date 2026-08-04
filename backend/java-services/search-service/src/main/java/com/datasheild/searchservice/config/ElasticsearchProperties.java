package com.datasheild.searchservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "search.elasticsearch")
public class ElasticsearchProperties {

    private boolean enabled = true;
    private String url = "http://localhost:9200";
    private int connectTimeoutMillis = 1000;
    private int socketTimeoutMillis = 2000;
}
