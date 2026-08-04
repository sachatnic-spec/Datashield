-- =============================================================================
-- DataShield India - End-to-End Demo Seed Data
-- =============================================================================
-- Purpose:
--   Insert deterministic demo data for local end-to-end testing across the
--   current Spring services. This script targets the CURRENT schemas/tables,
--   not the older public.* layout.
--
-- Usage:
--   1. Start the services once so Hibernate creates the tables.
--   2. Run:
--      psql -U datasheild -d datasheild -f infrastructure/database/seed.sql
--
-- Notes:
--   - The script is idempotent. It uses fixed UUIDs and inserts only if the
--     target tables exist.
--   - Some services own their own schema and may not have created tables yet.
--     Those sections are skipped automatically.
--
-- Demo login:
--   Tenant ID : 11111111-1111-1111-1111-111111111111
--   Email     : dpo@example.com
--   Password  : demo1234
--
-- Extra users:
--   admin@example.com    / demo1234
--   user@example.com     / demo1234
--   auditor@example.com  / demo1234
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS tenant;
CREATE SCHEMA IF NOT EXISTS consent;
CREATE SCHEMA IF NOT EXISTS rights;
CREATE SCHEMA IF NOT EXISTS breach;
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS notification;
CREATE SCHEMA IF NOT EXISTS grievance;
CREATE SCHEMA IF NOT EXISTS vendor;
CREATE SCHEMA IF NOT EXISTS policy;
CREATE SCHEMA IF NOT EXISTS retention;
CREATE SCHEMA IF NOT EXISTS analytics;
CREATE SCHEMA IF NOT EXISTS config;
CREATE SCHEMA IF NOT EXISTS dpbi;
CREATE SCHEMA IF NOT EXISTS webhook;
CREATE SCHEMA IF NOT EXISTS siem;

DO $$
DECLARE
    v_now TIMESTAMP := CURRENT_TIMESTAMP;

    v_tenant_default UUID := '11111111-1111-1111-1111-111111111111';
    v_tenant_finance UUID := '22222222-2222-2222-2222-222222222222';

    v_user_super_admin UUID := 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1';
    v_user_tenant_admin UUID := 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2';
    v_user_dpo UUID := 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3';
    v_user_data_principal UUID := 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4';
    v_user_auditor UUID := 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5';
    v_user_finance_dpo UUID := 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1';

    v_notice_en UUID := 'cccccccc-cccc-cccc-cccc-ccccccccccc1';
    v_notice_hi UUID := 'cccccccc-cccc-cccc-cccc-ccccccccccc2';
    v_purpose_essential UUID := 'dddddddd-dddd-dddd-dddd-ddddddddddd1';
    v_purpose_marketing UUID := 'dddddddd-dddd-dddd-dddd-ddddddddddd2';
    v_purpose_analytics UUID := 'dddddddd-dddd-dddd-dddd-ddddddddddd3';
    v_purpose_support UUID := 'dddddddd-dddd-dddd-dddd-ddddddddddd4';

    v_consent_essential UUID := 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1';
    v_consent_analytics UUID := 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2';

    v_dpr_access UUID := 'ffffffff-ffff-ffff-ffff-fffffffffff1';
    v_dpr_erasure UUID := 'ffffffff-ffff-ffff-ffff-fffffffffff2';

    v_breach_demo UUID := '99999999-9999-9999-9999-999999999991';

    v_audit_event_login UUID := '12121212-1212-1212-1212-121212121211';
    v_audit_event_consent UUID := '12121212-1212-1212-1212-121212121212';
    v_audit_event_breach UUID := '12121212-1212-1212-1212-121212121213';

    v_audit_log_login UUID := '13131313-1313-1313-1313-131313131311';
    v_audit_log_consent UUID := '13131313-1313-1313-1313-131313131312';
    v_audit_log_breach UUID := '13131313-1313-1313-1313-131313131313';

    v_grievance_demo UUID := '14141414-1414-1414-1414-141414141414';

    v_vendor_cloud UUID := '15151515-1515-1515-1515-151515151511';
    v_vendor_sms UUID := '15151515-1515-1515-1515-151515151512';

    v_policy_privacy UUID := '16161616-1616-1616-1616-161616161611';
    v_policy_breach UUID := '16161616-1616-1616-1616-161616161612';

    v_retention_customer UUID := '17171717-1717-1717-1717-171717171711';
    v_retention_audit UUID := '17171717-1717-1717-1717-171717171712';

    v_metric_consent UUID := '18181818-1818-1818-1818-181818181811';
    v_metric_dsar UUID := '18181818-1818-1818-1818-181818181812';
    v_metric_grievance UUID := '18181818-1818-1818-1818-181818181813';
    v_metric_vendor UUID := '18181818-1818-1818-1818-181818181814';

    v_tenant_history_default UUID := '19191919-1919-1919-1919-191919191911';
    v_tenant_history_finance UUID := '19191919-1919-1919-1919-191919191912';
BEGIN
    IF to_regclass('tenant.tenants') IS NOT NULL THEN
        INSERT INTO tenant.tenants (
            id, name, description, tier, subscription_status, schema_name,
            provisioning_status, max_data_principals, max_consents,
            max_dpr_requests, max_storage_gb, api_rate_limit_rpm, logo_url,
            support_email, support_phone, contract_start_date, contract_end_date,
            auto_renewal, invoice_email, created_at, updated_at
        ) VALUES
        (
            v_tenant_default,
            'Default Demo Tenant',
            'Primary demo tenant for local dashboard and API testing',
            'ENTERPRISE',
            'ACTIVE',
            'default',
            'ACTIVE',
            500000,
            2000000,
            50000,
            1024,
            5000,
            'https://example.com/assets/default-tenant-logo.png',
            'support@example.com',
            '+91-9876543210',
            v_now - INTERVAL '120 days',
            v_now + INTERVAL '245 days',
            TRUE,
            'billing@example.com',
            v_now - INTERVAL '120 days',
            v_now - INTERVAL '1 day'
        ),
        (
            v_tenant_finance,
            'FinBank Sandbox',
            'Secondary finance tenant for multi-tenant testing',
            'PROFESSIONAL',
            'ACTIVE',
            'finbank',
            'ACTIVE',
            150000,
            750000,
            15000,
            256,
            2500,
            'https://example.com/assets/finbank-logo.png',
            'privacy@finbank.example',
            '+91-9988776655',
            v_now - INTERVAL '90 days',
            v_now + INTERVAL '275 days',
            TRUE,
            'finance-ops@finbank.example',
            v_now - INTERVAL '90 days',
            v_now - INTERVAL '2 days'
        )
        ON CONFLICT (id) DO NOTHING;
    ELSE
        RAISE NOTICE 'Skipping tenant.tenants - table not found';
    END IF;

    IF to_regclass('tenant.feature_flags') IS NOT NULL THEN
        INSERT INTO tenant.feature_flags (
            id, flag_name, description, tier, is_active, api_quota_per_month,
            concurrent_requests_limit, feature_value, metadata, created_at, updated_at
        ) VALUES
        (
            '20202020-2020-2020-2020-202020202021',
            'CONSENT_WIDGET_V2',
            'Enhanced consent widget with localization and purpose grouping',
            'PROFESSIONAL',
            TRUE,
            500000,
            250,
            '{"theme":"light","languages":["en","hi"]}',
            '{"owner":"product","rollout":"global"}',
            v_now - INTERVAL '60 days',
            v_now - INTERVAL '1 day'
        ),
        (
            '20202020-2020-2020-2020-202020202022',
            'AI_RISK_SCORING',
            'Risk scoring and compliance analytics features',
            'ENTERPRISE',
            TRUE,
            1000000,
            500,
            '{"provider":"internal","version":"v2"}',
            '{"owner":"ml-platform"}',
            v_now - INTERVAL '55 days',
            v_now - INTERVAL '1 day'
        ),
        (
            '20202020-2020-2020-2020-202020202023',
            'BREACH_AUTO_NOTIFY',
            'Automatic DPBI notification helpers',
            'ENTERPRISE',
            TRUE,
            250000,
            100,
            '{"dpbiDeadlineHours":72}',
            '{"owner":"security-ops"}',
            v_now - INTERVAL '45 days',
            v_now - INTERVAL '1 day'
        )
        ON CONFLICT (id) DO NOTHING;
    ELSE
        RAISE NOTICE 'Skipping tenant.feature_flags - table not found';
    END IF;

    IF to_regclass('tenant.tenant_provisioning_history') IS NOT NULL THEN
        INSERT INTO tenant.tenant_provisioning_history (
            id, tenant_id, status, action, details, error_message, stack_trace,
            executed_by, duration_ms, created_at, updated_at
        ) VALUES
        (
            v_tenant_history_default,
            v_tenant_default,
            'SUCCESS',
            'Provisioning',
            'Seeded demo tenant and core modules',
            NULL,
            NULL,
            'SYSTEM',
            2840,
            v_now - INTERVAL '119 days',
            v_now - INTERVAL '119 days'
        ),
        (
            v_tenant_history_finance,
            v_tenant_finance,
            'SUCCESS',
            'Provisioning',
            'Provisioned finance sandbox tenant',
            NULL,
            NULL,
            'SYSTEM',
            2310,
            v_now - INTERVAL '89 days',
            v_now - INTERVAL '89 days'
        )
        ON CONFLICT (id) DO NOTHING;
    END IF;

    IF to_regclass('auth.users') IS NOT NULL THEN
        INSERT INTO auth.users (
            id, tenant_id, email, username, password_hash, first_name, last_name,
            phone_number, status, mfa_enabled, preferred_mfa_method, created_at,
            updated_at, last_login_at, password_changed_at, deleted, deleted_at
        ) VALUES
        (
            v_user_super_admin,
            v_tenant_default,
            'superadmin@example.com',
            'superadmin',
            '$2a$10$H/cbC8rSxSddB8YGlkVhKesoFwghhAHvqASJE3Lcm6Lr0omJDorpa',
            'Platform',
            'Admin',
            '+91-9000000001',
            'ACTIVE',
            FALSE,
            'EMAIL',
            v_now - INTERVAL '100 days',
            v_now - INTERVAL '1 day',
            v_now - INTERVAL '2 days',
            v_now - INTERVAL '30 days',
            FALSE,
            NULL
        ),
        (
            v_user_tenant_admin,
            v_tenant_default,
            'admin@example.com',
            'tenantadmin',
            '$2a$10$H/cbC8rSxSddB8YGlkVhKesoFwghhAHvqASJE3Lcm6Lr0omJDorpa',
            'Tenant',
            'Admin',
            '+91-9000000002',
            'ACTIVE',
            FALSE,
            'EMAIL',
            v_now - INTERVAL '95 days',
            v_now - INTERVAL '1 day',
            v_now - INTERVAL '3 days',
            v_now - INTERVAL '30 days',
            FALSE,
            NULL
        ),
        (
            v_user_dpo,
            v_tenant_default,
            'dpo@example.com',
            'dpo',
            '$2a$10$H/cbC8rSxSddB8YGlkVhKesoFwghhAHvqASJE3Lcm6Lr0omJDorpa',
            'Priya',
            'Sharma',
            '+91-9000000003',
            'ACTIVE',
            FALSE,
            'EMAIL',
            v_now - INTERVAL '90 days',
            v_now - INTERVAL '1 day',
            v_now - INTERVAL '5 hours',
            v_now - INTERVAL '30 days',
            FALSE,
            NULL
        ),
        (
            v_user_data_principal,
            v_tenant_default,
            'user@example.com',
            'dataprincipal',
            '$2a$10$H/cbC8rSxSddB8YGlkVhKesoFwghhAHvqASJE3Lcm6Lr0omJDorpa',
            'Aarav',
            'Patel',
            '+91-9000000004',
            'ACTIVE',
            FALSE,
            'EMAIL',
            v_now - INTERVAL '85 days',
            v_now - INTERVAL '1 day',
            v_now - INTERVAL '2 days',
            v_now - INTERVAL '30 days',
            FALSE,
            NULL
        ),
        (
            v_user_auditor,
            v_tenant_default,
            'auditor@example.com',
            'auditor',
            '$2a$10$H/cbC8rSxSddB8YGlkVhKesoFwghhAHvqASJE3Lcm6Lr0omJDorpa',
            'Rohit',
            'Menon',
            '+91-9000000005',
            'ACTIVE',
            FALSE,
            'EMAIL',
            v_now - INTERVAL '80 days',
            v_now - INTERVAL '1 day',
            v_now - INTERVAL '6 days',
            v_now - INTERVAL '30 days',
            FALSE,
            NULL
        ),
        (
            v_user_finance_dpo,
            v_tenant_finance,
            'dpo@finbank.example',
            'finbankdpo',
            '$2a$10$H/cbC8rSxSddB8YGlkVhKesoFwghhAHvqASJE3Lcm6Lr0omJDorpa',
            'Kavita',
            'Iyer',
            '+91-9000000006',
            'ACTIVE',
            FALSE,
            'EMAIL',
            v_now - INTERVAL '70 days',
            v_now - INTERVAL '2 days',
            v_now - INTERVAL '3 days',
            v_now - INTERVAL '30 days',
            FALSE,
            NULL
        )
        ON CONFLICT (id) DO NOTHING;
    ELSE
        RAISE NOTICE 'Skipping auth.users - table not found';
    END IF;

    IF to_regclass('auth.user_roles') IS NOT NULL THEN
        INSERT INTO auth.user_roles (user_id, role)
        SELECT v_user_super_admin, 'SUPER_ADMIN'
        WHERE NOT EXISTS (
            SELECT 1 FROM auth.user_roles WHERE user_id = v_user_super_admin AND role = 'SUPER_ADMIN'
        );

        INSERT INTO auth.user_roles (user_id, role)
        SELECT v_user_tenant_admin, 'TENANT_ADMIN'
        WHERE NOT EXISTS (
            SELECT 1 FROM auth.user_roles WHERE user_id = v_user_tenant_admin AND role = 'TENANT_ADMIN'
        );

        INSERT INTO auth.user_roles (user_id, role)
        SELECT v_user_dpo, 'DPO'
        WHERE NOT EXISTS (
            SELECT 1 FROM auth.user_roles WHERE user_id = v_user_dpo AND role = 'DPO'
        );

        INSERT INTO auth.user_roles (user_id, role)
        SELECT v_user_data_principal, 'DATA_PRINCIPAL'
        WHERE NOT EXISTS (
            SELECT 1 FROM auth.user_roles WHERE user_id = v_user_data_principal AND role = 'DATA_PRINCIPAL'
        );

        INSERT INTO auth.user_roles (user_id, role)
        SELECT v_user_auditor, 'AUDITOR'
        WHERE NOT EXISTS (
            SELECT 1 FROM auth.user_roles WHERE user_id = v_user_auditor AND role = 'AUDITOR'
        );

        INSERT INTO auth.user_roles (user_id, role)
        SELECT v_user_finance_dpo, 'DPO'
        WHERE NOT EXISTS (
            SELECT 1 FROM auth.user_roles WHERE user_id = v_user_finance_dpo AND role = 'DPO'
        );
    END IF;

    IF to_regclass('config.feature_flag') IS NOT NULL THEN
        INSERT INTO config.feature_flag (
            id, tenant_id, feature_name, enabled, status, created_at, updated_at
        ) VALUES
        (
            '21212121-2121-2121-2121-212121212121',
            v_tenant_default::TEXT,
            'REDIS_ENABLED',
            TRUE,
            'ACTIVE',
            v_now - INTERVAL '60 days',
            v_now - INTERVAL '1 day'
        ),
        (
            '21212121-2121-2121-2121-212121212122',
            v_tenant_default::TEXT,
            'ML_ENABLED',
            TRUE,
            'ACTIVE',
            v_now - INTERVAL '60 days',
            v_now - INTERVAL '1 day'
        ),
        (
            '21212121-2121-2121-2121-212121212123',
            v_tenant_default::TEXT,
            'SIEM_ENABLED',
            TRUE,
            'ACTIVE',
            v_now - INTERVAL '60 days',
            v_now - INTERVAL '1 day'
        ),
        (
            '21212121-2121-2121-2121-212121212124',
            v_tenant_finance::TEXT,
            'REDIS_ENABLED',
            TRUE,
            'ACTIVE',
            v_now - INTERVAL '55 days',
            v_now - INTERVAL '2 days'
        )
        ON CONFLICT (id) DO NOTHING;
    END IF;

    IF to_regclass('consent.consent_notices') IS NOT NULL THEN
        INSERT INTO consent.consent_notices (
            id, tenant_id, version_number, language_code, notice_content,
            privacy_policy_url, status, created_at, updated_at, deprecated_at,
            data_retention_days
        ) VALUES
        (
            v_notice_en,
            v_tenant_default,
            1,
            'en',
            'We process personal data for service delivery, support, analytics, and optional marketing in line with the DPDP Act, 2023.',
            'https://example.com/privacy/default/en',
            'ACTIVE',
            v_now - INTERVAL '45 days',
            v_now - INTERVAL '2 days',
            NULL,
            365
        ),
        (
            v_notice_hi,
            v_tenant_default,
            1,
            'hi',
            'Hum seva dene, support, analytics aur ichchhik marketing ke liye vyaktigat data process karte hain.',
            'https://example.com/privacy/default/hi',
            'ACTIVE',
            v_now - INTERVAL '45 days',
            v_now - INTERVAL '2 days',
            NULL,
            365
        )
        ON CONFLICT (id) DO NOTHING;
    END IF;

    IF to_regclass('consent.consent_purposes') IS NOT NULL THEN
        INSERT INTO consent.consent_purposes (
            id, tenant_id, purpose_code, purpose_name, description, status,
            retention_days, requires_audit, created_at, updated_at, retired_at
        ) VALUES
        (
            v_purpose_essential,
            v_tenant_default,
            'ESSENTIAL',
            'Essential Processing',
            'Required to provide the platform and maintain user accounts',
            'ACTIVE',
            365,
            TRUE,
            v_now - INTERVAL '45 days',
            v_now - INTERVAL '2 days',
            NULL
        ),
        (
            v_purpose_marketing,
            v_tenant_default,
            'MARKETING',
            'Marketing Communication',
            'Optional product updates, newsletters, and feature launches',
            'ACTIVE',
            180,
            TRUE,
            v_now - INTERVAL '45 days',
            v_now - INTERVAL '2 days',
            NULL
        ),
        (
            v_purpose_analytics,
            v_tenant_default,
            'ANALYTICS',
            'Usage Analytics',
            'Aggregate analytics to improve consent and DSAR journeys',
            'ACTIVE',
            180,
            TRUE,
            v_now - INTERVAL '45 days',
            v_now - INTERVAL '2 days',
            NULL
        ),
        (
            v_purpose_support,
            v_tenant_default,
            'SUPPORT',
            'Customer Support',
            'Case handling, service diagnostics, and grievance resolution',
            'ACTIVE',
            365,
            TRUE,
            v_now - INTERVAL '45 days',
            v_now - INTERVAL '2 days',
            NULL
        )
        ON CONFLICT (id) DO NOTHING;
    END IF;

    IF to_regclass('consent.consent_records') IS NOT NULL THEN
        INSERT INTO consent.consent_records (
            id, tenant_id, data_principal_id, purpose_id, status, ip_address,
            device_fingerprint, channel, metadata, granted_at, last_modified_at,
            withdrawn_at, expires_at, withdrawal_reason, audit_logged
        ) VALUES
        (
            v_consent_essential,
            v_tenant_default,
            v_user_data_principal,
            v_purpose_essential,
            'GRANTED',
            '127.0.0.1',
            'browser-demo-device-001',
            'WEB',
            '{"locale":"en-IN","source":"seed"}',
            v_now - INTERVAL '20 days',
            v_now - INTERVAL '20 days',
            NULL,
            v_now + INTERVAL '345 days',
            NULL,
            TRUE
        ),
        (
            v_consent_analytics,
            v_tenant_default,
            v_user_data_principal,
            v_purpose_analytics,
            'GRANTED',
            '127.0.0.1',
            'browser-demo-device-001',
            'WEB',
            '{"locale":"en-IN","source":"seed"}',
            v_now - INTERVAL '12 days',
            v_now - INTERVAL '12 days',
            NULL,
            v_now + INTERVAL '168 days',
            NULL,
            TRUE
        )
        ON CONFLICT (id) DO NOTHING;
    END IF;

    IF to_regclass('rights.dpr_requests') IS NOT NULL THEN
        INSERT INTO rights.dpr_requests (
            id, tenant_id, data_principal_id, request_type, status, channel,
            request_metadata, request_details, created_at, updated_at,
            sla_deadline, verified_at, completed_at, verification_challenge_id,
            identity_verified, rejection_reason, activity_count
        ) VALUES
        (
            v_dpr_access,
            v_tenant_default,
            v_user_data_principal,
            'ACCESS',
            'PROCESSING',
            'WEB',
            '{"locale":"en-IN","source":"dashboard"}',
            'Access request for all profile, consent, and grievance data',
            v_now - INTERVAL '6 days',
            v_now - INTERVAL '1 day',
            v_now + INTERVAL '24 days',
            v_now - INTERVAL '5 days',
            NULL,
            NULL,
            TRUE,
            NULL,
            3
        ),
        (
            v_dpr_erasure,
            v_tenant_default,
            v_user_data_principal,
            'ERASURE',
            'VERIFICATION_PENDING',
            'EMAIL',
            '{"locale":"en-IN","source":"support"}',
            'Erase archived marketing profile data no longer required',
            v_now - INTERVAL '2 days',
            v_now - INTERVAL '4 hours',
            v_now + INTERVAL '28 days',
            NULL,
            NULL,
            NULL,
            FALSE,
            NULL,
            1
        )
        ON CONFLICT (id) DO NOTHING;
    END IF;

    IF to_regclass('breach.breach_incidents') IS NOT NULL THEN
        INSERT INTO breach.breach_incidents (
            id, tenant_id, status, severity, incident_title, incident_description,
            affected_systems, estimated_data_subjects, estimated_records,
            data_categories, discovered_at, reported_at, dpbi_notified_at,
            dp_notified_at, processor_notified_at, contained_at, resolved_at,
            dpbi_deadline, root_cause, loss_of_confidentiality, loss_of_integrity,
            loss_of_availability, containment_strategy, dpbi_form_id, created_at, updated_at
        ) VALUES
        (
            v_breach_demo,
            v_tenant_default,
            'RESOLVED',
            'P2',
            'Demo phishing incident',
            'Suspicious mailbox access was detected and contained without evidence of large-scale exfiltration.',
            'mail-gateway, helpdesk-portal',
            42,
            128,
            '["contact","support-ticket","device"]',
            v_now - INTERVAL '10 days',
            v_now - INTERVAL '10 days',
            v_now - INTERVAL '8 days 20 hours',
            v_now - INTERVAL '8 days 18 hours',
            v_now - INTERVAL '9 days',
            v_now - INTERVAL '9 days 20 hours',
            v_now - INTERVAL '7 days',
            v_now - INTERVAL '7 days',
            'Compromised password reused by support agent',
            TRUE,
            FALSE,
            FALSE,
            'Disabled credentials, reset sessions, and tightened mailbox access rules',
            NULL,
            v_now - INTERVAL '10 days',
            v_now - INTERVAL '7 days'
        )
        ON CONFLICT (id) DO NOTHING;
    END IF;

    IF to_regclass('audit.audit_events') IS NOT NULL THEN
        INSERT INTO audit.audit_events (
            id, tenant_id, correlation_id, source_service, entity_type, event_type,
            entity_id, event_payload, actor_id, actor_role, ip_address, user_agent,
            created_at, previous_state, current_state
        ) VALUES
        (
            v_audit_event_login,
            v_tenant_default,
            'corr-login-001',
            'auth-service',
            'USER',
            'LOGIN_SUCCESS',
            v_user_dpo,
            '{"email":"dpo@example.com"}',
            v_user_dpo::TEXT,
            'DPO',
            '127.0.0.1',
            'Mozilla/5.0 demo-seed',
            v_now - INTERVAL '5 hours',
            '{"status":"ACTIVE"}',
            '{"status":"ACTIVE","lastLoginAt":"updated"}'
        ),
        (
            v_audit_event_consent,
            v_tenant_default,
            'corr-consent-001',
            'consent-service',
            'CONSENT',
            'CONSENT_GRANTED',
            v_consent_analytics,
            '{"purpose":"ANALYTICS","channel":"WEB"}',
            v_user_data_principal::TEXT,
            'DATA_PRINCIPAL',
            '127.0.0.1',
            'Mozilla/5.0 demo-seed',
            v_now - INTERVAL '12 days',
            '{"status":"PENDING"}',
            '{"status":"GRANTED"}'
        ),
        (
            v_audit_event_breach,
            v_tenant_default,
            'corr-breach-001',
            'breach-service',
            'BREACH',
            'BREACH_REPORTED',
            v_breach_demo,
            '{"severity":"P2"}',
            v_user_dpo::TEXT,
            'DPO',
            '127.0.0.1',
            'Mozilla/5.0 demo-seed',
            v_now - INTERVAL '10 days',
            '{"status":"REPORTED"}',
            '{"status":"RESOLVED"}'
        )
        ON CONFLICT (id) DO NOTHING;
    END IF;

    IF to_regclass('audit.audit_logs') IS NOT NULL THEN
        INSERT INTO audit.audit_logs (
            id, tenant_id, user_id, entity_type, entity_id, action, old_values,
            new_values, ip_address, user_agent, created_at, archived, archived_at,
            audit_event_id, event_summary, file_size_bytes, hash_chain_valid,
            previous_event_hash, s3object_key, sha256hash
        ) VALUES
        (
            v_audit_log_login,
            v_tenant_default,
            v_user_dpo,
            'USER',
            v_user_dpo::TEXT,
            'LOGIN_SUCCESS',
            '{"status":"ACTIVE"}',
            '{"status":"ACTIVE","lastLoginAt":"updated"}',
            '127.0.0.1',
            'Mozilla/5.0 demo-seed',
            v_now - INTERVAL '5 hours',
            FALSE,
            NULL,
            v_audit_event_login,
            'DPO login successful from dashboard',
            2048,
            'VALID',
            NULL,
            'audit/default/login-001.json',
            'sha256-login-001'
        ),
        (
            v_audit_log_consent,
            v_tenant_default,
            v_user_data_principal,
            'CONSENT',
            v_consent_analytics::TEXT,
            'CONSENT_GRANTED',
            '{"status":"PENDING"}',
            '{"status":"GRANTED"}',
            '127.0.0.1',
            'Mozilla/5.0 demo-seed',
            v_now - INTERVAL '12 days',
            FALSE,
            NULL,
            v_audit_event_consent,
            'Analytics consent granted by demo data principal',
            1850,
            'VALID',
            'sha256-login-001',
            'audit/default/consent-001.json',
            'sha256-consent-001'
        ),
        (
            v_audit_log_breach,
            v_tenant_default,
            v_user_dpo,
            'BREACH',
            v_breach_demo::TEXT,
            'BREACH_REPORTED',
            '{"status":"REPORTED"}',
            '{"status":"RESOLVED"}',
            '127.0.0.1',
            'Mozilla/5.0 demo-seed',
            v_now - INTERVAL '10 days',
            FALSE,
            NULL,
            v_audit_event_breach,
            'Demo breach lifecycle captured for reporting tests',
            4096,
            'VALID',
            'sha256-consent-001',
            'audit/default/breach-001.json',
            'sha256-breach-001'
        )
        ON CONFLICT (id) DO NOTHING;
    END IF;

    IF to_regclass('notification.notification_templates') IS NOT NULL THEN
        INSERT INTO notification.notification_templates (
            id, tenant_id, template_code, event_type, subject, body, status,
            created_at, updated_at
        ) VALUES
        (
            '23232323-2323-2323-2323-232323232321',
            v_tenant_default,
            'CONSENT_GRANTED_EMAIL',
            'CONSENT_GRANTED',
            'Your consent was recorded',
            'Hello {{firstName}}, your consent for {{purpose}} was recorded successfully.',
            'ACTIVE',
            v_now - INTERVAL '40 days',
            v_now - INTERVAL '1 day'
        ),
        (
            '23232323-2323-2323-2323-232323232322',
            v_tenant_default,
            'DPR_SUBMITTED_EMAIL',
            'DPR_SUBMITTED',
            'We received your DPDP request',
            'Hello {{firstName}}, your request {{requestId}} is now being processed.',
            'ACTIVE',
            v_now - INTERVAL '40 days',
            v_now - INTERVAL '1 day'
        ),
        (
            '23232323-2323-2323-2323-232323232323',
            v_tenant_default,
            'BREACH_NOTIFIED_DPBI_EMAIL',
            'BREACH_NOTIFIED_DPBI',
            'DPBI breach notification submitted',
            'The breach incident {{incidentId}} has been submitted to DPBI.',
            'ACTIVE',
            v_now - INTERVAL '40 days',
            v_now - INTERVAL '1 day'
        )
        ON CONFLICT (id) DO NOTHING;
    END IF;

    IF to_regclass('grievance.grievance') IS NOT NULL THEN
        INSERT INTO grievance.grievance (
            id, tenant_id, data_principal_id, category, channel, subject,
            description, status, priority, filed_at, sla_deadline,
            investigation_started_at, resolved_at, assigned_to, resolution,
            escalation_reason, created_at, updated_at
        ) VALUES
        (
            v_grievance_demo,
            v_tenant_default,
            v_user_data_principal,
            'SLA_BREACH',
            'WEB',
            'Status update needed on access request',
            'Data principal asked for an update because the access request appears stalled.',
            'INVESTIGATING',
            'HIGH',
            v_now - INTERVAL '4 days',
            v_now + INTERVAL '26 days',
            v_now - INTERVAL '3 days',
            NULL,
            'dpo@example.com',
            NULL,
            NULL,
            v_now - INTERVAL '4 days',
            v_now - INTERVAL '12 hours'
        )
        ON CONFLICT (id) DO NOTHING;
    END IF;

    IF to_regclass('vendor.vendors') IS NOT NULL THEN
        INSERT INTO vendor.vendors (
            id, name, description, vendor_type, status, website, contact_email,
            contact_phone, data_processor_role, country_of_operation, data_categories,
            processing_purposes, data_retention_policy, has_dpa, dpa_signed_date,
            risk_score, risk_level, risk_last_assessed, audit_notes, created_at, updated_at
        ) VALUES
        (
            v_vendor_cloud,
            'CloudHost India',
            'Cloud infrastructure partner for app hosting and backups',
            'INFRASTRUCTURE',
            'ACTIVE',
            'https://cloudhost.example',
            'privacy@cloudhost.example',
            '+91-8040001000',
            'Hosting and secure storage',
            'India',
            'contact,account,consent,audit',
            'hosting,backup,disaster-recovery',
            'Encrypted backups retained for 365 days',
            TRUE,
            v_now - INTERVAL '180 days',
            22,
            'LOW',
            v_now - INTERVAL '7 days',
            'Annual assessment passed.',
            v_now - INTERVAL '180 days',
            v_now - INTERVAL '7 days'
        ),
        (
            v_vendor_sms,
            'NotifySMS',
            'Transactional SMS delivery provider',
            'COMMUNICATION',
            'ACTIVE',
            'https://notifysms.example',
            'support@notifysms.example',
            '+91-8040002000',
            'OTP and user notification delivery',
            'India',
            'phone,notification-log',
            'otp,alerts,transactional-messaging',
            'Message logs retained for 90 days',
            TRUE,
            v_now - INTERVAL '150 days',
            48,
            'MEDIUM',
            v_now - INTERVAL '10 days',
            'DPA signed; monitor delivery failure spikes.',
            v_now - INTERVAL '150 days',
            v_now - INTERVAL '10 days'
        )
        ON CONFLICT (id) DO NOTHING;
    END IF;

    IF to_regclass('policy.policies') IS NOT NULL THEN
        INSERT INTO policy.policies (
            id, tenant_id, name, description, policy_type, rules, is_active,
            created_at, updated_at
        ) VALUES
        (
            v_policy_privacy,
            v_tenant_default,
            'Privacy Notice Handling Policy',
            'Rules for consent notice publication, update, and localization',
            'PRIVACY',
            '{"minLanguages":["en"],"noticeVersioning":true,"consentExpiryDays":365}',
            TRUE,
            v_now - INTERVAL '60 days',
            v_now - INTERVAL '1 day'
        ),
        (
            v_policy_breach,
            v_tenant_default,
            'Breach Escalation Policy',
            'Operational policy for breach triage and DPBI notification timelines',
            'SECURITY',
            '{"dpbiDeadlineHours":72,"internalEscalationHours":2,"notifyDataPrincipals":true}',
            TRUE,
            v_now - INTERVAL '45 days',
            v_now - INTERVAL '1 day'
        )
        ON CONFLICT (id) DO NOTHING;
    END IF;

    IF to_regclass('retention.retention_policy') IS NOT NULL THEN
        INSERT INTO retention.retention_policy (
            id, policy_name, sector, data_category, retention_days_default,
            retention_days_max, status, disposal_method, requires_approval,
            approved_by, created_at, updated_at
        ) VALUES
        (
            v_retention_customer,
            'Customer Profile Retention',
            'GENERAL',
            'CUSTOMER_PROFILE',
            365,
            1095,
            'ACTIVE',
            'ANONYMIZE',
            TRUE,
            'dpo@example.com',
            v_now - INTERVAL '50 days',
            v_now - INTERVAL '2 days'
        ),
        (
            v_retention_audit,
            'Audit Evidence Retention',
            'GENERAL',
            'AUDIT_LOG',
            1825,
            3650,
            'ACTIVE',
            'ARCHIVE',
            TRUE,
            'auditor@example.com',
            v_now - INTERVAL '50 days',
            v_now - INTERVAL '2 days'
        )
        ON CONFLICT (id) DO NOTHING;
    END IF;

    IF to_regclass('analytics.compliance_metric') IS NOT NULL THEN
        INSERT INTO analytics.compliance_metric (
            id, tenant_id, metric_type, metric_value, unit, measured_at, status,
            compliance_section, created_at, updated_at
        ) VALUES
        (
            v_metric_consent,
            v_tenant_default,
            'CONSENT_GRANT_RATE',
            91.2,
            'PERCENT',
            v_now - INTERVAL '1 day',
            'OK',
            'Section 5',
            v_now - INTERVAL '1 day',
            v_now - INTERVAL '1 day'
        ),
        (
            v_metric_dsar,
            v_tenant_default,
            'DSAR_PROCESSING_TIME',
            4.8,
            'DAYS',
            v_now - INTERVAL '1 day',
            'OK',
            'Section 13',
            v_now - INTERVAL '1 day',
            v_now - INTERVAL '1 day'
        ),
        (
            v_metric_grievance,
            v_tenant_default,
            'GRIEVANCE_SLA_COMPLIANCE',
            96.0,
            'PERCENT',
            v_now - INTERVAL '1 day',
            'WARNING',
            'Section 13',
            v_now - INTERVAL '1 day',
            v_now - INTERVAL '1 day'
        ),
        (
            v_metric_vendor,
            v_tenant_finance,
            'VENDOR_RISK_SCORE',
            72.5,
            'SCORE',
            v_now - INTERVAL '1 day',
            'OK',
            'Section 8',
            v_now - INTERVAL '1 day',
            v_now - INTERVAL '1 day'
        )
        ON CONFLICT (id) DO NOTHING;
    END IF;

    RAISE NOTICE '=====================================================';
    RAISE NOTICE 'DataShield E2E seed completed';
    RAISE NOTICE 'Tenant ID : %', v_tenant_default;
    RAISE NOTICE 'Login     : dpo@example.com / demo1234';
    RAISE NOTICE 'Users     : admin@example.com, user@example.com, auditor@example.com';
    RAISE NOTICE '=====================================================';
END $$;
