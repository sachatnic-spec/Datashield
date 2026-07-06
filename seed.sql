-- =============================================================================
-- DataShield India — Database Seed Script (RANDOM UUID VERSION)
-- DB:   datasheild
-- User: datasheild / datasheild_dev_pwd
-- Run:  psql -U datasheild -d datasheild -f seed_random_uuid.sql
--
-- Difference from original seed.sql:
--   All previously hardcoded UUIDs (e.g. '00000000-0000-0000-0000-000000000001')
--   are now generated randomly at runtime using gen_random_uuid(). A DO block
--   with variables is used so that foreign-key references (tenant_id on users,
--   consent notices, etc.) still resolve correctly to the same random values.
-- =============================================================================

-- Requires pgcrypto for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------------------------------------------------------------------------
-- 0. SCHEMAS — create all service schemas
-- ---------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS consent;
CREATE SCHEMA IF NOT EXISTS rights;
CREATE SCHEMA IF NOT EXISTS breach;
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS notification;
CREATE SCHEMA IF NOT EXISTS dpbi;
CREATE SCHEMA IF NOT EXISTS webhook;
CREATE SCHEMA IF NOT EXISTS siem;
CREATE SCHEMA IF NOT EXISTS connector;
CREATE SCHEMA IF NOT EXISTS search;
CREATE SCHEMA IF NOT EXISTS config;
CREATE SCHEMA IF NOT EXISTS vendor;
CREATE SCHEMA IF NOT EXISTS policy;
CREATE SCHEMA IF NOT EXISTS analytics;
CREATE SCHEMA IF NOT EXISTS workflow;
CREATE SCHEMA IF NOT EXISTS discovery;
CREATE SCHEMA IF NOT EXISTS classification;
CREATE SCHEMA IF NOT EXISTS retention;
CREATE SCHEMA IF NOT EXISTS grievance;
CREATE SCHEMA IF NOT EXISTS report;

-- ---------------------------------------------------------------------------
-- 1. TABLE DEFINITIONS (unchanged from original)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.tenants (
                                              id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_code     VARCHAR(50)  UNIQUE NOT NULL,
    name            VARCHAR(255) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    tier            VARCHAR(20)  NOT NULL DEFAULT 'STANDARD',
    contact_email   VARCHAR(255),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS public.feature_flags (
                                                    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID         NOT NULL REFERENCES public.tenants(id),
    flag_name   VARCHAR(100) NOT NULL,
    enabled     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, flag_name)
    );

CREATE TABLE IF NOT EXISTS public.users (
                                            id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID         NOT NULL REFERENCES public.tenants(id),
    email           VARCHAR(255) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255),
    role            VARCHAR(50)  NOT NULL DEFAULT 'DATA_PRINCIPAL',
    mfa_enabled     BOOLEAN      NOT NULL DEFAULT FALSE,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS consent.consent_notices (
                                                       id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID         NOT NULL,
    version     VARCHAR(20)  NOT NULL,
    title       VARCHAR(255) NOT NULL,
    content     TEXT         NOT NULL,
    language    VARCHAR(10)  NOT NULL DEFAULT 'en',
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS consent.consent_purposes (
                                                        id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID         NOT NULL,
    code        VARCHAR(100) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    category    VARCHAR(50)  NOT NULL,
    required    BOOLEAN      NOT NULL DEFAULT FALSE,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, code)
    );

CREATE TABLE IF NOT EXISTS consent.consent_records (
                                                       id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID         NOT NULL,
    data_principal  VARCHAR(255) NOT NULL,
    notice_id       UUID         REFERENCES consent.consent_notices(id),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    source          VARCHAR(50)  NOT NULL DEFAULT 'CONSENT_WIDGET',
    language        VARCHAR(10)  NOT NULL DEFAULT 'en',
    granted_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMPTZ,
    withdrawn_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS rights.dpr_requests (
                                                   id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID         NOT NULL,
    data_principal  VARCHAR(255) NOT NULL,
    request_type    VARCHAR(50)  NOT NULL,
    status          VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    description     TEXT,
    due_date        TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS breach.breach_incidents (
                                                       id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID         NOT NULL,
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    severity            VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    status              VARCHAR(30)  NOT NULL DEFAULT 'OPEN',
    detected_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    notify_deadline     TIMESTAMPTZ  NOT NULL,
    dpbi_notified_at    TIMESTAMPTZ,
    principals_notified_at TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS audit.audit_logs (
                                                id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID         NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    actor           VARCHAR(255),
    entity_type     VARCHAR(100),
    entity_id       VARCHAR(255),
    action          VARCHAR(100),
    outcome         VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS',
    ip_address      VARCHAR(45),
    payload         JSONB,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_audit_tenant_created ON audit.audit_logs (tenant_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit.audit_logs (entity_type, entity_id);

CREATE TABLE IF NOT EXISTS notification.notification_templates (
                                                                   id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID         NOT NULL,
    code        VARCHAR(100) NOT NULL,
    channel     VARCHAR(20)  NOT NULL,
    subject     VARCHAR(255),
    body        TEXT         NOT NULL,
    language    VARCHAR(10)  NOT NULL DEFAULT 'en',
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, code, channel, language)
    );

CREATE TABLE IF NOT EXISTS dpbi.dpbi_submissions (
                                                     id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID         NOT NULL,
    breach_id       UUID,
    reference_no    VARCHAR(100),
    status          VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    submitted_at    TIMESTAMPTZ,
    acknowledged_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS vendor.vendors (
                                              id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID         NOT NULL,
    name            VARCHAR(255) NOT NULL,
    category        VARCHAR(100),
    risk_level      VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    contact_email   VARCHAR(255),
    dpa_signed      BOOLEAN      NOT NULL DEFAULT FALSE,
    dpa_expiry      DATE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS policy.policies (
                                               id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID         NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    version     VARCHAR(20)  NOT NULL,
    title       VARCHAR(255) NOT NULL,
    content     TEXT         NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    effective_from DATE      NOT NULL DEFAULT CURRENT_DATE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, type, version)
    );

CREATE TABLE IF NOT EXISTS config.tenant_configs (
                                                     id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID         NOT NULL,
    config_key  VARCHAR(255) NOT NULL,
    config_value TEXT        NOT NULL,
    encrypted   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, config_key)
    );

CREATE TABLE IF NOT EXISTS grievance.grievances (
                                                    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID         NOT NULL,
    data_principal  VARCHAR(255) NOT NULL,
    subject         VARCHAR(255) NOT NULL,
    description     TEXT,
    status          VARCHAR(30)  NOT NULL DEFAULT 'OPEN',
    due_date        TIMESTAMPTZ  NOT NULL,
    resolved_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS analytics.compliance_metrics (
                                                            id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID         NOT NULL,
    metric_name     VARCHAR(100) NOT NULL,
    metric_value    NUMERIC      NOT NULL,
    period_start    DATE         NOT NULL,
    period_end      DATE         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, metric_name, period_start)
    );

CREATE TABLE IF NOT EXISTS retention.retention_policies (
                                                            id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID         NOT NULL,
    data_category   VARCHAR(100) NOT NULL,
    retention_days  INT          NOT NULL,
    action_on_expiry VARCHAR(30) NOT NULL DEFAULT 'ANONYMIZE',
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, data_category)
    );

CREATE TABLE IF NOT EXISTS siem.siem_integrations (
                                                      id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID         NOT NULL,
    siem_type   VARCHAR(50)  NOT NULL,
    endpoint    VARCHAR(500) NOT NULL,
    enabled     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, siem_type)
    );

CREATE TABLE IF NOT EXISTS webhook.webhook_registrations (
                                                             id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID         NOT NULL,
    url             VARCHAR(500) NOT NULL,
    secret          VARCHAR(255) NOT NULL,
    events          TEXT[]       NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

-- ---------------------------------------------------------------------------
-- 2. SEED DATA — all UUIDs generated randomly via gen_random_uuid()
--    A DO block with variables is used so FK references stay consistent.
-- ---------------------------------------------------------------------------
DO $$
DECLARE
v_tenant_demo      UUID := gen_random_uuid();
    v_tenant_fintech    UUID := gen_random_uuid();
    v_tenant_health     UUID := gen_random_uuid();

    v_user_admin        UUID := gen_random_uuid();
    v_user_dpo_demo     UUID := gen_random_uuid();
    v_user_dpo_fintech  UUID := gen_random_uuid();
    v_user_principal    UUID := gen_random_uuid();

    v_notice_demo       UUID := gen_random_uuid();
BEGIN

    -- Tenants
INSERT INTO public.tenants (id, tenant_code, name, status, tier, contact_email) VALUES
                                                                                    (v_tenant_demo,    'DEMO_CORP',   'Demo Corporation Pvt Ltd',    'ACTIVE', 'ENTERPRISE', 'dpo@democorp.in'),
                                                                                    (v_tenant_fintech, 'FINTECH_ONE', 'FinTech One India Ltd',       'ACTIVE', 'STANDARD',   'privacy@fintechone.in'),
                                                                                    (v_tenant_health,  'HEALTH_PLUS', 'HealthPlus Digital Pvt Ltd',  'ACTIVE', 'STANDARD',   'compliance@healthplus.in')
    ON CONFLICT (tenant_code) DO NOTHING;

-- Feature flags
INSERT INTO public.feature_flags (tenant_id, flag_name, enabled) VALUES
                                                                     (v_tenant_demo,    'CONSENT_WIDGET_V2',  TRUE),
                                                                     (v_tenant_demo,    'BREACH_AUTO_NOTIFY', TRUE),
                                                                     (v_tenant_demo,    'AI_RISK_SCORING',    TRUE),
                                                                     (v_tenant_fintech, 'CONSENT_WIDGET_V2',  TRUE),
                                                                     (v_tenant_fintech, 'BREACH_AUTO_NOTIFY', FALSE),
                                                                     (v_tenant_health,  'CONSENT_WIDGET_V2',  TRUE),
                                                                     (v_tenant_health,  'BREACH_AUTO_NOTIFY', TRUE)
    ON CONFLICT (tenant_id, flag_name) DO NOTHING;

-- Users (passwords are BCrypt of 'Admin@1234' — change before production)
INSERT INTO public.users (id, tenant_id, email, password_hash, full_name, role) VALUES
                                                                                    (v_user_admin,       v_tenant_demo,    'admin@datasheild.in',
                                                                                     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4J/HS.iK8.', 'Platform Admin', 'PLATFORM_ADMIN'),
                                                                                    (v_user_dpo_demo,    v_tenant_demo,    'dpo@democorp.in',
                                                                                     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4J/HS.iK8.', 'Demo Corp DPO', 'DPO'),
                                                                                    (v_user_dpo_fintech, v_tenant_fintech, 'dpo@fintechone.in',
                                                                                     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4J/HS.iK8.', 'FinTech One DPO', 'DPO'),
                                                                                    (v_user_principal,   v_tenant_demo,    'user@democorp.in',
                                                                                     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4J/HS.iK8.', 'Test Data Principal', 'DATA_PRINCIPAL')
    ON CONFLICT (email) DO NOTHING;

-- Consent notice + purposes
INSERT INTO consent.consent_notices (id, tenant_id, version, title, content, language) VALUES
    (v_notice_demo, v_tenant_demo, '1.0', 'Privacy Notice — Demo Corp',
     'We collect and process your personal data for the purposes listed below under DPDP Act 2023.', 'en')
    ON CONFLICT DO NOTHING;

INSERT INTO consent.consent_purposes (tenant_id, code, name, description, category, required) VALUES
                                                                                                  (v_tenant_demo,    'ESSENTIAL',       'Essential Services',       'Required for core service delivery',           'ESSENTIAL',       TRUE),
                                                                                                  (v_tenant_demo,    'MARKETING',       'Marketing Communications', 'Promotional emails and offers',                'MARKETING',       FALSE),
                                                                                                  (v_tenant_demo,    'ANALYTICS',       'Analytics & Improvement',  'Usage analytics to improve our services',      'ANALYTICS',       FALSE),
                                                                                                  (v_tenant_demo,    'PERSONALIZATION', 'Personalization',          'Personalised content and recommendations',     'PERSONALIZATION', FALSE),
                                                                                                  (v_tenant_fintech, 'ESSENTIAL',       'Essential Services',       'Required for core financial service delivery','ESSENTIAL',       TRUE),
                                                                                                  (v_tenant_fintech, 'ANALYTICS',       'Analytics',                'Transaction analytics',                        'ANALYTICS',       FALSE),
                                                                                                  (v_tenant_health,  'ESSENTIAL',       'Essential Services',       'Required for healthcare service delivery',     'ESSENTIAL',       TRUE),
                                                                                                  (v_tenant_health,  'MARKETING',       'Health Tips & Offers',     'Health tips and promotional content',          'MARKETING',       FALSE)
    ON CONFLICT (tenant_id, code) DO NOTHING;

-- Rights (DPR) requests
INSERT INTO rights.dpr_requests (tenant_id, data_principal, request_type, status, description, due_date) VALUES
                                                                                                             (v_tenant_demo, 'user@democorp.in', 'ACCESS',     'PENDING',   'Request to access all personal data held', NOW() + INTERVAL '30 days'),
                                                                                                             (v_tenant_demo, 'user@democorp.in', 'CORRECTION', 'PENDING',   'Correct phone number on file',              NOW() + INTERVAL '30 days'),
                                                                                                             (v_tenant_demo, 'user@democorp.in', 'ERASURE',    'PENDING',   'Right to be forgotten request',             NOW() + INTERVAL '30 days'),
                                                                                                             (v_tenant_demo, 'user@democorp.in', 'NOMINATION', 'COMPLETED', 'Nominate spouse as data nominee',           NOW() - INTERVAL '5 days')
    ON CONFLICT DO NOTHING;

-- Breach incidents
INSERT INTO breach.breach_incidents (tenant_id, title, description, severity, status, detected_at, notify_deadline, dpbi_notified_at) VALUES
    (v_tenant_demo,
     'Sample Phishing Incident — Resolved',
     'A phishing email targeted 3 employees. No data exfiltration confirmed. Contained within 4 hours.',
     'LOW', 'RESOLVED',
     NOW() - INTERVAL '10 days',
     NOW() - INTERVAL '7 days 4 hours',
     NOW() - INTERVAL '7 days 6 hours')
    ON CONFLICT DO NOTHING;

-- Audit logs
INSERT INTO audit.audit_logs (tenant_id, event_type, actor, entity_type, entity_id, action, outcome) VALUES
                                                                                                         (v_tenant_demo,    'TENANT_PROVISIONED', 'system',              'TENANT',  v_tenant_demo::TEXT,    'CREATE', 'SUCCESS'),
                                                                                                         (v_tenant_demo,    'USER_CREATED',       'admin@datasheild.in', 'USER',    v_user_dpo_demo::TEXT,  'CREATE', 'SUCCESS'),
                                                                                                         (v_tenant_demo,    'CONSENT_GRANTED',    'user@democorp.in',    'CONSENT', NULL,                   'GRANT',  'SUCCESS'),
                                                                                                         (v_tenant_fintech, 'TENANT_PROVISIONED', 'system',              'TENANT',  v_tenant_fintech::TEXT, 'CREATE', 'SUCCESS'),
                                                                                                         (v_tenant_health,  'TENANT_PROVISIONED', 'system',              'TENANT',  v_tenant_health::TEXT,  'CREATE', 'SUCCESS')
    ON CONFLICT DO NOTHING;

-- Notification templates
INSERT INTO notification.notification_templates (tenant_id, code, channel, subject, body) VALUES
                                                                                              (v_tenant_demo, 'CONSENT_GRANTED',        'EMAIL', 'Your consent has been recorded',
                                                                                               'Dear {{name}}, your consent for {{purposes}} has been recorded on {{date}} under DPDP Act 2023.'),
                                                                                              (v_tenant_demo, 'CONSENT_WITHDRAWN',      'EMAIL', 'Your consent withdrawal is confirmed',
                                                                                               'Dear {{name}}, your consent withdrawal has been processed. We will stop processing your data for {{purposes}}.'),
                                                                                              (v_tenant_demo, 'RIGHTS_REQUEST_RECEIVED','EMAIL', 'Your rights request has been received',
                                                                                               'Dear {{name}}, your {{requestType}} request (Ref: {{requestId}}) has been received and will be processed within 30 days.'),
                                                                                              (v_tenant_demo, 'BREACH_NOTIFICATION',    'EMAIL', 'Important: Data Breach Notification',
                                                                                               'Dear {{name}}, we are writing to inform you of a data security incident that may have affected your personal data.')
    ON CONFLICT (tenant_id, code, channel, language) DO NOTHING;

-- Vendors
INSERT INTO vendor.vendors (tenant_id, name, category, risk_level, status, contact_email, dpa_signed, dpa_expiry) VALUES
                                                                                                                      (v_tenant_demo,    'AWS India',        'Cloud Infrastructure', 'LOW',    'ACTIVE', 'compliance@aws.com', TRUE,  '2026-12-31'),
                                                                                                                      (v_tenant_demo,    'Twilio India',     'SMS/Communication',    'MEDIUM', 'ACTIVE', 'privacy@twilio.com', TRUE,  '2026-06-30'),
                                                                                                                      (v_tenant_demo,    'Razorpay',         'Payment Processing',   'HIGH',   'ACTIVE', 'dpo@razorpay.com',   TRUE,  '2025-12-31'),
                                                                                                                      (v_tenant_demo,    'Google Analytics', 'Analytics',            'HIGH',   'REVIEW', 'privacy@google.com', FALSE, NULL),
                                                                                                                      (v_tenant_fintech, 'AWS India',        'Cloud Infrastructure', 'LOW',    'ACTIVE', 'compliance@aws.com', TRUE,  '2026-12-31'),
                                                                                                                      (v_tenant_fintech, 'Setu (NBFC-AA)',   'Account Aggregator',   'HIGH',   'ACTIVE', 'compliance@setu.co', TRUE,  '2026-03-31')
    ON CONFLICT DO NOTHING;

-- Policies
INSERT INTO policy.policies (tenant_id, type, version, title, content, effective_from) VALUES
                                                                                           (v_tenant_demo,    'PRIVACY_POLICY', '1.0', 'Privacy Policy v1.0',
                                                                                            'This Privacy Policy describes how Demo Corporation Pvt Ltd collects, uses, and protects your personal data under the Digital Personal Data Protection Act 2023.',
                                                                                            CURRENT_DATE),
                                                                                           (v_tenant_demo,    'DATA_RETENTION', '1.0', 'Data Retention Policy v1.0',
                                                                                            'Personal data shall be retained only for the period necessary for the purpose for which it was collected, not exceeding 3 years unless required by law.',
                                                                                            CURRENT_DATE),
                                                                                           (v_tenant_fintech, 'PRIVACY_POLICY', '1.0', 'Privacy Policy v1.0',
                                                                                            'FinTech One India Ltd processes your financial personal data under DPDP Act 2023 and RBI guidelines.',
                                                                                            CURRENT_DATE)
    ON CONFLICT (tenant_id, type, version) DO NOTHING;

-- Tenant configs
INSERT INTO config.tenant_configs (tenant_id, config_key, config_value) VALUES
                                                                            (v_tenant_demo,    'consent.widget.theme',    'light'),
                                                                            (v_tenant_demo,    'consent.widget.position', 'bottom'),
                                                                            (v_tenant_demo,    'consent.expiry.days',     '365'),
                                                                            (v_tenant_demo,    'breach.notify.auto',      'true'),
                                                                            (v_tenant_demo,    'rights.sla.days',         '30'),
                                                                            (v_tenant_fintech, 'consent.widget.theme',    'dark'),
                                                                            (v_tenant_fintech, 'consent.expiry.days',     '180'),
                                                                            (v_tenant_fintech, 'rights.sla.days',         '30'),
                                                                            (v_tenant_health,  'consent.widget.theme',    'light'),
                                                                            (v_tenant_health,  'consent.expiry.days',     '365'),
                                                                            (v_tenant_health,  'rights.sla.days',         '30')
    ON CONFLICT (tenant_id, config_key) DO NOTHING;

-- Analytics metrics
INSERT INTO analytics.compliance_metrics (tenant_id, metric_name, metric_value, period_start, period_end) VALUES
                                                                                                              (v_tenant_demo,    'CONSENT_GRANT_RATE',    87.5,  CURRENT_DATE - 30, CURRENT_DATE),
                                                                                                              (v_tenant_demo,    'RIGHTS_SLA_COMPLIANCE', 100.0, CURRENT_DATE - 30, CURRENT_DATE),
                                                                                                              (v_tenant_demo,    'BREACH_INCIDENTS_TOTAL',1.0,   CURRENT_DATE - 30, CURRENT_DATE),
                                                                                                              (v_tenant_demo,    'VENDOR_DPA_COVERAGE',   75.0,  CURRENT_DATE - 30, CURRENT_DATE),
                                                                                                              (v_tenant_fintech, 'CONSENT_GRANT_RATE',    92.3,  CURRENT_DATE - 30, CURRENT_DATE),
                                                                                                              (v_tenant_fintech, 'RIGHTS_SLA_COMPLIANCE', 100.0, CURRENT_DATE - 30, CURRENT_DATE)
    ON CONFLICT (tenant_id, metric_name, period_start) DO NOTHING;

-- Retention policies
INSERT INTO retention.retention_policies (tenant_id, data_category, retention_days, action_on_expiry) VALUES
                                                                                                          (v_tenant_demo,    'CONSENT_RECORDS',  1095, 'ARCHIVE'),
                                                                                                          (v_tenant_demo,    'AUDIT_LOGS',       2555, 'ARCHIVE'),
                                                                                                          (v_tenant_demo,    'USER_PROFILES',    1095, 'ANONYMIZE'),
                                                                                                          (v_tenant_demo,    'TRANSACTION_DATA', 2555, 'ARCHIVE'),
                                                                                                          (v_tenant_fintech, 'CONSENT_RECORDS',  730,  'ARCHIVE'),
                                                                                                          (v_tenant_fintech, 'AUDIT_LOGS',       2555, 'ARCHIVE'),
                                                                                                          (v_tenant_fintech, 'FINANCIAL_DATA',   2555, 'ARCHIVE')
    ON CONFLICT (tenant_id, data_category) DO NOTHING;

-- SIEM integrations
INSERT INTO siem.siem_integrations (tenant_id, siem_type, endpoint, enabled) VALUES
                                                                                 (v_tenant_demo, 'SPLUNK',   'http://localhost:8088', FALSE),
                                                                                 (v_tenant_demo, 'QRADAR',   'http://localhost:8443', FALSE),
                                                                                 (v_tenant_demo, 'SENTINEL', 'http://localhost:8081', FALSE)
    ON CONFLICT (tenant_id, siem_type) DO NOTHING;

RAISE NOTICE '==============================================';
    RAISE NOTICE 'DataShield seed (random UUIDs) completed.';
    RAISE NOTICE 'Demo tenant id:    %', v_tenant_demo;
    RAISE NOTICE 'FinTech tenant id: %', v_tenant_fintech;
    RAISE NOTICE 'Health tenant id:  %', v_tenant_health;
    RAISE NOTICE '==============================================';

END $$;