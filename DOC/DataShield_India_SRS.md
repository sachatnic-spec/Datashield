# DataShield India
## Software Requirements Specification (SRS)

**Document Version:** 1.0 · **Classification:** Confidential — Internal Engineering
**Status:** Source of Truth for Functional Scope · **Companion Documents:** Enterprise PRD v1.0, Software Architecture Document v1.0

---

## Table of Contents
1. Business Requirements
2. Functional Requirements
3. Non-Functional Requirements
4. User Roles
5. User Stories
6. Acceptance Criteria
7. Edge Cases
8. Validations
9. Error Handling

---

# 1. Business Requirements

| BR-ID | Business Requirement | Driver |
|---|---|---|
| BR-001 | Achieve and demonstrate 100% DPDP Act, 2023 (§4–§20) compliance coverage for enterprise customers | Regulatory mandate, penalty exposure up to ₹250 Cr/violation |
| BR-002 | Reduce customer DPO manual compliance workload by ≥70% | Customer pain point — avg. 847 person-hours/year on manual compliance |
| BR-003 | Guarantee 100% India data residency for all personal data | DPDP data localization expectation, RBI/sectoral regulator alignment |
| BR-004 | Support DPBI's mandatory 72-hour breach notification window with automated workflows | §8 statutory deadline; manual processes cannot reliably meet it |
| BR-005 | Enable verifiable parental consent for users under 18 | §9 children's data obligation, EdTech/Healthcare segment requirement |
| BR-006 | Provide multi-language (22 Indian languages) consent and rights experiences | Market reach beyond English-literate urban users; §5 notice obligation |
| BR-007 | Achieve ₹500 Crore ARR within 5 years across BFSI, Healthcare, EdTech, E-Commerce, Telecom, Government, IT/ITeS | Investor/board growth target |
| BR-008 | Differentiate from global privacy platforms (OneTrust, TrustArc) via India-native architecture and pricing | Competitive positioning — global players average 55–65% DPDP coverage |
| BR-009 | Provide audit-ready, immutable evidence for every compliance action to withstand DPBI/regulator scrutiny | Legal defensibility; board/audit committee requirement |
| BR-010 | Support tiered commercial packaging (Starter → Professional → Enterprise → Government) | Revenue segmentation across enterprise sizes and budgets |

---

# 2. Functional Requirements

Functional requirements are grouped by module. Each FR is traceable to the owning microservice defined in the Architecture Document.

## 2.1 Consent Management
| FR-ID | Requirement |
|---|---|
| FR-CM-001 | The system shall collect granular, purpose-specific consent (no bundling of unrelated purposes). |
| FR-CM-002 | The system shall record IP address, device fingerprint, channel, and timestamp for every consent event. |
| FR-CM-003 | The system shall allow withdrawal of consent through a mechanism no more complex than the one used to grant it. |
| FR-CM-004 | The system shall trigger a re-consent flow whenever the underlying privacy notice version changes. |
| FR-CM-005 | The system shall verify the age of a data principal and route users under 18 into a parental-consent workflow. |
| FR-CM-006 | The system shall expose a public Consent Collection API for first-party app/website integration. |
| FR-CM-007 | The system shall render consent notices in the data principal's selected language from a 22-language catalogue. |

## 2.2 Data Principal Rights
| FR-ID | Requirement |
|---|---|
| FR-DPR-001 | The system shall accept rights requests (access, correction, erasure, nomination, grievance) via web, mobile, email, and WhatsApp. |
| FR-DPR-002 | The system shall verify data principal identity (OTP at minimum) before processing any rights request. |
| FR-DPR-003 | The system shall compute and display a statutory SLA deadline for every request type at submission time. |
| FR-DPR-004 | The system shall orchestrate erasure as a distributed transaction (Saga) across all connected source systems. |
| FR-DPR-005 | The system shall block erasure where an active legal hold exists and notify the DPO of the conflict. |
| FR-DPR-006 | The system shall issue a verifiable erasure certificate upon successful multi-system deletion. |

## 2.3 Breach Notification
| FR-ID | Requirement |
|---|---|
| FR-BN-001 | The system shall accept breach incident creation from both manual entry and SIEM webhook integration. |
| FR-BN-002 | The system shall classify breach severity (P0–P3) using AI-assisted harm-likelihood scoring. |
| FR-BN-003 | The system shall start a 72-hour countdown to DPBI notification immediately upon P0/P1 incident creation. |
| FR-BN-004 | The system shall auto-populate the DPBI notification form from incident data and require explicit DPO confirmation before submission. |
| FR-BN-005 | The system shall dispatch multi-channel notifications to affected data principals for P0/P1 incidents. |

## 2.4 Vendor & Data Discovery
| FR-ID | Requirement |
|---|---|
| FR-VR-001 | The system shall maintain a registry of data processors with associated DPA lifecycle status. |
| FR-VR-002 | The system shall compute an AI-assisted vendor risk score (0–100) from questionnaire and DPA data. |
| FR-DD-001 | The system shall scan connected databases and object storage on a scheduled and on-demand basis to discover PII. |
| FR-DD-002 | The system shall classify discovered PII against the DPDP Schedule with a confidence score, flagging sub-0.85-confidence findings for human review. |

---

# 3. Non-Functional Requirements

| Category | Requirement |
|---|---|
| **Performance** | API p95 < 200ms, p99 < 500ms; consent widget load < 100ms; sustain 100,000 consent TPS at peak. |
| **Scalability** | Support 500M+ data principals, 10,000+ concurrent tenants, 10B+ consent records, horizontal scale-out < 60s. |
| **Availability** | 99.99% platform SLA; 99.999% for the consent-collection critical path; RTO < 15 min, RPO < 5 min. |
| **Security** | AES-256 at rest, TLS 1.3 in transit, HSM-backed per-tenant keys, zero-trust network model, OWASP ASVS L2 baseline. |
| **Maintainability** | ≥80% automated test coverage gate; OpenAPI-first service contracts; ADR required for cross-service changes. |
| **Reliability** | Circuit breakers on all outbound calls; idempotency keys mandatory on write APIs; DLQ on every Kafka topic. |
| **Disaster Recovery** | Active-passive multi-region (Mumbai/Hyderabad); quarterly DR drills against published RTO/RPO targets. |
| **Localization** | 100% of Class A (PII) data resident in India; zero cross-border transfer of sensitive personal data. |
| **Usability** | DPO time-to-first-value < 2 hours from onboarding; WCAG 2.1 AA accessibility compliance. |

---

# 4. User Roles

| Role | Description | Primary Goals |
|---|---|---|
| **Super Admin** | DataShield internal platform operations team | Tenant provisioning, platform health, billing oversight |
| **Tenant Admin** | Customer IT/Admin lead | User management, module configuration, SSO setup |
| **DPO (Data Protection Officer)** | Customer's compliance owner | Compliance posture, DPR fulfillment, breach response, board reporting |
| **Privacy Manager** | Compliance analyst, operational level | Day-to-day consent/vendor assessments, operational dashboards |
| **CISO** | Customer security lead | Breach alerts, security posture, audit log review |
| **Legal Reviewer** | Customer legal team | Policy approval, DPA review, grievance escalation decisions |
| **IT Admin** | Customer engineering/IT lead | Data source connections, discovery scan configuration |
| **API Consumer** | Customer backend service account | Programmatic consent/rights integration |
| **Data Principal** | End user / citizen whose data is processed | View consents, exercise rights, file grievances |

---

# 5. User Stories

## 5.1 Data Principal
- **US-DP-01:** As a data principal, I want to grant consent for a specific purpose only, so that my data isn't used beyond what I agreed to.
- **US-DP-02:** As a data principal, I want to withdraw consent in one tap from a single screen, so that I can stop processing without contacting support.
- **US-DP-03:** As a data principal, I want to submit an access request and track its status, so that I know when I'll receive my data.
- **US-DP-04:** As a parent, I want to review and approve data processing on behalf of my child, so that I retain control over their data.

## 5.2 DPO
- **US-DPO-01:** As a DPO, I want a real-time compliance score, so that I can report posture to the board without manual compilation.
- **US-DPO-02:** As a DPO, I want an AI-generated summary of every incoming DSAR, so that I can review and approve faster.
- **US-DPO-03:** As a DPO, I want the DPBI notification form pre-filled from incident data, so that I can meet the 72-hour deadline reliably.
- **US-DPO-04:** As a DPO, I want to be alerted before any rights-request SLA breaches, so that I can intervene proactively.

## 5.3 CISO
- **US-CISO-01:** As a CISO, I want breach incidents auto-created from SIEM alerts, so that detection-to-containment time is minimized.
- **US-CISO-02:** As a CISO, I want immutable, tamper-evident audit logs, so that I can produce defensible evidence during a regulatory audit.

## 5.4 IT Admin
- **US-ITA-01:** As an IT Admin, I want to connect a new database for PII discovery in under 10 minutes, so that onboarding isn't an engineering bottleneck.

## 5.5 Legal Reviewer
- **US-LEG-01:** As a Legal Reviewer, I want AI-flagged DPDP policy gaps with section citations, so that I can prioritize remediation accurately.

---

# 6. Acceptance Criteria

### US-DP-02 — Withdraw consent in one tap
- **Given** a data principal with an active consent for "Marketing Communications"
- **When** they tap "Withdraw" on the Consent Preference Center
- **Then** the consent status transitions to `WITHDRAWN` within 2 seconds, a `consent.withdrawn` event is published, and the customer's downstream systems are notified via webhook within 5 seconds.
- **And** the withdrawal is recorded in the immutable audit log with timestamp, reason, and channel.

### US-DPO-03 — DPBI form pre-filled from incident data
- **Given** a breach incident classified P0/P1
- **When** the DPO opens the DPBI Notification draft
- **Then** Sections A (Fiduciary Details), B (Nature of Breach), C (Data Types Affected), D (Estimated Records), E (Containment Measures), and F (DPO Contact) are pre-populated from the incident record.
- **And** the form cannot be submitted without an explicit `dpoConfirmation: true` flag (4-eyes control).

### US-DP-04 — Parental consent
- **Given** a registration flow where age verification determines the user is under 18
- **Then** the system blocks data collection until a parent/guardian completes identity verification (OTP, optional Aadhaar) and explicitly grants consent.
- **And** the parental consent record is linked to the child's account and scheduled for annual re-verification.

### US-CISO-01 — SIEM-triggered incident creation
- **Given** a SIEM tool (Splunk/QRadar/Sentinel) configured via webhook
- **When** a qualifying security alert fires
- **Then** a breach incident is created in DataShield within 30 minutes of detection, pre-populated with systems affected and discovery timestamp.

---

# 7. Edge Cases

| Area | Edge Case | Expected Behavior |
|---|---|---|
| Consent | Data principal grants consent, then the underlying purpose is deleted by the tenant before withdrawal | Consent record is preserved (immutable); purpose is marked `RETIRED`, not hard-deleted; UI shows "purpose no longer active." |
| Consent | Same data principal grants consent twice for the same purpose within 1 second (double-tap / network retry) | `X-Correlation-ID` idempotency key ensures only one record is created; second request returns the original 201 response. |
| Erasure | Erasure request submitted while a legal hold is simultaneously placed on the same data principal | Legal hold check is synchronous and authoritative; erasure is deferred and the DPO is notified of the conflict — hold always wins. |
| Erasure | One of 30+ connected systems never acknowledges the erasure event within the 48-hour Saga timeout | Saga orchestrator marks that system `FAILED`, documents it in the erasure certificate as an exception, and alerts the DPO for manual remediation; successfully-deleted systems are not rolled back. |
| Breach | A breach is discovered, but estimated records affected is unknown at creation time | Incident is created with `estimated_records_affected: null`; severity defaults to the higher of the two plausible classifications until the assessment completes; 72-hour clock still starts at discovery. |
| Children's Data | A user is correctly age-gated as a minor, but the parent fails identity verification 3 times | Account creation is blocked entirely; no partial data is retained; the attempt is logged for fraud-pattern monitoring (not for the minor's profile). |
| Multi-tenancy | Two tenants share a Starter-tier Kubernetes namespace and one issues an unusually high burst of consent writes | Per-tenant rate limiting (Kong) and RLS-enforced query isolation prevent both a "noisy neighbor" availability impact and any cross-tenant data leakage. |
| DSAR | A bulk/class-action rights request affects >100,000 data principals simultaneously | Request is routed to the bulk-handling path (`FR-DPR-012`), processed asynchronously via Kubernetes Jobs rather than the synchronous request path, with a longer SLA communicated to the requester. |
| AI | The policy-gap-analysis LLM call times out or returns a hallucinated section citation | Guardrail validator checks every citation against the authoritative DPDP corpus; any mismatch routes the result to mandatory human review rather than auto-publishing. |

---

# 8. Validations

| Field / Action | Validation Rule |
|---|---|
| `dataPrincipalId` | Must be a pre-hashed identifier; raw PII (plaintext email/phone) is rejected at the API edge with `400`. |
| `purposeId` | Must reference an active, non-retired purpose within the requesting tenant. |
| Consent creation | Rejected if the request bundles two or more distinct purposes into a single non-granular checkbox (`FR-CM-001` enforcement). |
| `requestType` (DPR) | Must be one of `ACCESS \| CORRECTION \| ERASURE \| GRIEVANCE \| NOMINATION`; any other value returns `400`. |
| Breach `severity` | Must be one of `P0_CRITICAL \| P1_HIGH \| P2_MEDIUM \| P3_LOW`; severity downgrade after DPBI notification has been sent requires Legal + DPO dual-approval. |
| DPBI submission | Blocked (`422`) unless `dpoConfirmation: true` is explicitly present in the request body. |
| Age input (consent) | DOB-based age calculation is authoritative; if DigiLocker verification and self-reported DOB disagree, the system defaults to the more protective (younger) classification. |
| Idempotency | `X-Correlation-ID` header is mandatory on all POST/PATCH endpoints; missing header returns `400`. |
| Tenant isolation | Every query carries an implicit `tenant_id` predicate (RLS); any attempt to query across tenants without `SUPER_ADMIN` scope returns `403`, not an empty result set (fail closed, not fail open). |

---

# 9. Error Handling

All errors follow **RFC 7807 Problem Details**: `{type, title, status, detail, instance}`. Validation errors additionally include `invalidFields[]`.

| HTTP Status | Meaning | Example Scenario |
|---|---|---|
| 400 | Validation error | Missing required field, bundled consent purposes, invalid enum value |
| 401 | Unauthenticated | Missing/expired JWT |
| 403 | Forbidden | Valid token but insufficient scope, or cross-tenant access attempt |
| 404 | Not found | Consent record / DPR request / breach incident does not exist for this tenant |
| 409 | Conflict | Duplicate consent creation without withdrawal; concurrent erasure already in progress |
| 422 | Unprocessable | DPBI submission attempted without `dpoConfirmation`; erasure attempted under active legal hold |
| 423 | Locked | Resource locked by legal hold |
| 429 | Rate limited | Tenant tier rate limit exceeded — response includes `Retry-After` header |
| 500 | Internal error | Unhandled exception — logged with `traceId`, never exposes stack trace to the client |
| 503 | Service unavailable | Downstream dependency (e.g., AI inference) degraded — circuit breaker open, fallback behavior engaged where defined (Section 5 of the Architecture Document) |

**Error-handling principles:**
- Compliance-critical writes (consent, erasure, breach) never fail silently — every error path either completes the action or surfaces an explicit, actionable error to the caller and the audit log.
- AI-assisted features (severity scoring, gap analysis) degrade gracefully to a manual/human-review fallback rather than blocking the underlying compliance action.
- All 5xx errors are correlated via `traceId` across the distributed trace (Jaeger) for fast root-cause triage.

---

*End of Document — DataShield India SRS v1.0*
