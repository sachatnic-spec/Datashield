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
    private static final Pattern AADHAAR_PATTERN = Pattern.compile("\\b\\d{4}\\s?\\d{4}\\s?\\d{4}\\b");
    private static final Pattern PAN_PATTERN = Pattern.compile("\\b[A-Z]{5}\\d{4}[A-Z]\\b");
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("\\b(?:\\d{4}[-\\s]?){3}\\d{4}\\b");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[A-Za-z]{2,}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b(?:\\+91[-\\s]?)?[6-9]\\d{9}\\b");
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("\\b[A-PR-WYa-pr-wy][1-9]\\d\\s?\\d{4}[1-9]\\b");
    private static final Pattern DOB_PATTERN = Pattern.compile("\\b(?:0?[1-9]|[12][0-9]|3[01])[-/](?:0?[1-9]|1[0-2])[-/](?:19|20)\\d{2}\\b|\\b(?:19|20)\\d{2}[-/](?:0?[1-9]|1[0-2])[-/](?:0?[1-9]|[12][0-9]|3[01])\\b");
    private static final Pattern BANK_ACCOUNT_PATTERN = Pattern.compile("\\b\\d{9,18}\\b");
    private static final Pattern NAME_PATTERN = Pattern.compile("\\b(?:Mr|Mrs|Ms|Dr)?\\.?\\s?[A-Z][a-z]+(?:\\s+[A-Z][a-z]+){1,2}\\b");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("\\b\\d{1,4},?\\s+[A-Za-z0-9\\s,.-]{8,}(Street|St|Road|Rd|Avenue|Ave|Lane|Ln|Nagar|Colony|Sector)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern MEDICAL_PATTERN = Pattern.compile("(?i)\\b(patient|diagnosis|prescription|medication|blood group|medical record)\\b");
    private static final List<PatternRule> PATTERN_RULES = List.of(
        new PatternRule(PIIDetectionResult.PIICategory.AADHAAR, AADHAAR_PATTERN, 0.95, PIIDetectionResult.DetectionSource.HYBRID),
        new PatternRule(PIIDetectionResult.PIICategory.PAN, PAN_PATTERN, 0.94, PIIDetectionResult.DetectionSource.HYBRID),
        new PatternRule(PIIDetectionResult.PIICategory.CREDIT_CARD, CREDIT_CARD_PATTERN, 0.92, PIIDetectionResult.DetectionSource.HYBRID),
        new PatternRule(PIIDetectionResult.PIICategory.EMAIL, EMAIL_PATTERN, 0.88, PIIDetectionResult.DetectionSource.REGEX_PATTERN),
        new PatternRule(PIIDetectionResult.PIICategory.PHONE, PHONE_PATTERN, 0.84, PIIDetectionResult.DetectionSource.REGEX_PATTERN),
        new PatternRule(PIIDetectionResult.PIICategory.PASSPORT, PASSPORT_PATTERN, 0.90, PIIDetectionResult.DetectionSource.HYBRID),
        new PatternRule(PIIDetectionResult.PIICategory.DOB, DOB_PATTERN, 0.82, PIIDetectionResult.DetectionSource.HYBRID),
        new PatternRule(PIIDetectionResult.PIICategory.BANK_ACCOUNT, BANK_ACCOUNT_PATTERN, 0.79, PIIDetectionResult.DetectionSource.ML_MODEL)
    );

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
        redacted = applyMask(redacted, AADHAAR_PATTERN, value -> "XXXX XXXX " + digitsOnly(value).substring(Math.max(0, digitsOnly(value).length() - 4)));
        redacted = applyMask(redacted, PAN_PATTERN, value -> value.substring(0, 2) + "******" + value.substring(value.length() - 2));
        redacted = applyMask(redacted, CREDIT_CARD_PATTERN, value -> "XXXX-XXXX-XXXX-" + digitsOnly(value).substring(Math.max(0, digitsOnly(value).length() - 4)));
        redacted = applyMask(redacted, EMAIL_PATTERN, this::maskEmail);
        redacted = applyMask(redacted, PHONE_PATTERN, value -> maskPreservingLast(value, 4));
        redacted = applyMask(redacted, PASSPORT_PATTERN, value -> maskPreservingLast(value, 3));
        redacted = applyMask(redacted, DOB_PATTERN, value -> "**/**/****");
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
                matches.add(new DetectionCandidate(rule.category(), confidence, source));
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
