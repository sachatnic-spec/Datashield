package io.datasheild.auth.entity;

public enum UserRole {
    SUPER_ADMIN,          // DataShield internal platform ops
    TENANT_ADMIN,         // Customer IT/Admin lead
    DPO,                  // Data Protection Officer
    PRIVACY_MANAGER,      // Compliance analyst
    CISO,                 // Chief Information Security Officer
    LEGAL_REVIEWER,       // Legal team
    IT_ADMIN,             // Engineering/IT lead
    API_CONSUMER,         // Service account for programmatic access
    DATA_PRINCIPAL,       // End user / citizen
    AUDITOR,              // Internal audit team
    SUPPORT_AGENT         // Customer support
}
