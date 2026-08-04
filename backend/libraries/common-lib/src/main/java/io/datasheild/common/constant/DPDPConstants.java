package io.datasheild.common.constant;

/**
 * DPDP Act section and compliance constants
 */
public class DPDPConstants {
    
    // DPDP Act sections
    public static final String DPDP_SECTION_4 = "§4 - Principles";
    public static final String DPDP_SECTION_8 = "§8 - Notice of Breach";
    public static final String DPDP_SECTION_9 = "§9 - Data Processing Agreement";
    public static final String DPDP_SECTION_17 = "§17 - Right to Access";
    public static final String DPDP_SECTION_18 = "§18 - Right to Correction";
    public static final String DPDP_SECTION_19 = "§19 - Right to Erasure";
    
    // Breach notification
    public static final long BREACH_NOTIFICATION_HOURS = 72;
    
    // DPR (Data Principal Rights)
    public static final long DPR_RESPONSE_DAYS = 30;
    
    // Data retention
    public static final int DEFAULT_RETENTION_DAYS = 365;
    
    // Consent validity
    public static final int CONSENT_VALIDITY_YEARS = 2;
    
    // Tenant tiers
    public static final String TIER_STARTER = "STARTER";
    public static final String TIER_PROFESSIONAL = "PROFESSIONAL";
    public static final String TIER_ENTERPRISE = "ENTERPRISE";
    public static final String TIER_GOVERNMENT = "GOVERNMENT";
}
