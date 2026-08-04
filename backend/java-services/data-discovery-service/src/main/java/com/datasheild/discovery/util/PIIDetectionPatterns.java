package com.datasheild.discovery.util;

import java.util.regex.Pattern;

public class PIIDetectionPatterns {

    // Indian PII patterns
    public static final Pattern AADHAAR_PATTERN = Pattern.compile("\\b[0-9]{4}\\s?[0-9]{4}\\s?[0-9]{4}\\b");
    public static final Pattern PAN_PATTERN = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}");
    public static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("\\b(?:\\d{4}[-\\s]?){3}\\d{4}\\b");
    public static final Pattern SSN_PATTERN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    
    // Email pattern
    public static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    
    // Phone patterns (India: +91 or 0)
    public static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+91|0)?[6-9]\\d{9}\\b");
    
    // Passport pattern (Indian: A-Z followed by digits)
    public static final Pattern PASSPORT_PATTERN = Pattern.compile("[A-Z]{1}[0-9]{7}");
    
    // Driver License pattern (varies by state, basic)
    public static final Pattern DRIVER_LICENSE_PATTERN = Pattern.compile("[A-Z]{2}[0-9]{13}");
    
    // Bank account patterns (16 digits for most Indian banks)
    public static final Pattern BANK_ACCOUNT_PATTERN = Pattern.compile("\\b[0-9]{9,18}\\b");
    
    // IPv4 address pattern
    public static final Pattern IPV4_PATTERN = Pattern.compile("\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b");
    
    // Medical record indicators (basic)
    public static final Pattern MEDICAL_PATTERN = Pattern.compile("(?i)(patient|prescription|diagnosis|treatment|medication|hospital|doctor|medical)\\s+[A-Za-z0-9#]+");
    
    // DOB patterns (dd-MM-yyyy or dd/MM/yyyy or yyyy-MM-dd)
    public static final Pattern DOB_PATTERN = Pattern.compile("\\b(?:19|20)\\d{2}[-/](?:0[1-9]|1[0-2])[-/](?:0[1-9]|[12][0-9]|3[01])\\b");
    
    // Name pattern (common Indian names - basic)
    public static final Pattern NAME_PATTERN = Pattern.compile("\\b(?:[A-Z][a-z]+\\s+)+[A-Z][a-z]+\\b");
    
    public static class DetectionResult {
        public String piiType;
        public String matchedValue;
        public double confidenceScore;
        public String pattern;
        
        public DetectionResult(String piiType, String matchedValue, double confidence, String pattern) {
            this.piiType = piiType;
            this.matchedValue = matchedValue;
            this.confidenceScore = confidence;
            this.pattern = pattern;
        }
    }
    
    public static DetectionResult detectPII(String text) {
        if (text == null || text.isEmpty()) return null;
        
        // Aadhaar (very high confidence - 12 digits in groups of 4)
        if (AADHAAR_PATTERN.matcher(text).find()) {
            return new DetectionResult("AADHAAR", text, 0.95, "aadhaar");
        }
        
        // PAN (very high confidence - fixed format)
        if (PAN_PATTERN.matcher(text).find()) {
            return new DetectionResult("PAN", text, 0.95, "pan");
        }
        
        // Credit Card (high confidence - 16 digits with separators)
        if (CREDIT_CARD_PATTERN.matcher(text).find()) {
            return new DetectionResult("CREDIT_CARD", text, 0.90, "credit_card");
        }
        
        // Email (medium-high confidence)
        if (EMAIL_PATTERN.matcher(text).find()) {
            return new DetectionResult("EMAIL", text, 0.85, "email");
        }
        
        // Phone (medium confidence - can have false positives)
        if (PHONE_PATTERN.matcher(text).find()) {
            return new DetectionResult("PHONE", text, 0.80, "phone");
        }
        
        // Passport
        if (PASSPORT_PATTERN.matcher(text).find()) {
            return new DetectionResult("PASSPORT", text, 0.90, "passport");
        }
        
        // Driver License
        if (DRIVER_LICENSE_PATTERN.matcher(text).find()) {
            return new DetectionResult("DRIVER_LICENSE", text, 0.85, "driver_license");
        }
        
        // IPv4
        if (IPV4_PATTERN.matcher(text).find()) {
            return new DetectionResult("IP_ADDRESS", text, 0.85, "ipv4");
        }
        
        // DOB
        if (DOB_PATTERN.matcher(text).find()) {
            return new DetectionResult("DOB", text, 0.80, "dob");
        }
        
        return null;
    }
}
