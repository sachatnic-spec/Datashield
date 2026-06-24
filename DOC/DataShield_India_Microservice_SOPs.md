# DataShield India
## Standard Operating Procedure — Per-Microservice Reference

**Document Version:** 1.0 · **Classification:** Confidential — Internal Engineering
**Scope:** All 27 production microservices. Each entry is the operational source of truth for the team owning that service.
**Companion Documents:** SRS v1.0, Architecture Document v1.0

---

## How to Use This Document
Every service follows the same 15-field template: **Purpose · Responsibilities · Database · Tables · REST APIs · Events Produced · Events Consumed · External Dependencies · Retry Logic · Error Codes · Security · Caching · Indexes · Performance Targets · Rate Limits**. Services are grouped by domain to match the Architecture Document's microservice boundaries.

## Table of Contents
1. Core Compliance Domain (7 services)
2. Data Intelligence Domain (3 services)
3. AI Services Domain (4 services)
4. Platform Domain (5 services)
5. Reporting Domain (2 services)
6. Integration Domain (4 services)
7. Infrastructure Domain (2 services)

---

# 1. Core Compliance Domain

## 1.1 consent-service

**Purpose:** System of record for the full consent lifecycle under DPDP §6, §7, §9.

**Responsibilities:**
- Collect, validate, and version consent per processing purpose
- Detect and reject bundled-purpose consent requests
- Issue and validate consent tokens (JWT)
- Orchestrate minor/parental consent verification

**Database:** PostgreSQL 15, schema-per-tenant, hash-partitioned by `tenant_id` (8 partitions)

**Tables:** `consent_records`, `consent_purposes`, `consent_notices`, `consent_audit_outbox`

**REST APIs:**
- `POST /v1/consent-records` — create consent
- `POST /v1/consent-records/{id}/withdraw` — withdraw consent
- `GET /v1/consent-records?dpId={hash}` — list consent summary
- `PATCH /v1/consent-purposes/{id}` — update purpose metadata (admin)

**Events Produced:** `consent.granted`, `consent.withdrawn`, `consent.expired`

**Events Consumed:** `policy.version.changed` (triggers re-consent flow)

**External Dependencies:** DigiLocker/UIDAI (age verification), policy-service (active notice version)

**Retry Logic:** Outbox polling publisher retries Kafka publish every 5s with exponential backoff (max 5 attempts, then DLQ + PagerDuty); DigiLocker calls wrapped in Resilience4j circuit breaker (3 failures/10s → open), falling back to manual DOB capture.

**Error Codes:** `400` bundled-purpose rejection · `409` duplicate active consent · `423` withdrawal blocked by legal hold · `429` tenant rate limit

**Security:** Scopes `consent:read`, `consent:write`, `consent:withdraw`; mTLS via Istio sidecar; raw PII rejected at API edge (hashed identifiers only)

**Caching:** Rendered notices `{tenant}:consent:notice:{lang}:{v}` (1h, cache-aside); active-consent DP summary (5 min)

**Indexes:** `idx_consent_dp (tenant_id, data_principal_id, status)`, `idx_consent_purpose (purpose_id, status)`

**Performance Targets:** p95 < 150ms, p99 < 400ms; sustain 100,000 TPS at peak; widget render < 100ms

**Rate Limits:** Starter 100/min · Professional 1,000/min · Enterprise 10,000/min · Government custom SLA

---

## 1.2 rights-service

**Purpose:** Orchestrates fulfillment of all Data Principal Rights — access, correction, erasure, nomination, grievance routing.

**Responsibilities:**
- Intake and SLA-track DSAR requests across channels
- Coordinate identity verification before fulfillment
- Run the multi-system erasure Saga
- Generate structured (JSON/PDF/CSV) access responses

**Database:** PostgreSQL 15, schema-per-tenant

**Tables:** `dpr_requests`, `dpr_activities`, `dpr_aggregated_data` (transient, TTL-purged post-delivery)

**REST APIs:**
- `POST /v1/dpr-requests` — submit rights request
- `GET /v1/dpr-requests/{requestNumber}` — status + activity timeline
- `POST /v1/dpr-requests/{id}/approve` — DPO approval (correction/access)
- `POST /v1/dpr-requests/{id}/erasure/certificate` — fetch erasure certificate

**Events Produced:** `dpr.request.submitted`, `dpr.erasure.initiated`, `dpr.erasure.completed`

**Events Consumed:** `dpr.erasure.ack`, `dpr.erasure.failed` (per connected system, Saga participants)

**External Dependencies:** auth-service (identity verification), connector-service (source-system aggregation), retention-service (legal hold check)

**Retry Logic:** Saga orchestrator (Temporal.io) with 48-hour timeout per erasure; failed-system compensation logs the failure and alerts DPO rather than rolling back successful deletions; aggregation calls to connectors retried 3× with jittered backoff.

**Error Codes:** `404` unknown request number · `409` duplicate submission · `422` action attempted out of allowed state transition · `423` erasure blocked by legal hold

**Security:** Scopes `dpr:read`, `dpr:write`, `dpr:fulfill`; identity verification (OTP minimum, Aadhaar/video-KYC for high-sensitivity erasure) is a hard precondition, enforced server-side not client-side

**Caching:** Request status (5 min, cache-aside, invalidated on activity write)

**Indexes:** `idx_dpr_tenant_status (tenant_id, status, sla_deadline)`, `idx_dpr_dp (data_principal_id)`

**Performance Targets:** p95 < 200ms (status reads); bulk export (<1M records) < 5 min; Saga completion tracked against 30-day statutory SLA

**Rate Limits:** Standard tier limits; bulk/class-action requests routed to async path, exempt from synchronous rate limit

---

## 1.3 breach-service

**Purpose:** System of record for breach incident lifecycle and the 72-hour DPBI notification obligation.

**Responsibilities:**
- Incident intake (manual + SIEM webhook)
- Severity classification and countdown management
- Containment action logging
- Remediation task tracking

**Database:** PostgreSQL 15

**Tables:** `breach_incidents`, `breach_containment_actions`, `breach_remediation_tasks`

**REST APIs:**
- `POST /v1/breach-incidents` — create incident
- `POST /v1/breach-incidents/{id}/containment-actions` — log containment step
- `GET /v1/breach-incidents/{id}` — incident detail + AI severity assessment
- `POST /v1/breach-incidents/{id}/dpbi-notification` — submit DPBI notification (via dpbi-service)

**Events Produced:** `breach.incident.created`, `breach.dpbi.notified`, `breach.remediation.completed`

**Events Consumed:** `siem.alert.raised` (from siem-service)

**External Dependencies:** risk-scoring-service (severity/harm assessment), dpbi-service (regulatory submission), notification-service (DP alerts)

**Retry Logic:** DPBI submission retried with backoff (max 5 attempts); terminal failure triggers P0 PagerDuty page to DPO + Legal with documented manual-submission fallback.

**Error Codes:** `422` DPBI submission without `dpoConfirmation` · `409` incident already closed · `400` invalid severity enum

**Security:** Scopes `breach:read`, `breach:write`; DPBI submission requires dual confirmation (4-eyes: system + explicit DPO flag)

**Caching:** 72-hour countdown state in Redis with TTL-based expiry alerting

**Indexes:** `idx_breach_tenant_status (tenant_id, status, dpbi_notification_deadline)`, `idx_breach_severity (severity, discovered_at)`

**Performance Targets:** Incident creation < 500ms; countdown alert delivery < 1 min of threshold crossing

**Rate Limits:** Not consumer-rate-limited (internal/admin-driven volume is low); SIEM webhook ingestion capped at 100 events/min/tenant to prevent alert-storm overload

---

## 1.4 vendor-service

**Purpose:** Manages the data processor/vendor relationship lifecycle, DPA execution, and third-party risk.

**Responsibilities:**
- Vendor onboarding and profile management
- DPA lifecycle (negotiation → execution → renewal)
- AI-assisted vendor risk scoring
- Cross-border transfer tracking

**Database:** PostgreSQL 15

**Tables:** `vendors`, `vendor_dpas`, `vendor_risk_assessments`, `cross_border_transfers`

**REST APIs:**
- `POST /v1/vendors` — onboard vendor
- `POST /v1/vendors/{id}/dpa` — create/update DPA
- `GET /v1/vendors/{id}/risk-score` — current risk score + explanation
- `POST /v1/vendors/{id}/assessments` — submit DPDP questionnaire

**Events Produced:** `vendor.onboarded`, `vendor.dpa.signed`, `vendor.risk.updated`

**Events Consumed:** `ai.risk.scored` (from risk-scoring-service)

**External Dependencies:** Leegality/DigiSign (e-signature), risk-scoring-service

**Retry Logic:** E-signature webhook processed idempotently keyed on document ID; nightly reconciliation job cross-checks DPA status against the e-signature provider's API to catch missed webhooks.

**Error Codes:** `409` DPA already executed · `404` vendor not found · `400` malformed risk questionnaire payload

**Security:** Scope `vendor:write` restricted to DPO/Privacy Manager/Procurement roles

**Caching:** Vendor risk score (1h, cache-aside)

**Indexes:** `idx_vendor_tenant (tenant_id, status)`, `idx_dpa_expiry (expiry_at)`

**Performance Targets:** p95 < 250ms; risk score recompute < 10s post-assessment submission

**Rate Limits:** Standard tier limits

---

## 1.5 policy-service

**Purpose:** Version-controlled privacy policy management with AI-assisted gap analysis and multi-language notice generation.

**Responsibilities:**
- Policy versioning and change-impact analysis
- Notice builder (drag-and-drop, 22-language templates)
- Orchestrate AI gap-analysis requests

**Database:** PostgreSQL 15

**Tables:** `policies`, `policy_versions`, `policy_translations`

**REST APIs:**
- `POST /v1/policies` — create policy
- `POST /v1/policies/{id}/versions` — publish new version
- `POST /v1/policies/{id}/gap-analysis` — trigger AI analysis
- `GET /v1/policies/{id}/translations/{lang}` — fetch localized notice

**Events Produced:** `policy.version.changed`, `policy.gap.analysis.completed`

**Events Consumed:** none (upstream of the pipeline)

**External Dependencies:** ai-analysis-service (gap analysis, translation generation)

**Retry Logic:** AI gap-analysis call has a 10s timeout with graceful degradation to `"manual review required"` status rather than blocking publish.

**Error Codes:** `409` version conflict (concurrent edit) · `422` publish attempted with unresolved HIGH-severity gap unacknowledged

**Security:** Scope `policy:write` restricted to Legal Reviewer/DPO; publish action requires Legal approval workflow state

**Caching:** Active policy version per tenant (15 min)

**Indexes:** `idx_policy_tenant_active (tenant_id, is_active)`

**Performance Targets:** Gap analysis result < 10s p95; translation generation < 15s p95

**Rate Limits:** Standard tier limits

---

## 1.6 retention-service

**Purpose:** Enforces data retention schedules and legal hold precedence platform-wide.

**Responsibilities:**
- Scheduled retention-driven deletion dispatch
- Legal hold creation, conflict detection, and release workflow
- Retention policy configuration per data type

**Database:** PostgreSQL 15

**Tables:** `retention_policies`, `legal_holds`, `deletion_jobs`

**REST APIs:**
- `POST /v1/legal-holds` — create hold
- `POST /v1/legal-holds/{id}/release` — release hold (dual-approval)
- `GET /v1/retention-policies` — list active policies
- `POST /v1/deletion-jobs/{id}/retry` — retry failed deletion job

**Events Produced:** `retention.conflict.flagged`, `retention.deletion.completed`

**Events Consumed:** `consent.withdrawn`, `dpr.erasure.completed` (trigger purge scheduling)

**External Dependencies:** Apache Airflow (scheduled job execution)

**Retry Logic:** Deletion jobs retried 3× with backoff; persistent failure routes to a manual-intervention queue rather than silently abandoning the job.

**Error Codes:** `423` deletion blocked by active hold · `403` hold release attempted without dual-approval (Legal Head + DPO)

**Security:** Hold release requires two distinct authorized approvers — enforced server-side as a state-machine precondition, not a UI checkbox

**Caching:** None (correctness-critical path; always reads current hold state)

**Indexes:** `idx_holds_active (tenant_id, status)`, `idx_deletion_jobs_scheduled (scheduled_at, status)`

**Performance Targets:** Legal hold check < 50ms (synchronous precondition on every deletion path)

**Rate Limits:** Internal/admin-driven; not externally rate-limited

---

## 1.7 grievance-service

**Purpose:** Manages grievance intake, internal routing, and DPBI escalation under DPDP §14.

**Responsibilities:**
- Grievance intake and categorization
- SLA-based escalation routing
- DPBI escalation pathway management

**Database:** PostgreSQL 15

**Tables:** `grievances`, `grievance_escalations`

**REST APIs:**
- `POST /v1/grievances` — file grievance
- `GET /v1/grievances/{id}` — status
- `POST /v1/grievances/{id}/escalate` — manual escalation to DPBI

**Events Produced:** `grievance.filed`, `grievance.escalated.dpbi`

**Events Consumed:** none

**External Dependencies:** dpbi-service (escalation submission)

**Retry Logic:** Escalation submission retried with backoff identical to breach-service's DPBI pattern; failure pages DPO + Legal.

**Error Codes:** `409` grievance already resolved · `403` manual override attempted without dual-approval

**Security:** Scope `grievance:write`; resolution requires DPO sign-off

**Caching:** None

**Indexes:** `idx_grievance_tenant_sla (tenant_id, status, sla_deadline)`

**Performance Targets:** SLA breach detection < 1 min of threshold crossing; 30-day statutory SLA tracked per grievance

**Rate Limits:** Standard tier limits


---

# 2. Data Intelligence Domain

## 2.1 discovery-service

**Purpose:** Discovers PII across connected databases, file systems, and object storage.

**Responsibilities:**
- Scheduled and on-demand scan execution
- Source-system connection health monitoring
- Throttled, checkpointed scanning to avoid overloading source systems

**Database:** PostgreSQL 15

**Tables:** `discovery_scans`, `discovery_findings` (raw, pre-classification)

**REST APIs:**
- `POST /v1/discovery-scans` — trigger scan
- `GET /v1/discovery-scans/{id}` — scan status/progress
- `GET /v1/discovery-findings?scanId={id}` — raw findings

**Events Produced:** `discovery.scan.completed`, `discovery.finding.raised`

**Events Consumed:** none (pipeline entry point)

**External Dependencies:** PostgreSQL/MySQL/Oracle/MSSQL/MongoDB/DynamoDB/Cassandra connectors, S3/GCS/SFTP scanners (via connector-service)

**Retry Logic:** Scans run as Kubernetes Jobs with checkpointing; partial-scan resume on pod eviction; token-bucket throttling caps QPS per source connector (configurable per system to respect source-system capacity).

**Error Codes:** `503` source system unreachable (scan paused, not failed) · `408` scan timeout on oversized dataset (auto-chunked retry)

**Security:** Scan credentials stored in AWS Secrets Manager, never in scan configuration records; read-only DB roles enforced on all scanner connections

**Caching:** None (scan results are persisted, not cached)

**Indexes:** `idx_scans_tenant_status (tenant_id, status, scheduled_at)`

**Performance Targets:** 1M records/hour scan throughput; scan job resume < 30s after pod eviction

**Rate Limits:** Configurable per-connector QPS cap (protects source systems, not API consumers)

---

## 2.2 classification-service

**Purpose:** Classifies discovered data against the DPDP Schedule §2(t) sensitive-category taxonomy with confidence scoring.

**Responsibilities:**
- Entity classification (delegates inference to pii-detection-service)
- Confidence scoring and false-positive management
- Human-review routing for low-confidence findings

**Database:** PostgreSQL 15

**Tables:** `pii_classifications`, `false_positive_overrides`

**REST APIs:**
- `POST /v1/classifications` — classify a finding
- `POST /v1/classifications/{id}/override` — mark false positive
- `GET /v1/classifications?scanId={id}` — list classified results

**Events Produced:** `classification.completed`

**Events Consumed:** `discovery.finding.raised`

**External Dependencies:** pii-detection-service

**Retry Logic:** Inference call retried 2× on timeout before routing to human-review queue rather than failing the pipeline stage.

**Error Codes:** `422` classification confidence below 0.85 — auto-routed to review, not an error condition surfaced to the caller

**Security:** Scope `discovery:read`/`discovery:write`

**Caching:** None (classification results persisted as the system of record)

**Indexes:** `idx_classification_finding (finding_id)`, `idx_classification_confidence (confidence_score)`

**Performance Targets:** p95 classification latency < 300ms per finding (batched)

**Rate Limits:** Internal pipeline service; not externally rate-limited

---

## 2.3 lineage-service

**Purpose:** Builds data flow lineage and auto-generates the Data Processing Activity Register (DPAR).

**Responsibilities:**
- Lineage graph construction from classified findings
- DPAR auto-generation and export
- Data flow diagram export

**Database:** PostgreSQL 15 (graph stored as adjacency table + materialized view)

**Tables:** `data_flows`, `dpar_records`

**REST APIs:**
- `GET /v1/lineage/dpar` — generate/fetch current DPAR
- `GET /v1/lineage/flow-diagram` — export flow visualization data

**Events Produced:** none (terminal pipeline stage)

**Events Consumed:** `classification.completed`

**External Dependencies:** none beyond internal pipeline

**Retry Logic:** DPAR generation is idempotent and re-runnable; partial graph data still produces a `"draft"` DPAR flagged incomplete rather than blocking.

**Error Codes:** none service-specific beyond standard 4xx/5xx

**Security:** Scope `discovery:read`

**Caching:** Materialized DPAR view refreshed on a schedule (15 min) rather than computed per request

**Indexes:** `idx_flows_source_dest (source_system, destination_system)`

**Performance Targets:** DPAR generation < 30s for a tenant with <10,000 findings

**Rate Limits:** Standard tier limits

---

# 3. AI Services Domain

## 3.1 ai-analysis-service

**Purpose:** LLM-powered policy gap analysis, consent notice generation, and legal summarization via RAG.

**Responsibilities:**
- RAG retrieval against the DPDP Act/Rules/case-law corpus (Qdrant)
- Prompt orchestration (LangChain/LangGraph, 200+ DPDP-specific prompts)
- Guardrail validation (citation hallucination checks)

**Database:** PostgreSQL 15 (results); Qdrant (vector store, not relational)

**Tables:** `ai_analysis_results`

**REST APIs:**
- `POST /v1/ai/policy-gap-analysis` — analyze policy text
- `POST /v1/ai/notice-generation` — generate consent notice in target language
- `POST /v1/ai/legal-summary` — plain-language summarization

**Events Produced:** `ai.policy.gap.analyzed`

**Events Consumed:** none (synchronous, on-demand service)

**External Dependencies:** GPT-4o/Claude 3.5 Sonnet (via LangChain), Qdrant vector DB

**Retry Logic:** LLM API timeout (10s) falls back to `"analysis pending"` state, queued for batch retry rather than blocking the caller.

**Error Codes:** `422` confidence < 0.85 or citation mismatch — routed to mandatory human review, not auto-published

**Security:** Input PII masked before any payload leaves India-hosted infrastructure; output guardrails validate every legal citation against the authoritative corpus before delivery

**Caching:** Embedding cache for repeated policy chunks (24h TTL)

**Indexes:** N/A relationally; Qdrant uses HNSW vector index, Top-K=5 with MMR re-ranking

**Performance Targets:** Policy gap analysis < 10s p95; legal summarization < 20s p95

**Rate Limits:** Per-tenant token budget enforced to control inference cost; Enterprise tier gets a dedicated quota bulkhead

---

## 3.2 pii-detection-service

**Purpose:** Real-time PII detection across 50+ entity types (including Indian formats) in payloads and data streams.

**Responsibilities:**
- NER inference (fine-tuned Llama-3-8B)
- Real-time API payload scanning
- Confidence-scored entity output

**Database:** none persistent (stateless inference; results published as events)

**Tables:** N/A

**REST APIs:**
- `POST /v1/ai/pii-detect` — detect PII in submitted text/payload

**Events Produced:** `ai.pii.detected` (high-confidence findings only)

**Events Consumed:** none (invoked synchronously by discovery/classification pipeline and API-payload scanners)

**External Dependencies:** AWS Bedrock / self-hosted GPU inference cluster

**Retry Logic:** GPU inference circuit breaker; on model unavailability, falls back to a regex-based detector for high-precision entity types (Aadhaar, PAN, email) with `degraded_mode: true` flagged in the response.

**Error Codes:** `503` model unavailable (degraded-mode fallback engaged, not a hard failure)

**Security:** Input never logged verbatim; only hashed payload + detection result persisted to cache

**Caching:** SHA-256(text) → result cache, 5-min TTL (avoids re-inferencing identical payloads)

**Indexes:** N/A (cache-keyed, not table-indexed)

**Performance Targets:** < 50ms p95 real-time detection latency

**Rate Limits:** Per-tenant inference quota; burst capacity reserved for breach-investigation use cases

---

## 3.3 risk-scoring-service

**Purpose:** Computes composite compliance risk scores (tenant, vendor, breach impact) via an ML ensemble.

**Responsibilities:**
- Feature aggregation from consent/breach/vendor signals
- XGBoost/LightGBM ensemble scoring
- Plain-English explanation generation per score

**Database:** PostgreSQL 15

**Tables:** `risk_scores`

**REST APIs:**
- `GET /v1/risk-scores/tenant/{tenantId}` — current compliance score
- `GET /v1/risk-scores/vendor/{vendorId}` — vendor risk score
- `POST /v1/risk-scores/breach-impact` — on-demand breach harm assessment

**Events Produced:** `ai.risk.scored`

**Events Consumed:** `consent.granted`, `breach.incident.created`, `vendor.risk.updated` (signal inputs)

**External Dependencies:** none beyond internal data sources

**Retry Logic:** Nightly batch scoring runs as a fallback if real-time scoring is degraded; scores older than 24h are flagged `stale` on the dashboard rather than silently served as current.

**Error Codes:** none service-specific beyond standard 4xx/5xx

**Security:** Scope `audit:read` required to view detailed score breakdowns

**Caching:** Tenant compliance score (1h, cache-aside)

**Indexes:** `idx_risk_scores_tenant_date (tenant_id, computed_at)`

**Performance Targets:** Real-time scoring < 200ms p95; nightly batch completes within a 2-hour maintenance window across all tenants

**Rate Limits:** Standard tier limits

---

## 3.4 anomaly-service

**Purpose:** Detects unusual data-access patterns (volume, time, location, behavior) from the audit event stream.

**Responsibilities:**
- Stream consumption of audit events
- Isolation Forest + LSTM anomaly inference
- Alert dispatch on detected anomalies

**Database:** Elasticsearch (write-optimized for stream ingestion)

**Tables:** `anomaly_alerts` (ES index, not relational table)

**REST APIs:**
- `GET /v1/anomalies?tenantId={id}` — list recent anomaly alerts
- `POST /v1/anomalies/{id}/acknowledge` — CISO acknowledgment

**Events Produced:** `anomaly.detected`

**Events Consumed:** `audit.event.created` (full stream)

**External Dependencies:** none beyond the Kafka audit stream

**Retry Logic:** Model inference failures are logged but non-blocking — anomaly detection is advisory and never blocks the underlying audited action.

**Error Codes:** none service-specific (advisory-only service; no write-path errors propagate to users)

**Security:** Scope `breach:read`/`audit:read` required to view anomaly alerts (CISO/DPO only)

**Caching:** None (stream-processing service; no read-cache layer)

**Indexes:** ES index on `tenant_id`, `timestamp`, `anomaly_type`, `severity`

**Performance Targets:** Detection latency < 100ms p95 from event ingestion to alert

**Rate Limits:** N/A (internal stream consumer, not a rate-limited API)


---

# 4. Platform Domain

## 4.1 auth-service

**Purpose:** Authentication, authorization token issuance, MFA, and session lifecycle management.

**Responsibilities:**
- OAuth2/OIDC token issuance (JWT, RS256)
- MFA enrollment and verification
- Session and refresh-token lifecycle (rotate-on-use)

**Database:** PostgreSQL 15 + Redis (sessions)

**Tables:** `users`, `sessions`, `mfa_devices`, `refresh_tokens` (hashed at rest)

**REST APIs:**
- `POST /v1/auth/login` — password login
- `POST /v1/auth/mfa/verify` — MFA verification
- `POST /v1/auth/token/refresh` — refresh access token
- `POST /v1/auth/dp/login` / `POST /v1/auth/dp/verify-otp` — Data Principal OTP flow

**Events Produced:** `auth.session.created`, `auth.session.revoked`

**Events Consumed:** none

**External Dependencies:** MSG91/Gupshup (OTP delivery), Okta/Azure AD/Google Workspace (Enterprise SSO)

**Retry Logic:** OTP delivery retried once on provider timeout before surfacing failure to the user; refresh-token reuse detection triggers immediate full session-family revocation (no retry — treated as a security event).

**Error Codes:** `401` invalid credentials · `423` account locked (5 failed attempts) · `409` MFA already verified for session

**Security:** Argon2id password hashing; account lockout after 5 failed attempts; MFA mandatory for Admin/DPO roles; refresh-token rotation with reuse detection

**Caching:** Active session state (`{tenant}:user:session:{id}`, 30-min sliding TTL, Redis)

**Indexes:** `idx_users_tenant_email (tenant_id, email)`, `idx_sessions_user (user_id, expires_at)`

**Performance Targets:** Login p95 < 300ms (excluding MFA round-trip); token validation < 10ms (gateway-cached JWKS)

**Rate Limits:** Login attempts capped at 10/min/IP; OTP requests capped at 3/5-min/identifier

---

## 4.2 tenant-service

**Purpose:** Multi-tenancy provisioning, routing registry, and feature-flag management.

**Responsibilities:**
- Tenant onboarding/provisioning (durable workflow)
- Tenant registry lookups for request routing
- Feature flag management per tenant/tier

**Database:** PostgreSQL 15

**Tables:** `tenants`, `feature_flags`

**REST APIs:**
- `POST /v1/admin/tenants` — provision tenant
- `GET /v1/admin/tenants/{id}` — tenant detail
- `PATCH /v1/admin/tenants/{id}` — tier/feature-flag update
- `POST /v1/admin/tenants/{id}/suspend` — suspend tenant

**Events Produced:** `tenant.provisioned`, `tenant.suspended`, `tenant.tier.changed`

**Events Consumed:** none

**External Dependencies:** Temporal.io (durable provisioning workflow)

**Retry Logic:** Provisioning is a Temporal.io durable workflow — survives pod restarts mid-provisioning, automatically resumes from the last completed step.

**Error Codes:** `409` tenant identifier already exists · `403` suspend attempted without `SUPER_ADMIN` scope

**Security:** Scope `admin:tenants` restricted exclusively to `SUPER_ADMIN` role

**Caching:** Tenant registry write-through (`platform:tenant:registry:{id}`, 5-min TTL)

**Indexes:** `idx_tenants_subdomain (subdomain)`, `idx_tenants_tier (tier, status)`

**Performance Targets:** Registry lookup < 10ms p95 (cache hit); provisioning completion < 5 min end-to-end

**Rate Limits:** Admin-only; not consumer-rate-limited

---

## 4.3 notification-service

**Purpose:** Multi-channel notification dispatch with delivery tracking.

**Responsibilities:**
- Channel routing (email/SMS/WhatsApp/push/in-app/webhook)
- Multi-language template rendering
- Delivery status tracking

**Database:** PostgreSQL 15

**Tables:** `notification_jobs`, `delivery_receipts`

**REST APIs:**
- `GET /v1/notifications/{id}/status` — delivery status
- `POST /v1/notifications/templates` — manage templates (admin)

**Events Produced:** `notification.sent`, `notification.delivered`, `notification.failed`

**Events Consumed:** `consent.granted`, `consent.withdrawn`, `dpr.*`, `breach.*` (notification-consumer-group)

**External Dependencies:** AWS SES, MSG91/Gupshup (SMS/WhatsApp), FCM/APNs (push)

**Retry Logic:** Per-channel circuit breaker; failed sends retried 3× then routed to DLQ for manual resend via admin console.

**Error Codes:** `502` upstream channel provider error (retried) · `400` invalid template/locale combination

**Security:** Webhook payloads HMAC-signed; channel provider credentials in Secrets Manager

**Caching:** Rendered template cache per `{tenant}:{templateId}:{lang}` (1h)

**Indexes:** `idx_notification_status (tenant_id, status, created_at)`

**Performance Targets:** Dispatch initiation < 2s p95 from event consumption; delivery confirmation tracked end-to-end

**Rate Limits:** Channel-provider-imposed limits respected via internal token-bucket throttling per channel

---

## 4.4 workflow-service

**Purpose:** BPMN 2.0 workflow engine for SLA-aware, durable compliance process orchestration.

**Responsibilities:**
- Workflow definition and execution (Temporal.io for durable orchestration, Airflow for scheduled batch jobs)
- SLA-aware task routing and escalation rules
- Workflow versioning and audit trail

**Database:** PostgreSQL 15

**Tables:** `workflow_definitions`, `workflow_instances`, `workflow_tasks`

**REST APIs:**
- `POST /v1/workflows/{definitionId}/start` — start workflow instance
- `GET /v1/workflows/instances/{id}` — instance status
- `POST /v1/workflows/instances/{id}/tasks/{taskId}/complete` — complete a task

**Events Produced:** `workflow.instance.started`, `workflow.instance.completed`, `workflow.sla.breached`

**Events Consumed:** triggers from rights-service, breach-service, vendor-service (workflow kickoff events)

**External Dependencies:** none beyond Temporal.io/Airflow runtime

**Retry Logic:** Temporal's built-in durable execution handles worker crashes; in-flight workflows resume from the last completed activity automatically.

**Error Codes:** `409` task already completed by another actor · `422` task completion attempted out of valid state transition

**Security:** Task completion scoped to the assigned role only (enforced via OPA policy, not just UI hiding)

**Caching:** None (workflow state must always be read live for correctness)

**Indexes:** `idx_workflow_instances_status (tenant_id, status, sla_deadline)`

**Performance Targets:** Task routing latency < 500ms; SLA breach detection < 1 min of threshold crossing

**Rate Limits:** Internal/admin-driven; not externally rate-limited

---

## 4.5 audit-service

**Purpose:** Immutable, hash-chained audit log ingestion and integrity verification — the platform's evidentiary backbone.

**Responsibilities:**
- Ingest all platform audit events (60+ topics)
- Hash-chain construction (each record references the prior record's hash)
- Daily integrity verification

**Database:** S3 Object Lock (canonical, WORM) + Elasticsearch (searchable copy)

**Tables:** `audit_events` (ES index + S3 object per event batch)

**REST APIs:**
- `GET /v1/audit-events?tenantId={id}&from=&to=` — search audit trail
- `GET /v1/audit-events/{id}/verify` — verify hash-chain integrity for a record

**Events Produced:** `audit.integrity.violation` (only on verification failure)

**Events Consumed:** all platform events (audit-consumer-group, 60+ topics — the sole consumer of the raw firehose)

**External Dependencies:** AWS CloudTrail (infra-level audit correlation)

**Retry Logic:** Ingestion lag alerting at >30s; consumer group rebalances automatically on pod failure with at-least-once delivery guarantees from Kafka.

**Error Codes:** `500` hash-chain mismatch on daily verification — escalated as a P1 security incident, not a routine retry

**Security:** Write-once storage (S3 Object Lock); read access restricted to DPO/CISO/Legal/Super Admin; IP addresses encrypted at rest within the audit payload itself

**Caching:** None (audit reads must reflect ground truth, never a stale cache)

**Indexes:** ES index on `tenant_id`, `event_type`, `timestamp`, `actor.user_id`, `resource.id`

**Performance Targets:** Ingestion lag < 30s p95; integrity verification job completes nightly within a 2-hour window

**Rate Limits:** N/A (internal consumer); search API rate-limited at standard tier limits

---

# 5. Reporting Domain

## 5.1 analytics-service

**Purpose:** Real-time aggregation and dashboard metrics — the CQRS read path for compliance KPIs.

**Responsibilities:**
- Metrics aggregation from event streams
- Dashboard query serving (consent coverage, DPR SLA status, breach readiness)

**Database:** Elasticsearch (read-optimized rollups)

**Tables:** Materialized aggregation indices (not relational tables)

**REST APIs:**
- `GET /v1/analytics/compliance-score?dimensions=&period=` — compliance score
- `GET /v1/analytics/dpr-queue` — operational DPR queue metrics

**Events Produced:** none (pure read path)

**Events Consumed:** `consent.*`, `dpr.*`, `breach.*`, `ai.*` (analytics-consumer-group)

**External Dependencies:** none beyond the Kafka event stream

**Retry Logic:** CQRS read path — a degraded analytics-service never blocks any write-path compliance operation; consumer lag is monitored but non-urgent relative to write-path services.

**Error Codes:** `503` aggregation temporarily stale (degraded but non-blocking UX fallback)

**Security:** Scope `audit:read` for detailed dimension breakdowns

**Caching:** Aggregated dashboard payloads (5 min)

**Indexes:** ES index on `tenant_id`, `metric_type`, `period`

**Performance Targets:** Dashboard query < 2s p95

**Rate Limits:** Standard tier limits

---

## 5.2 report-service

**Purpose:** Generates PDF/Excel regulatory and board-level reports.

**Responsibilities:**
- Report rendering (PDF/Excel)
- Regulatory template management (DPBI annual, RBI/SEBI templates, ISO 27701 evidence)
- Scheduled report dispatch

**Database:** PostgreSQL 15 (job metadata) + S3 (output storage)

**Tables:** `report_jobs`

**REST APIs:**
- `POST /v1/reports/generate` — trigger report generation
- `GET /v1/reports/{jobId}/status` — job status
- `GET /v1/reports/{jobId}/download` — signed S3 download URL

**Events Produced:** `report.generation.completed`

**Events Consumed:** scheduled triggers (monthly/quarterly cron via workflow-service)

**External Dependencies:** none beyond internal data sources

**Retry Logic:** Long-running report jobs run async via Kubernetes Jobs; failed jobs retried once automatically, then surfaced to the requester for manual retry.

**Error Codes:** `404` report job not found · `410` download link expired (72h expiry)

**Security:** Download links are signed, time-limited (72h), and scoped to the requesting tenant only

**Caching:** None (each report reflects point-in-time data; never served from cache)

**Indexes:** `idx_report_jobs_tenant_status (tenant_id, status, created_at)`

**Performance Targets:** Report generation < 30s for standard reports; large board packs < 5 min

**Rate Limits:** Standard tier limits; concurrent generation capped at 5 jobs/tenant to bound resource usage


---

# 6. Integration Domain

## 6.1 connector-service

**Purpose:** Maintains 30+ pre-built data source connectors and their health for cross-system aggregation and discovery.

**Responsibilities:**
- Connector configuration and credential management
- Connection health monitoring
- Source-system query execution on behalf of rights-service/discovery-service

**Database:** PostgreSQL 15

**Tables:** `connectors`, `connector_health_checks`

**REST APIs:**
- `POST /v1/connectors` — register connector
- `GET /v1/connectors/{id}/health` — health status
- `POST /v1/connectors/{id}/query` — execute scoped query (internal use)

**Events Produced:** `connector.health.degraded`, `connector.health.restored`

**Events Consumed:** none

**External Dependencies:** Razorpay, Finacle/Temenos, DigiLocker, Zoho, and 25+ other source systems

**Retry Logic:** Per-connector health check every 60s; unhealthy connectors auto-paused with a DPO alert rather than silently failing dependent aggregation requests.

**Error Codes:** `503` connector unhealthy/paused · `401` connector credential expired/invalid

**Security:** Per-connector credentials stored in Secrets Manager; read-only access scoped to the minimum required fields per connector

**Caching:** Connector health status (60s)

**Indexes:** `idx_connectors_tenant_status (tenant_id, status)`

**Performance Targets:** Health check round-trip < 5s p95; query proxying overhead < 50ms added latency

**Rate Limits:** Per-connector QPS cap configurable to respect source-system capacity

---

## 6.2 webhook-service

**Purpose:** Reliable outbound webhook delivery to customer systems.

**Responsibilities:**
- Signed payload delivery (HMAC)
- Retry and DLQ management for failed deliveries
- Delivery audit trail

**Database:** PostgreSQL 15

**Tables:** `webhook_subscriptions`, `webhook_deliveries`

**REST APIs:**
- `POST /v1/webhooks/subscriptions` — register webhook endpoint
- `GET /v1/webhooks/deliveries/{id}` — delivery status/history

**Events Produced:** none (terminal delivery point)

**Events Consumed:** `consent.withdrawn`, `dpr.erasure.completed` (customer-webhook-group)

**External Dependencies:** customer-hosted webhook endpoints

**Retry Logic:** Exponential backoff (1 min → 1 hour cap); DLQ after 10 failed attempts, surfaced in the admin console for manual resend.

**Error Codes:** `502` customer endpoint unreachable · `401` signature verification failed at customer end (logged, not retried indefinitely)

**Security:** Every payload HMAC-signed with a per-tenant secret; customer endpoints must be HTTPS

**Caching:** None

**Indexes:** `idx_webhook_deliveries_status (subscription_id, status, attempted_at)`

**Performance Targets:** Initial delivery attempt < 5s of source event; full retry cycle resolves within 1 hour

**Rate Limits:** Outbound delivery throttled per customer endpoint to avoid overwhelming smaller integrations

---

## 6.3 siem-service

**Purpose:** Bidirectional integration with customer SIEM tools for breach-detection triggers.

**Responsibilities:**
- Inbound SIEM alert ingestion (webhook)
- Outbound enrichment data to SIEM (optional)
- SIEM connectivity health monitoring

**Database:** PostgreSQL 15

**Tables:** `siem_integrations`, `siem_alert_log`

**REST APIs:**
- `POST /v1/siem/integrations` — configure SIEM connection
- `POST /v1/siem/alerts` — inbound alert webhook (Splunk/QRadar/Sentinel)

**Events Produced:** `siem.alert.raised`

**Events Consumed:** none

**External Dependencies:** Splunk, IBM QRadar, Microsoft Sentinel

**Retry Logic:** Bidirectional health-check every 5 min; loss of SIEM connectivity raises a platform-level alert (breach-detection coverage gap), distinct from a routine service error.

**Error Codes:** `401` SIEM webhook authentication failed · `503` SIEM connectivity lost

**Security:** Webhook authentication via per-integration API key; inbound payloads validated against expected schema before processing

**Caching:** Connectivity status (5 min)

**Indexes:** `idx_siem_alerts_tenant_time (tenant_id, received_at)`

**Performance Targets:** Alert-to-incident-creation latency < 30 min (per FR-BN-001 statutory expectation)

**Rate Limits:** Inbound alert ingestion capped at 100 events/min/tenant

---

## 6.4 dpbi-service

**Purpose:** DPBI (Data Protection Board of India) portal submission integration and regulatory correspondence tracking.

**Responsibilities:**
- DPBI notification form submission
- Submission status tracking and reference number capture
- Regulatory correspondence archive

**Database:** PostgreSQL 15

**Tables:** `dpbi_submissions`, `dpbi_correspondence`

**REST APIs:**
- `POST /v1/dpbi/submissions` — submit notification (called by breach-service/grievance-service)
- `GET /v1/dpbi/submissions/{id}` — submission status

**Events Produced:** `dpbi.submission.acknowledged`, `dpbi.submission.failed`

**Events Consumed:** none (invoked synchronously by breach-service and grievance-service)

**External Dependencies:** DPBI regulatory portal API

**Retry Logic:** Submission retried with backoff (max 5 attempts over the available SLA window); terminal failure escalates to PagerDuty with a documented manual-portal-upload fallback procedure.

**Error Codes:** `502` DPBI portal unreachable · `422` submission rejected by portal validation (surfaced verbatim to DPO for correction)

**Security:** Digital signature (DSC) integration for submission authenticity; all correspondence archived immutably

**Caching:** None (submission state must always be current)

**Indexes:** `idx_dpbi_submissions_deadline (tenant_id, deadline, status)`

**Performance Targets:** Submission attempt initiated within 1 minute of DPO confirmation; full retry cycle completes within the remaining SLA window

**Rate Limits:** N/A (low-volume, regulatory-driven traffic)

---

# 7. Infrastructure Domain

## 7.1 config-service

**Purpose:** Centralized, Git-backed configuration management (Spring Cloud Config) for all services.

**Responsibilities:**
- Serve environment-specific configuration to all 26 other services
- Configuration versioning via Git
- Dynamic config refresh without redeploy

**Database:** Git repository (source of truth); no relational database

**Tables:** N/A

**REST APIs:**
- `GET /v1/config/{service}/{profile}` — fetch service configuration
- `POST /v1/config/refresh` — trigger config refresh broadcast

**Events Produced:** `config.refreshed`

**Events Consumed:** none

**External Dependencies:** GitHub Enterprise (config repository)

**Retry Logic:** Consuming services cache last-known-good configuration locally; a config-service outage does not crash dependent services — they continue operating on cached config until service recovers.

**Error Codes:** `404` configuration profile not found · `409` config refresh conflict (concurrent Git update)

**Security:** Config repository access restricted to platform engineering; secrets are never stored in config-service (referenced from Secrets Manager instead)

**Caching:** Every consuming service caches its config locally (refresh-on-demand, not polling)

**Indexes:** N/A (Git-backed, not a database)

**Performance Targets:** Config fetch < 100ms p95; refresh broadcast propagates within 30s

**Rate Limits:** Internal-only; not externally exposed

---

## 7.2 search-service

**Purpose:** Elasticsearch proxy providing tenant-scoped routing and query optimization for all search-dependent services.

**Responsibilities:**
- Tenant-scoped query routing to the correct ES index
- Query optimization and result shaping
- Index lifecycle coordination (hot/warm/cold tiers)

**Database:** Elasticsearch (proxied, not owned data)

**Tables:** N/A

**REST APIs:**
- `POST /v1/search/{indexAlias}` — execute tenant-scoped search query

**Events Produced:** none

**Events Consumed:** none (synchronous proxy)

**External Dependencies:** OpenSearch/Elasticsearch cluster

**Retry Logic:** Query timeout circuit breaker (2s) with a graceful "search temporarily degraded" UX fallback rather than a hard error.

**Error Codes:** `504` query timeout · `403` cross-tenant index access attempt (always rejected, fail-closed)

**Security:** Every query is rewritten server-side to inject the `tenant_id` filter — callers cannot override or omit it

**Caching:** Frequently repeated query signatures cached (2 min)

**Indexes:** Delegated to the underlying ES cluster's per-index settings (Section 35 of the Architecture Document — hot/warm/cold ILM)

**Performance Targets:** p95 query latency < 300ms

**Rate Limits:** Standard tier limits

---

*End of Document — DataShield India Microservice SOP Reference v1.0*
