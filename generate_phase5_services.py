from pathlib import Path
from textwrap import dedent

files = {}

def add(path: str, content: str):
    files[path] = dedent(content).strip() + "\n"

add(r"D:\Development Practice\Datasheild\services\pii-detection-service\src\main\java\com\datasheild\piidetection\PIIDetectionServiceApplication.java", '''
package com.datasheild.piidetection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PIIDetectionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PIIDetectionServiceApplication.class, args);
    }
}
''')

add(r"D:\Development Practice\Datasheild\services\pii-detection-service\src\main\java\com\datasheild\piidetection\config\OpenAPIConfig.java", '''
package com.datasheild.piidetection.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("DataShield PII Detection Service")
                .version("1.0.0")
                .description("PII detection, confidence scoring, and redaction APIs"));
    }
}
''')

add(r"D:\Development Practice\Datasheild\services\pii-detection-service\src\main\java\com\datasheild\piidetection\dto\DetectionRequest.java", '''
package com.datasheild.piidetection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class DetectionRequest {
    @NotNull
    private UUID tenantId;

    @NotBlank
    private String text;

    private String context;
}
''')

add(r"D:\Development Practice\Datasheild\services\pii-detection-service\src\main\java\com\datasheild\piidetection\dto\RedactionRequest.java", '''
package com.datasheild.piidetection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RedactionRequest {
    @NotBlank
    private String text;
}
''')

add(r"D:\Development Practice\Datasheild\services\pii-detection-service\src\main\java\com\datasheild\piidetection\dto\BulkDetectionRequest.java", '''
package com.datasheild.piidetection.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BulkDetectionRequest {
    @NotNull
    private UUID tenantId;

    @NotEmpty
    private List<String> texts;

    private String context;
}
''')

add(r"D:\Development Practice\Datasheild\services\pii-detection-service\src\main\java\com\datasheild\piidetection\repository\PIIDetectionResultRepository.java", '''
package com.datasheild.piidetection.repository;

import com.datasheild.piidetection.entity.PIIDetectionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PIIDetectionResultRepository extends JpaRepository<PIIDetectionResult, UUID> {

    @Query("SELECT p FROM PIIDetectionResult p WHERE p.tenantId = :tenantId ORDER BY p.detectedAt DESC")
    List<PIIDetectionResult> findByTenantId(UUID tenantId);

    @Query("SELECT p FROM PIIDetectionResult p WHERE p.confidenceScore >= :confidenceScore ORDER BY p.confidenceScore DESC, p.detectedAt DESC")
    List<PIIDetectionResult> findByConfidenceScore(Double confidenceScore);
}
''')

add(r"D:\Development Practice\Datasheild\services\pii-detection-service\src\main\java\com\datasheild\piidetection\service\PIIDetectionService.java", '''
package com.datasheild.piidetection.service;

import com.datasheild.piidetection.entity.PIIDetectionResult;
import com.datasheild.piidetection.repository.PIIDetectionResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class PIIDetectionService {
    private static final List<PatternRule> PATTERN_RULES = List.of(
        new PatternRule(PIIDetectionResult.PIICategory.AADHAAR, Pattern.compile("\\b\\d{4}\\s?\\d{4}\\s?\\d{4}\\b"), 0.95, PIIDetectionResult.DetectionSource.HYBRID),
        new PatternRule(PIIDetectionResult.PIICategory.PAN, Pattern.compile("\\b[A-Z]{5}\\d{4}[A-Z]\\b"), 0.94, PIIDetectionResult.DetectionSource.HYBRID),
        new PatternRule(PIIDetectionResult.PIICategory.CREDIT_CARD, Pattern.compile("\\b(?:\\d{4}[-\\s]?){3}\\d{4}\\b"), 0.92, PIIDetectionResult.DetectionSource.HYBRID),
        new PatternRule(PIIDetectionResult.PIICategory.EMAIL, Pattern.compile("\\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[A-Za-z]{2,}\\b"), 0.88, PIIDetectionResult.DetectionSource.REGEX_PATTERN),
        new PatternRule(PIIDetectionResult.PIICategory.PHONE, Pattern.compile("\\b(?:\\+91[-\\s]?)?[6-9]\\d{9}\\b"), 0.84, PIIDetectionResult.DetectionSource.REGEX_PATTERN),
        new PatternRule(PIIDetectionResult.PIICategory.PASSPORT, Pattern.compile("\\b[A-PR-WYa-pr-wy][1-9]\\d\\s?\\d{4}[1-9]\\b"), 0.90, PIIDetectionResult.DetectionSource.HYBRID),
        new PatternRule(PIIDetectionResult.PIICategory.DOB, Pattern.compile("\\b(?:0?[1-9]|[12][0-9]|3[01])[-/](?:0?[1-9]|1[0-2])[-/](?:19|20)\\d{2}\\b|\\b(?:19|20)\\d{2}[-/](?:0?[1-9]|1[0-2])[-/](?:0?[1-9]|[12][0-9]|3[01])\\b"), 0.82, PIIDetectionResult.DetectionSource.HYBRID),
        new PatternRule(PIIDetectionResult.PIICategory.BANK_ACCOUNT, Pattern.compile("\\b\\d{9,18}\\b"), 0.79, PIIDetectionResult.DetectionSource.ML_MODEL)
    );
    private static final Pattern NAME_PATTERN = Pattern.compile("\\b(?:Mr|Mrs|Ms|Dr)?\\.?\\s?[A-Z][a-z]+(?:\\s+[A-Z][a-z]+){1,2}\\b");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("\\b\\d{1,4},?\\s+[A-Za-z0-9\\s,.-]{8,}(Street|St|Road|Rd|Avenue|Ave|Lane|Ln|Nagar|Colony|Sector)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern MEDICAL_PATTERN = Pattern.compile("(?i)\\b(patient|diagnosis|prescription|medication|blood group|medical record)\\b");

    private final PIIDetectionResultRepository repository;

    public PIIDetectionResult detectPII(String text, String context, UUID tenantId) {
        String normalizedText = requireText(text);
        DetectionCandidate candidate = findBestCandidate(normalizedText, context);

        PIIDetectionResult result = PIIDetectionResult.builder()
            .tenantId(tenantId)
            .inputText(normalizedText)
            .context(context)
            .piiType(candidate.category())
            .confidenceScore(candidate.confidence())
            .redactedText(redactPII(normalizedText))
            .detectionSource(candidate.source())
            .requiresHumanReview(candidate.confidence() < 0.82 || candidate.category() == PIIDetectionResult.PIICategory.UNKNOWN)
            .build();

        return repository.save(result);
    }

    public String redactPII(String text) {
        String redacted = requireText(text);
        redacted = applyMask(redacted, Pattern.compile("\\b\\d{4}\\s?\\d{4}\\s?\\d{4}\\b"), value -> "XXXX XXXX " + digitsOnly(value).substring(Math.max(0, digitsOnly(value).length() - 4)));
        redacted = applyMask(redacted, Pattern.compile("\\b[A-Z]{5}\\d{4}[A-Z]\\b"), value -> value.substring(0, 2) + "******" + value.substring(value.length() - 2));
        redacted = applyMask(redacted, Pattern.compile("\\b(?:\\d{4}[-\\s]?){3}\\d{4}\\b"), value -> "XXXX-XXXX-XXXX-" + digitsOnly(value).substring(Math.max(0, digitsOnly(value).length() - 4)));
        redacted = applyMask(redacted, Pattern.compile("\\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[A-Za-z]{2,}\\b"), this::maskEmail);
        redacted = applyMask(redacted, Pattern.compile("\\b(?:\\+91[-\\s]?)?[6-9]\\d{9}\\b"), value -> maskPreservingLast(value, 4));
        redacted = applyMask(redacted, Pattern.compile("\\b[A-PR-WYa-pr-wy][1-9]\\d\\s?\\d{4}[1-9]\\b"), value -> maskPreservingLast(value, 3));
        redacted = applyMask(redacted, Pattern.compile("\\b(?:0?[1-9]|[12][0-9]|3[01])[-/](?:0?[1-9]|1[0-2])[-/](?:19|20)\\d{2}\\b|\\b(?:19|20)\\d{2}[-/](?:0?[1-9]|1[0-2])[-/](?:0?[1-9]|[12][0-9]|3[01])\\b"), value -> "**/**/****");
        return redacted;
    }

    public List<PIIDetectionResult> bulkDetect(List<String> texts, String context, UUID tenantId) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("At least one text sample is required");
        }

        List<PIIDetectionResult> results = new ArrayList<>();
        for (String text : texts) {
            results.add(detectPII(text, context, tenantId));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public List<PIIDetectionResult> getResults(UUID tenantId, Double minConfidence) {
        if (minConfidence != null) {
            return repository.findByConfidenceScore(minConfidence).stream()
                .filter(result -> tenantId.equals(result.getTenantId()))
                .toList();
        }
        return repository.findByTenantId(tenantId);
    }

    private DetectionCandidate findBestCandidate(String text, String context) {
        List<DetectionCandidate> matches = new ArrayList<>();
        for (PatternRule rule : PATTERN_RULES) {
            Matcher matcher = rule.pattern().matcher(text);
            if (matcher.find()) {
                String matchedValue = matcher.group();
                double confidence = adjustConfidence(rule.baseConfidence(), rule.category(), matchedValue, context);
                PIIDetectionResult.DetectionSource source = confidence > rule.baseConfidence() ? PIIDetectionResult.DetectionSource.HYBRID : rule.source();
                matches.add(new DetectionCandidate(rule.category(), confidence, rule.source()));
            }
        }

        if (NAME_PATTERN.matcher(text).find() && containsAny(context, "customer", "employee", "owner", "nominee", "beneficiary")) {
            matches.add(new DetectionCandidate(PIIDetectionResult.PIICategory.NAME, 0.76, PIIDetectionResult.DetectionSource.ML_MODEL));
        }
        if (ADDRESS_PATTERN.matcher(text).find()) {
            matches.add(new DetectionCandidate(PIIDetectionResult.PIICategory.ADDRESS, 0.80, PIIDetectionResult.DetectionSource.HYBRID));
        }
        if (MEDICAL_PATTERN.matcher(text).find()) {
            matches.add(new DetectionCandidate(PIIDetectionResult.PIICategory.MEDICAL, 0.78, PIIDetectionResult.DetectionSource.ML_MODEL));
        }

        return matches.stream()
            .max(Comparator.comparingDouble(DetectionCandidate::confidence))
            .orElseGet(() -> new DetectionCandidate(PIIDetectionResult.PIICategory.UNKNOWN, 0.70, PIIDetectionResult.DetectionSource.ML_MODEL));
    }

    private double adjustConfidence(double baseConfidence, PIIDetectionResult.PIICategory category, String matchedValue, String context) {
        double confidence = baseConfidence;
        String combined = (matchedValue + " " + (context == null ? "" : context)).toLowerCase(Locale.ROOT);

        if (containsAny(combined, "kyc", "identity", "government", "account", "customer", "personal")) {
            confidence += 0.02;
        }
        if (category == PIIDetectionResult.PIICategory.CREDIT_CARD && containsAny(combined, "payment", "cvv", "card")) {
            confidence += 0.02;
        }
        if (category == PIIDetectionResult.PIICategory.BANK_ACCOUNT && containsAny(combined, "ifsc", "upi", "bank", "beneficiary")) {
            confidence += 0.05;
        }
        if (category == PIIDetectionResult.PIICategory.EMAIL && matchedValue.endsWith(".gov.in")) {
            confidence += 0.02;
        }
        return Math.max(0.70, Math.min(0.95, confidence));
    }

    private boolean containsAny(String value, String... keywords) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (normalized.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String applyMask(String text, Pattern pattern, Function<String, String> maskFunction) {
        Matcher matcher = pattern.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(maskFunction.apply(matcher.group())));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String maskEmail(String value) {
        int atIndex = value.indexOf('@');
        if (atIndex <= 1) {
            return "***" + value.substring(Math.max(0, atIndex));
        }
        return value.charAt(0) + "***" + value.substring(atIndex - 1);
    }

    private String maskPreservingLast(String value, int visibleCharacters) {
        String digits = digitsOnly(value);
        if (digits.length() <= visibleCharacters) {
            return "*".repeat(Math.max(4, digits.length()));
        }
        return "*".repeat(Math.max(0, digits.length() - visibleCharacters)) + digits.substring(digits.length() - visibleCharacters);
    }

    private String digitsOnly(String value) {
        return value.replaceAll("\\D", "");
    }

    private String requireText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text must not be blank");
        }
        return text.trim();
    }

    private record PatternRule(PIIDetectionResult.PIICategory category, Pattern pattern, double baseConfidence,
                               PIIDetectionResult.DetectionSource source) {
    }

    private record DetectionCandidate(PIIDetectionResult.PIICategory category, double confidence,
                                      PIIDetectionResult.DetectionSource source) {
    }
}
''')

add(r"D:\Development Practice\Datasheild\services\pii-detection-service\src\main\java\com\datasheild\piidetection\controller\PIIDetectionController.java", '''
package com.datasheild.piidetection.controller;

import com.datasheild.piidetection.dto.BulkDetectionRequest;
import com.datasheild.piidetection.dto.DetectionRequest;
import com.datasheild.piidetection.dto.RedactionRequest;
import com.datasheild.piidetection.entity.PIIDetectionResult;
import com.datasheild.piidetection.service.PIIDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pii-detection")
@RequiredArgsConstructor
@Tag(name = "PII Detection", description = "PII detection and redaction endpoints")
public class PIIDetectionController {
    private final PIIDetectionService piiDetectionService;

    @PostMapping("/detect")
    @Operation(summary = "Detect the most likely PII category in a text payload")
    public ResponseEntity<PIIDetectionResult> detect(@Valid @RequestBody DetectionRequest request) {
        return ResponseEntity.ok(piiDetectionService.detectPII(request.getText(), request.getContext(), request.getTenantId()));
    }

    @PostMapping("/redact")
    @Operation(summary = "Redact sensitive values from text")
    public ResponseEntity<Map<String, String>> redact(@Valid @RequestBody RedactionRequest request) {
        return ResponseEntity.ok(Map.of(
            "originalText", request.getText(),
            "redactedText", piiDetectionService.redactPII(request.getText())
        ));
    }

    @PostMapping("/bulk-detect")
    @Operation(summary = "Run batch PII detection for multiple text samples")
    public ResponseEntity<List<PIIDetectionResult>> bulkDetect(@Valid @RequestBody BulkDetectionRequest request) {
        return ResponseEntity.ok(piiDetectionService.bulkDetect(request.getTexts(), request.getContext(), request.getTenantId()));
    }

    @GetMapping("/results/{tenantId}")
    @Operation(summary = "Fetch stored detection results for a tenant")
    public ResponseEntity<List<PIIDetectionResult>> getResults(@PathVariable UUID tenantId,
                                                               @RequestParam(required = false) Double minConfidence) {
        return ResponseEntity.ok(piiDetectionService.getResults(tenantId, minConfidence));
    }
}
''')

add(r"D:\Development Practice\Datasheild\services\pii-detection-service\src\main\resources\application.yml", '''
spring:
  application:
    name: pii-detection-service
  datasource:
    url: jdbc:postgresql://localhost:5432/datasheild
    username: datasheild_user
    password: secure_password
    hikari:
      maximum-pool-size: 15
      minimum-idle: 3
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        default_schema: pii_detection
        hbm2ddl.create_namespaces: true

server:
  port: 8019
  servlet:
    context-path: /

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics

logging:
  level:
    root: INFO
    com.datasheild: DEBUG
''')

# Risk scoring files
add(r"D:\Development Practice\Datasheild\services\risk-scoring-service\pom.xml", '''
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.4</version>
        <relativePath/>
    </parent>

    <groupId>com.datasheild</groupId>
    <artifactId>risk-scoring-service</artifactId>
    <version>1.0.0</version>
    <name>DataShield Risk Scoring Service</name>
    <description>Vendor risk scoring and trend analysis service</description>

    <properties>
        <java.version>17</java.version>
        <springdoc-openapi-starter-webmvc-ui.version>2.2.0</springdoc-openapi-starter-webmvc-ui.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${springdoc-openapi-starter-webmvc-ui.version}</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
''')

add(r"D:\Development Practice\Datasheild\services\risk-scoring-service\src\main\java\com\datasheild\riskscoring\RiskScoringServiceApplication.java", '''
package com.datasheild.riskscoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RiskScoringServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RiskScoringServiceApplication.class, args);
    }
}
''')

add(r"D:\Development Practice\Datasheild\services\risk-scoring-service\src\main\java\com\datasheild\riskscoring\config\OpenAPIConfig.java", '''
package com.datasheild.riskscoring.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("DataShield Risk Scoring Service")
                .version("1.0.0")
                .description("Vendor risk scoring, ranking, and trend prediction APIs"));
    }
}
''')

add(r"D:\Development Practice\Datasheild\services\risk-scoring-service\src\main\java\com\datasheild\riskscoring\entity\RiskScore.java", '''
package com.datasheild.riskscoring.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "risk_score", schema = "risk_scoring", indexes = {
    @Index(name = "idx_risk_tenant_score", columnList = "tenant_id,overall_risk_score DESC"),
    @Index(name = "idx_risk_vendor_scored", columnList = "vendor_id,scored_at DESC")
})
public class RiskScore {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID vendorId;

    @Column(nullable = false)
    private Double securityFactor;

    @Column(nullable = false)
    private Double complianceFactor;

    @Column(nullable = false)
    private Double operationalFactor;

    @Column(nullable = false)
    private Double historicalFactor;

    @Column(nullable = false)
    private Double overallRiskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TrendDirection trendDirection;

    @Column(columnDefinition = "TEXT")
    private String rationale;

    @Column(columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(nullable = false)
    private LocalDateTime scoredAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum TrendDirection {
        FALLING, STABLE, RISING
    }
}
''')

add(r"D:\Development Practice\Datasheild\services\risk-scoring-service\src\main\java\com\datasheild\riskscoring\repository\RiskScoreRepository.java", '''
package com.datasheild.riskscoring.repository;

import com.datasheild.riskscoring.entity.RiskScore;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RiskScoreRepository extends JpaRepository<RiskScore, UUID> {

    @Query("SELECT r FROM RiskScore r WHERE r.vendorId = :vendorId ORDER BY r.scoredAt ASC")
    List<RiskScore> findByVendorId(UUID vendorId);

    @Query("SELECT r FROM RiskScore r WHERE r.tenantId = :tenantId ORDER BY r.overallRiskScore DESC, r.scoredAt DESC")
    List<RiskScore> findByTenantId(UUID tenantId);

    @Query("SELECT r FROM RiskScore r WHERE r.tenantId = :tenantId ORDER BY r.overallRiskScore DESC, r.scoredAt DESC")
    List<RiskScore> findTopRisks(UUID tenantId, Pageable pageable);

    Optional<RiskScore> findTopByVendorIdOrderByScoredAtDesc(UUID vendorId);
}
''')

add(r"D:\Development Practice\Datasheild\services\risk-scoring-service\src\main\java\com\datasheild\riskscoring\dto\RiskScoreRequest.java", '''
package com.datasheild.riskscoring.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RiskScoreRequest {
    @NotNull
    private UUID tenantId;

    @NotNull
    private UUID vendorId;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double security;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double compliance;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double operational;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double historical;
}
''')

add(r"D:\Development Practice\Datasheild\services\risk-scoring-service\src\main\java\com\datasheild\riskscoring\dto\VendorRiskResponse.java", '''
package com.datasheild.riskscoring.dto;

import com.datasheild.riskscoring.entity.RiskScore;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class VendorRiskResponse {
    private UUID tenantId;
    private UUID vendorId;
    private Double overallRiskScore;
    private RiskScore.RiskLevel riskLevel;
    private RiskScore.TrendDirection trendDirection;
    private Double securityFactor;
    private Double complianceFactor;
    private Double operationalFactor;
    private Double historicalFactor;
    private String rationale;
    private String recommendedAction;
    private LocalDateTime scoredAt;
}
''')

add(r"D:\Development Practice\Datasheild\services\risk-scoring-service\src\main\java\com\datasheild\riskscoring\service\RiskScoringService.java", '''
package com.datasheild.riskscoring.service;

import com.datasheild.riskscoring.dto.RiskScoreRequest;
import com.datasheild.riskscoring.dto.VendorRiskResponse;
import com.datasheild.riskscoring.entity.RiskScore;
import com.datasheild.riskscoring.repository.RiskScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RiskScoringService {
    private final RiskScoreRepository repository;

    public VendorRiskResponse scoreVendor(UUID vendorId, RiskScoreRequest factors) {
        double security = normalize(factors.getSecurity());
        double compliance = normalize(factors.getCompliance());
        double operational = normalize(factors.getOperational());
        double historical = normalize(factors.getHistorical());
        double score = (security * 0.4) + (compliance * 0.35) + (operational * 0.15) + (historical * 0.1);
        RiskScore.TrendDirection trend = determineTrend(vendorId, score);

        RiskScore riskScore = RiskScore.builder()
            .tenantId(factors.getTenantId())
            .vendorId(vendorId)
            .securityFactor(security)
            .complianceFactor(compliance)
            .operationalFactor(operational)
            .historicalFactor(historical)
            .overallRiskScore(round(score))
            .riskLevel(determineLevel(score))
            .trendDirection(trend)
            .rationale(buildRationale(security, compliance, operational, historical, score))
            .recommendedAction(buildRecommendation(score, compliance, security))
            .scoredAt(LocalDateTime.now())
            .build();

        return toResponse(repository.save(riskScore));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> predictRiskTrend(UUID vendorId, List<Double> historicalScores) {
        List<Double> scores = historicalScores == null || historicalScores.isEmpty()
            ? repository.findByVendorId(vendorId).stream().map(RiskScore::getOverallRiskScore).toList()
            : historicalScores;
        if (scores.isEmpty()) {
            return Map.of(
                "vendorId", vendorId,
                "trend", RiskScore.TrendDirection.STABLE,
                "projectedRisk", 0.0,
                "message", "No historical scores available"
            );
        }

        double averageDelta = calculateAverageDelta(scores);
        double projectedRisk = round(clamp(scores.get(scores.size() - 1) + averageDelta));
        RiskScore.TrendDirection trend = classifyTrend(averageDelta);
        double volatility = round(calculateVolatility(scores));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("vendorId", vendorId);
        response.put("trend", trend);
        response.put("currentRisk", round(scores.get(scores.size() - 1)));
        response.put("averageDelta", round(averageDelta));
        response.put("projectedRisk", projectedRisk);
        response.put("volatility", volatility);
        response.put("observations", scores.size());
        return response;
    }

    @Transactional(readOnly = true)
    public List<VendorRiskResponse> getTopRisks(UUID tenantId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return repository.findTopRisks(tenantId, PageRequest.of(0, safeLimit)).stream()
            .map(this::toResponse)
            .toList();
    }

    private RiskScore.TrendDirection determineTrend(UUID vendorId, double newScore) {
        return repository.findTopByVendorIdOrderByScoredAtDesc(vendorId)
            .map(existing -> classifyTrend(newScore - existing.getOverallRiskScore()))
            .orElse(RiskScore.TrendDirection.STABLE);
    }

    private RiskScore.RiskLevel determineLevel(double score) {
        if (score >= 0.80) {
            return RiskScore.RiskLevel.CRITICAL;
        }
        if (score >= 0.65) {
            return RiskScore.RiskLevel.HIGH;
        }
        if (score >= 0.40) {
            return RiskScore.RiskLevel.MEDIUM;
        }
        return RiskScore.RiskLevel.LOW;
    }

    private RiskScore.TrendDirection classifyTrend(double delta) {
        if (delta >= 0.03) {
            return RiskScore.TrendDirection.RISING;
        }
        if (delta <= -0.03) {
            return RiskScore.TrendDirection.FALLING;
        }
        return RiskScore.TrendDirection.STABLE;
    }

    private double calculateAverageDelta(List<Double> scores) {
        if (scores.size() < 2) {
            return 0.0;
        }
        double deltaSum = 0.0;
        for (int index = 1; index < scores.size(); index++) {
            deltaSum += scores.get(index) - scores.get(index - 1);
        }
        return deltaSum / (scores.size() - 1);
    }

    private double calculateVolatility(List<Double> scores) {
        if (scores.size() < 2) {
            return 0.0;
        }
        double averageDelta = calculateAverageDelta(scores);
        double totalVariance = 0.0;
        for (int index = 1; index < scores.size(); index++) {
            double delta = scores.get(index) - scores.get(index - 1);
            totalVariance += Math.pow(delta - averageDelta, 2);
        }
        return Math.sqrt(totalVariance / (scores.size() - 1));
    }

    private VendorRiskResponse toResponse(RiskScore entity) {
        return VendorRiskResponse.builder()
            .tenantId(entity.getTenantId())
            .vendorId(entity.getVendorId())
            .overallRiskScore(entity.getOverallRiskScore())
            .riskLevel(entity.getRiskLevel())
            .trendDirection(entity.getTrendDirection())
            .securityFactor(entity.getSecurityFactor())
            .complianceFactor(entity.getComplianceFactor())
            .operationalFactor(entity.getOperationalFactor())
            .historicalFactor(entity.getHistoricalFactor())
            .rationale(entity.getRationale())
            .recommendedAction(entity.getRecommendedAction())
            .scoredAt(entity.getScoredAt())
            .build();
    }

    private String buildRationale(double security, double compliance, double operational, double historical, double score) {
        return String.format(
            "Weighted score %.2f derived from security %.2f, compliance %.2f, operational %.2f, historical %.2f.",
            round(score), round(security), round(compliance), round(operational), round(historical)
        );
    }

    private String buildRecommendation(double score, double compliance, double security) {
        if (score >= 0.80) {
            return "Escalate immediately, pause high-risk data sharing, and trigger senior vendor review.";
        }
        if (compliance >= 0.75 || security >= 0.75) {
            return "Schedule remediation checkpoints with the vendor and review control attestations.";
        }
        if (score >= 0.40) {
            return "Maintain enhanced monitoring and refresh assessment evidence in the next cycle.";
        }
        return "Continue standard monitoring cadence with periodic reassessment.";
    }

    private double normalize(Double value) {
        return clamp(value == null ? 0.0 : value);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
''')

add(r"D:\Development Practice\Datasheild\services\risk-scoring-service\src\main\java\com\datasheild\riskscoring\controller\RiskScoringController.java", '''
package com.datasheild.riskscoring.controller;

import com.datasheild.riskscoring.dto.RiskScoreRequest;
import com.datasheild.riskscoring.dto.VendorRiskResponse;
import com.datasheild.riskscoring.service.RiskScoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/risk-scoring")
@RequiredArgsConstructor
@Tag(name = "Risk Scoring", description = "Vendor risk scoring and trend endpoints")
public class RiskScoringController {
    private final RiskScoringService riskScoringService;

    @PostMapping("/score-vendor")
    @Operation(summary = "Score a vendor using weighted risk factors")
    public ResponseEntity<VendorRiskResponse> scoreVendor(@Valid @RequestBody RiskScoreRequest request) {
        return ResponseEntity.ok(riskScoringService.scoreVendor(request.getVendorId(), request));
    }

    @GetMapping("/trend/{vendorId}")
    @Operation(summary = "Predict vendor risk trend from historical scores")
    public ResponseEntity<Map<String, Object>> getTrend(@PathVariable UUID vendorId) {
        return ResponseEntity.ok(riskScoringService.predictRiskTrend(vendorId, null));
    }

    @GetMapping("/top-risks/{tenantId}")
    @Operation(summary = "Fetch highest-risk vendors for a tenant")
    public ResponseEntity<List<VendorRiskResponse>> getTopRisks(@PathVariable UUID tenantId,
                                                                @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(riskScoringService.getTopRisks(tenantId, limit));
    }
}
''')

add(r"D:\Development Practice\Datasheild\services\risk-scoring-service\src\main\resources\application.yml", '''
spring:
  application:
    name: risk-scoring-service
  datasource:
    url: jdbc:postgresql://localhost:5432/datasheild
    username: datasheild_user
    password: secure_password
    hikari:
      maximum-pool-size: 15
      minimum-idle: 3
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        default_schema: risk_scoring
        hbm2ddl.create_namespaces: true

server:
  port: 8020
  servlet:
    context-path: /

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics

logging:
  level:
    root: INFO
    com.datasheild: DEBUG
''')

# Anomaly detection files
add(r"D:\Development Practice\Datasheild\services\anomaly-detection-service\pom.xml", '''
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.4</version>
        <relativePath/>
    </parent>

    <groupId>com.datasheild</groupId>
    <artifactId>anomaly-detection-service</artifactId>
    <version>1.0.0</version>
    <name>DataShield Anomaly Detection Service</name>
    <description>Behavioral access anomaly detection service</description>

    <properties>
        <java.version>17</java.version>
        <springdoc-openapi-starter-webmvc-ui.version>2.2.0</springdoc-openapi-starter-webmvc-ui.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${springdoc-openapi-starter-webmvc-ui.version}</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
''')

add(r"D:\Development Practice\Datasheild\services\anomaly-detection-service\src\main\java\com\datasheild\anomalydetection\AnomalyDetectionServiceApplication.java", '''
package com.datasheild.anomalydetection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AnomalyDetectionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnomalyDetectionServiceApplication.class, args);
    }
}
''')

add(r"D:\Development Practice\Datasheild\services\anomaly-detection-service\src\main\java\com\datasheild\anomalydetection\config\OpenAPIConfig.java", '''
package com.datasheild.anomalydetection.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("DataShield Anomaly Detection Service")
                .version("1.0.0")
                .description("Behavioral anomaly detection and unauthorized access alerting APIs"));
    }
}
''')

add(r"D:\Development Practice\Datasheild\services\anomaly-detection-service\src\main\java\com\datasheild\anomalydetection\entity\BehavioralAnomaly.java", '''
package com.datasheild.anomalydetection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "behavioral_anomaly", schema = "anomaly", indexes = {
    @Index(name = "idx_behavior_user_detected", columnList = "user_id,detected_at DESC"),
    @Index(name = "idx_behavior_risk", columnList = "overall_risk_score DESC")
})
public class BehavioralAnomaly {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private LocalDateTime accessTime;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Integer volume;

    @Column(nullable = false)
    private Double timeDeviationScore;

    @Column(nullable = false)
    private Double volumeDeviationScore;

    @Column(nullable = false)
    private Double geographyDeviationScore;

    @Column(nullable = false)
    private Double overallRiskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Severity severity;

    @Column(nullable = false)
    private Boolean unauthorizedAccess;

    @Column(nullable = false)
    private Boolean criticalAlert;

    @Column(nullable = false)
    private Integer baselineWindowDays;

    @Column(columnDefinition = "TEXT")
    private String profileSnapshot;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @CreationTimestamp
    private LocalDateTime detectedAt;

    public enum Severity {
        LOW, MODERATE, HIGH, CRITICAL
    }
}
''')

add(r"D:\Development Practice\Datasheild\services\anomaly-detection-service\src\main\java\com\datasheild\anomalydetection\repository\BehavioralAnomalyRepository.java", '''
package com.datasheild.anomalydetection.repository;

import com.datasheild.anomalydetection.entity.BehavioralAnomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BehavioralAnomalyRepository extends JpaRepository<BehavioralAnomaly, UUID> {

    @Query("SELECT b FROM BehavioralAnomaly b WHERE b.userId = :userId ORDER BY b.detectedAt DESC")
    List<BehavioralAnomaly> findByUserId(UUID userId);

    @Query("SELECT b FROM BehavioralAnomaly b WHERE b.userId = :userId AND b.accessTime >= :since ORDER BY b.accessTime DESC")
    List<BehavioralAnomaly> findRecentByUserId(UUID userId, LocalDateTime since);

    Optional<BehavioralAnomaly> findTopByUserIdOrderByDetectedAtDesc(UUID userId);
}
''')

add(r"D:\Development Practice\Datasheild\services\anomaly-detection-service\src\main\java\com\datasheild\anomalydetection\dto\AccessAnomalyRequest.java", '''
package com.datasheild.anomalydetection.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AccessAnomalyRequest {
    @NotNull
    private UUID userId;

    @NotNull
    private LocalDateTime accessTime;

    @NotBlank
    private String location;

    @NotNull
    @Min(1)
    private Integer volume;
}
''')

add(r"D:\Development Practice\Datasheild\services\anomaly-detection-service\src\main\java\com\datasheild\anomalydetection\dto\BehaviorProfileResponse.java", '''
package com.datasheild.anomalydetection.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BehaviorProfileResponse {
    private UUID userId;
    private LocalDateTime generatedAt;
    private int sampleSize;
    private double baselineAccessHour;
    private double averageVolume;
    private List<String> frequentLocations;
    private double averageRiskScore;
    private String normalBusinessHours;
    private String profileSummary;
}
''')

add(r"D:\Development Practice\Datasheild\services\anomaly-detection-service\src\main\java\com\datasheild\anomalydetection\service\BehavioralAnomalyService.java", '''
package com.datasheild.anomalydetection.service;

import com.datasheild.anomalydetection.dto.BehaviorProfileResponse;
import com.datasheild.anomalydetection.entity.BehavioralAnomaly;
import com.datasheild.anomalydetection.repository.BehavioralAnomalyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BehavioralAnomalyService {
    private static final int PROFILE_WINDOW_DAYS = 30;

    private final BehavioralAnomalyRepository repository;

    public BehavioralAnomaly detectAccessAnomaly(UUID userId, LocalDateTime accessTime, String location, Integer volume) {
        LocalDateTime effectiveAccessTime = accessTime == null ? LocalDateTime.now() : accessTime;
        int safeVolume = volume == null ? 1 : Math.max(1, volume);
        BehaviorProfile profile = buildProfile(userId);

        double timeDeviation = calculateTimeDeviation(profile, effectiveAccessTime);
        double volumeDeviation = calculateVolumeDeviation(profile, safeVolume);
        double geographyDeviation = calculateGeographyDeviation(profile, location);
        double riskScore = round(Math.min(1.0, (timeDeviation * 0.35) + (volumeDeviation * 0.35) + (geographyDeviation * 0.30)));

        BehavioralAnomaly anomaly = BehavioralAnomaly.builder()
            .userId(userId)
            .accessTime(effectiveAccessTime)
            .location(location)
            .volume(safeVolume)
            .timeDeviationScore(timeDeviation)
            .volumeDeviationScore(volumeDeviation)
            .geographyDeviationScore(geographyDeviation)
            .overallRiskScore(riskScore)
            .severity(determineSeverity(riskScore))
            .unauthorizedAccess(riskScore >= 0.70)
            .criticalAlert(riskScore > 0.80)
            .baselineWindowDays(PROFILE_WINDOW_DAYS)
            .profileSnapshot(snapshot(profile))
            .explanation(buildExplanation(location, effectiveAccessTime, safeVolume, timeDeviation, volumeDeviation, geographyDeviation, riskScore))
            .build();

        return repository.save(anomaly);
    }

    @Transactional(readOnly = true)
    public BehaviorProfileResponse generateBehaviorProfile(UUID userId) {
        BehaviorProfile profile = buildProfile(userId);
        return BehaviorProfileResponse.builder()
            .userId(userId)
            .generatedAt(LocalDateTime.now())
            .sampleSize(profile.sampleSize())
            .baselineAccessHour(round(profile.averageHour()))
            .averageVolume(round(profile.averageVolume()))
            .frequentLocations(profile.frequentLocations())
            .averageRiskScore(round(profile.averageRiskScore()))
            .normalBusinessHours(String.format("%.0f:00-%.0f:00", Math.max(0.0, profile.averageHour() - 2), Math.min(23.0, profile.averageHour() + 2)))
            .profileSummary(buildProfileSummary(profile))
            .build();
    }

    public BehavioralAnomaly flagUnauthorizedAccess(UUID userId, double risk) {
        BehaviorProfile profile = buildProfile(userId);
        double clampedRisk = round(Math.max(0.0, Math.min(1.0, risk)));
        BehavioralAnomaly anomaly = repository.findTopByUserIdOrderByDetectedAtDesc(userId)
            .map(existing -> BehavioralAnomaly.builder()
                .userId(userId)
                .accessTime(LocalDateTime.now())
                .location(existing.getLocation())
                .volume(existing.getVolume())
                .timeDeviationScore(existing.getTimeDeviationScore())
                .volumeDeviationScore(existing.getVolumeDeviationScore())
                .geographyDeviationScore(existing.getGeographyDeviationScore())
                .overallRiskScore(clampedRisk)
                .severity(determineSeverity(clampedRisk))
                .unauthorizedAccess(clampedRisk >= 0.70)
                .criticalAlert(clampedRisk > 0.80)
                .baselineWindowDays(PROFILE_WINDOW_DAYS)
                .profileSnapshot(snapshot(profile))
                .explanation("Unauthorized access manually flagged by risk threshold evaluation")
                .build())
            .orElseGet(() -> BehavioralAnomaly.builder()
                .userId(userId)
                .accessTime(LocalDateTime.now())
                .location("UNKNOWN")
                .volume(1)
                .timeDeviationScore(clampedRisk)
                .volumeDeviationScore(clampedRisk)
                .geographyDeviationScore(clampedRisk)
                .overallRiskScore(clampedRisk)
                .severity(determineSeverity(clampedRisk))
                .unauthorizedAccess(clampedRisk >= 0.70)
                .criticalAlert(clampedRisk > 0.80)
                .baselineWindowDays(PROFILE_WINDOW_DAYS)
                .profileSnapshot(snapshot(profile))
                .explanation("Unauthorized access flagged without historical access sample")
                .build());
        return repository.save(anomaly);
    }

    private BehaviorProfile buildProfile(UUID userId) {
        List<BehavioralAnomaly> history = repository.findRecentByUserId(userId, LocalDateTime.now().minusDays(PROFILE_WINDOW_DAYS));
        if (history.isEmpty()) {
            return new BehaviorProfile(9.0, 10.0, 0.25, List.of(), 0);
        }

        double averageHour = history.stream().mapToDouble(item -> item.getAccessTime().getHour()).average().orElse(9.0);
        double averageVolume = history.stream().mapToInt(BehavioralAnomaly::getVolume).average().orElse(10.0);
        double averageRisk = history.stream().mapToDouble(BehavioralAnomaly::getOverallRiskScore).average().orElse(0.25);
        List<String> frequentLocations = history.stream()
            .collect(Collectors.groupingBy(BehavioralAnomaly::getLocation, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
            .limit(3)
            .map(Map.Entry::getKey)
            .toList();

        return new BehaviorProfile(averageHour, averageVolume, averageRisk, frequentLocations, history.size());
    }

    private double calculateTimeDeviation(BehaviorProfile profile, LocalDateTime accessTime) {
        double difference = Math.abs(accessTime.getHour() - profile.averageHour());
        double normalized = Math.min(1.0, difference / 12.0);
        if (accessTime.getHour() < 6 || accessTime.getHour() > 22) {
            normalized = Math.min(1.0, normalized + 0.20);
        }
        return round(normalized);
    }

    private double calculateVolumeDeviation(BehaviorProfile profile, int volume) {
        double baseline = Math.max(1.0, profile.averageVolume());
        double normalized = Math.min(1.0, Math.abs(volume - baseline) / baseline);
        return round(normalized);
    }

    private double calculateGeographyDeviation(BehaviorProfile profile, String location) {
        if (profile.frequentLocations().isEmpty()) {
            return 0.35;
        }
        return profile.frequentLocations().stream().anyMatch(saved -> saved.equalsIgnoreCase(location)) ? 0.10 : 0.85;
    }

    private BehavioralAnomaly.Severity determineSeverity(double riskScore) {
        if (riskScore > 0.80) {
            return BehavioralAnomaly.Severity.CRITICAL;
        }
        if (riskScore >= 0.65) {
            return BehavioralAnomaly.Severity.HIGH;
        }
        if (riskScore >= 0.40) {
            return BehavioralAnomaly.Severity.MODERATE;
        }
        return BehavioralAnomaly.Severity.LOW;
    }

    private String buildExplanation(String location, LocalDateTime accessTime, int volume, double timeDeviation,
                                    double volumeDeviation, double geographyDeviation, double riskScore) {
        return String.format(
            "Access at %s from %s with volume %d produced time deviation %.2f, volume deviation %.2f, geography deviation %.2f and risk %.2f.",
            accessTime, location, volume, timeDeviation, volumeDeviation, geographyDeviation, riskScore
        );
    }

    private String buildProfileSummary(BehaviorProfile profile) {
        return String.format(
            "Baseline built from %d events with average hour %.1f, average volume %.1f, and preferred locations %s.",
            profile.sampleSize(), profile.averageHour(), profile.averageVolume(), profile.frequentLocations()
        );
    }

    private String snapshot(BehaviorProfile profile) {
        return String.format(
            "hour=%.2f, volume=%.2f, risk=%.2f, locations=%s",
            profile.averageHour(), profile.averageVolume(), profile.averageRiskScore(), profile.frequentLocations()
        );
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private record BehaviorProfile(double averageHour, double averageVolume, double averageRiskScore,
                                   List<String> frequentLocations, int sampleSize) {
    }
}
''')

add(r"D:\Development Practice\Datasheild\services\anomaly-detection-service\src\main\java\com\datasheild\anomalydetection\controller\AnomalyDetectionController.java", '''
package com.datasheild.anomalydetection.controller;

import com.datasheild.anomalydetection.dto.AccessAnomalyRequest;
import com.datasheild.anomalydetection.dto.BehaviorProfileResponse;
import com.datasheild.anomalydetection.entity.BehavioralAnomaly;
import com.datasheild.anomalydetection.service.BehavioralAnomalyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/anomaly-detection")
@RequiredArgsConstructor
@Tag(name = "Anomaly Detection", description = "Behavioral access anomaly endpoints")
public class AnomalyDetectionController {
    private final BehavioralAnomalyService behavioralAnomalyService;

    @PostMapping("/detect-access-anomaly")
    @Operation(summary = "Detect access anomalies based on user behavior baselines")
    public ResponseEntity<BehavioralAnomaly> detectAccessAnomaly(@Valid @RequestBody AccessAnomalyRequest request) {
        return ResponseEntity.ok(behavioralAnomalyService.detectAccessAnomaly(
            request.getUserId(),
            request.getAccessTime(),
            request.getLocation(),
            request.getVolume()
        ));
    }

    @GetMapping("/profile/{userId}")
    @Operation(summary = "Generate a 30-day behavioral profile for a user")
    public ResponseEntity<BehaviorProfileResponse> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(behavioralAnomalyService.generateBehaviorProfile(userId));
    }

    @PostMapping("/flag-unauthorized")
    @Operation(summary = "Flag unauthorized access when risk exceeds a threshold")
    public ResponseEntity<BehavioralAnomaly> flagUnauthorized(@RequestParam UUID userId,
                                                              @RequestParam double risk) {
        return ResponseEntity.ok(behavioralAnomalyService.flagUnauthorizedAccess(userId, risk));
    }
}
''')

add(r"D:\Development Practice\Datasheild\services\anomaly-detection-service\src\main\resources\application.yml", '''
spring:
  application:
    name: anomaly-detection-service
  datasource:
    url: jdbc:postgresql://localhost:5432/datasheild
    username: datasheild_user
    password: secure_password
    hikari:
      maximum-pool-size: 15
      minimum-idle: 3
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        default_schema: anomaly
        hbm2ddl.create_namespaces: true

server:
  port: 8021
  servlet:
    context-path: /

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics

logging:
  level:
    root: INFO
    com.datasheild: DEBUG
''')

for file_path, content in files.items():
    path = Path(file_path)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding='utf-8')
