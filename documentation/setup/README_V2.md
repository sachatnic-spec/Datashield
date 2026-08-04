# DataShield India — README v2

![Status](https://img.shields.io/badge/status-MVP-yellow)
![Java](https://img.shields.io/badge/Java-21%2F17-blue)
![Python](https://img.shields.io/badge/Python-3.11-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green)
![Angular](https://img.shields.io/badge/Angular-17-red)
![Kubernetes](https://img.shields.io/badge/Kubernetes-1.24%2B-blue)

Enterprise-grade **DPDP Act 2023 compliance** platform for India. This document covers every service, its exact tech stack, and its role in the platform.

---

## Platform at a Glance

| Layer | Technology |
|---|---|
| Backend (core) | Java 21, Spring Boot 3.3, Maven |
| Backend (AI/ML) | Python 3.11, FastAPI, Uvicorn |
| Frontend | Angular 17, TypeScript, TailwindCSS |
| Widget SDK | TypeScript, Rollup, UMD/ESM |
| Database | PostgreSQL (schema-per-tenant + RLS) |
| Cache | Redis |
| Messaging | Apache Kafka + Avro schemas |
| Search | Elasticsearch 8.x |
| Observability | Prometheus, Grafana, OpenTelemetry, Jaeger |
| Infrastructure | Docker, Kubernetes 1.24+, AWS (MSK, RDS, S3) |
| Build | Maven 3.9, npm |

---

## Services

### 1. auth-service
**Port:** 8001 | **Language:** Java 21 | **Framework:** Spring Boot 3.3

**Role:**
Central identity provider for the entire platform. Issues and validates JWT tokens, manages OAuth2 flows, handles MFA (TOTP/SMS), and maintains session state. Every other service validates tokens issued by this service.

**Tech Stack:**
- Spring Boot Web, Spring Security, Spring Data JPA
- Spring Data Redis — session store and token blacklist
- PostgreSQL — user accounts, roles, tenant mappings
- JJWT (jjwt-api, jjwt-impl, jjwt-jackson) — JWT signing and verification
- Bouncy Castle (bcprov-jdk15on 1.70) — AES-256 encryption, key derivation
- SpringDoc OpenAPI — Swagger UI
- Micrometer + Prometheus — metrics
- Testcontainers (PostgreSQL) — integration tests

---

### 2. consent-service
**Port:** 8002 | **Language:** Java 21 | **Framework:** Spring Boot 3.3

**Role:**
Core compliance service. Manages the full lifecycle of data principal consent — creation, versioning, withdrawal, and expiry. Enforces DPDP Act §6 (lawful processing), §7 (consent), and §9 (children's consent). Publishes consent events to Kafka for downstream services to react to.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA
- Spring Data Redis — consent cache for high-throughput reads
- Spring Kafka — publishes `consent.granted`, `consent.withdrawn`, `consent.expired` events
- PostgreSQL — consent records with schema-per-tenant isolation
- event-schemas lib — Avro schemas for Kafka messages
- common-lib — shared utilities, error handling
- SpringDoc OpenAPI, Micrometer + Prometheus
- Testcontainers — integration tests

---

### 3. rights-service
**Port:** 8003 | **Language:** Java 21 | **Framework:** Spring Boot 3.3

**Role:**
Orchestrates all Data Principal Rights requests under DPDP §13–§14A — right to access, right to correction, right to erasure (Right to be Forgotten), and right to nominate. Manages DSAR (Data Subject Access Request) workflows with SLA tracking and escalation.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA
- Spring Kafka — publishes `rights.request.created`, `rights.erasure.initiated` events; coordinates with other services for cross-service erasure sagas
- PostgreSQL — rights requests, SLA timers, status tracking
- event-schemas lib, common-lib
- SpringDoc OpenAPI, Micrometer + Prometheus

---

### 4. breach-service
**Port:** 8004 | **Language:** Java 21 | **Framework:** Spring Boot 3.3

**Role:**
Manages the full breach incident lifecycle under DPDP §8. Accepts breach reports, classifies severity, tracks the 72-hour notification countdown, coordinates with dpbi-service for statutory DPBI submissions, and notifies affected data principals via notification-service.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA
- Spring Kafka — publishes `breach.detected`, `breach.notified` events; triggers downstream DPBI and notification workflows
- PostgreSQL — breach incidents, timeline, evidence
- event-schemas lib, common-lib
- SpringDoc OpenAPI, Micrometer + Prometheus

---

### 5. dpbi-service
**Port:** 8005 | **Language:** Java 17 | **Framework:** Spring Boot 3.3

**Role:**
Handles statutory breach notification submissions to the Data Protection Board of India (DPBI). Formats and submits breach reports within the 72-hour regulatory window, tracks submission status, and maintains an audit trail of all DPBI communications.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA, Spring Validation
- Spring Kafka — consumes breach events from breach-service
- PostgreSQL — DPBI submissions, acknowledgements, status
- Lombok — boilerplate reduction
- Logstash Logback Encoder 7.4 — structured JSON logging
- SpringDoc OpenAPI 2.5.0, Micrometer + Prometheus

---

### 6. notification-service
**Port:** 8006 | **Language:** Java 21 | **Framework:** Spring Boot 3.3

**Role:**
Multi-channel notification dispatcher. Consumes events from Kafka and delivers notifications via Email (SES/SMTP), SMS (SNS/Twilio), WhatsApp, Push (FCM/APNs), and in-app channels. Handles templating, delivery retries, and delivery receipts.

**Tech Stack:**
- Spring Boot Web
- Spring Kafka — consumes events from consent, breach, rights, and audit services
- PostgreSQL — notification logs, delivery status, templates
- event-schemas lib, common-lib
- SpringDoc OpenAPI, Micrometer + Prometheus

---

### 7. audit-service
**Port:** 8007 | **Language:** Java 21 | **Framework:** Spring Boot 3.3

**Role:**
Immutable compliance audit log. Consumes all platform events from Kafka and writes tamper-proof records to PostgreSQL and archives to AWS S3 with Object Lock (WORM). Provides query APIs for compliance officers to retrieve audit trails for any entity, tenant, or time range.

**Tech Stack:**
- Spring Boot Web
- Spring Kafka — consumes events from all services (consent, rights, breach, auth, etc.)
- PostgreSQL — hot audit records (90-day retention)
- AWS S3 Object Lock — cold WORM archive (7-year retention)
- event-schemas lib, common-lib
- SpringDoc OpenAPI, Micrometer + Prometheus

---

### 8. tenant-service
**Port:** 8008 | **Language:** Java 21 | **Framework:** Spring Boot 3.3

**Role:**
Multi-tenant lifecycle management. Provisions new tenants (creates PostgreSQL schemas, Redis namespaces, Kafka topics), manages tenant configuration, feature flags, subscription tiers, and per-tenant encryption key references. Acts as the source of truth for tenant metadata consumed by all other services.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA
- Spring Data Redis — tenant config cache (sub-millisecond reads)
- PostgreSQL — tenant registry, configuration, feature flags
- common-lib
- SpringDoc OpenAPI, Micrometer + Prometheus

---

### 9. vendor-service
**Port:** 8009 | **Language:** Java | **Framework:** Spring Boot

**Role:**
Manages the third-party data processor (vendor) registry under DPDP §9.1–§9.2. Tracks vendor onboarding, Data Processing Agreements (DPAs), contract expiry, and compliance status. Provides vendor risk profiles consumed by risk-scoring-service.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA
- PostgreSQL — vendor registry, DPA documents, contract metadata
- Lombok
- common-lib
- SpringDoc OpenAPI

---

### 10. policy-service
**Port:** 8010 | **Language:** Java | **Framework:** Spring Boot

**Role:**
Manages DPDP compliance policies — privacy policies, data processing policies, and purpose definitions. Provides versioned policy documents, tracks policy acceptance by data principals, and enforces policy-based access rules consumed by consent-service and rights-service.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA
- PostgreSQL — policy versions, acceptance records
- Lombok
- common-lib
- SpringDoc OpenAPI

---

### 11. workflow-service
**Port:** 8011 | **Language:** Java | **Framework:** Spring Boot

**Role:**
State machine orchestration engine for long-running compliance workflows. Manages approval workflows (e.g., DPO approval for erasure), human-in-the-loop processing, escalation chains, and SLA enforcement. Coordinates multi-step processes across rights-service, breach-service, and vendor-service.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA, Spring Actuator
- Spring Kafka — event-driven workflow triggers and state transitions
- PostgreSQL — workflow instances, state, task assignments
- Lombok
- common-lib
- SpringDoc OpenAPI

---

### 12. config-service
**Port:** 8012 | **Language:** Java 17 | **Framework:** Spring Boot 3.3

**Role:**
Centralized configuration and secret management for all services. Stores per-tenant runtime configuration, feature toggles, and encrypted secrets. Provides a cached config API so services don't hit the database on every request.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA, Spring Validation, Spring Cache
- Spring Data Redis — distributed config cache
- Spring Kafka — config change event broadcasting
- PostgreSQL — configuration store
- Lombok
- Logstash Logback Encoder 7.4 — structured JSON logging
- SpringDoc OpenAPI 2.5.0, Micrometer + Prometheus

---

### 13. connector-service
**Port:** 8013 | **Language:** Java 17 | **Framework:** Spring Boot 3.3

**Role:**
Manages integrations with external data systems (CRMs, ERPs, cloud storage, SaaS apps). Handles connector onboarding, credential management, sync scheduling, data mapping, and audit logging of all data flows in and out of the platform.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA, Spring Validation, Spring Actuator
- Spring Kafka — publishes connector sync events
- PostgreSQL — connector registry, sync logs, credentials (encrypted)
- Lombok
- Logstash Logback Encoder 7.4
- SpringDoc OpenAPI 2.5.0, Micrometer + Prometheus

---

### 14. search-service
**Port:** 8014 | **Language:** Java 17 | **Framework:** Spring Boot 3.3

**Role:**
Tenant-scoped full-text search and audit index. Indexes consent records, rights requests, audit events, and breach incidents into Elasticsearch. Provides fast search APIs for compliance dashboards and DPO investigation workflows.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA, Spring Validation, Spring Actuator
- Spring Kafka — consumes indexing events from other services
- Elasticsearch Java Client 8.14.3 + elasticsearch-rest-client 8.14.3 — search index
- PostgreSQL — search metadata and index state
- Lombok
- Logstash Logback Encoder 7.4
- SpringDoc OpenAPI 2.5.0, Micrometer + Prometheus

---

### 15. webhook-service
**Port:** 8015 | **Language:** Java 17 | **Framework:** Spring Boot 3.3

**Role:**
Outbound event fan-out to customer-registered webhook endpoints. Consumes platform events from Kafka, signs payloads with HMAC-SHA256, delivers to registered URLs with exponential backoff retries, and tracks delivery status. Enables customers to integrate DataShield events into their own systems.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA, Spring Validation, Spring Actuator
- Spring Kafka — consumes all platform events for fan-out
- PostgreSQL — webhook registrations, delivery logs, retry queues
- Lombok
- Logstash Logback Encoder 7.4
- SpringDoc OpenAPI 2.5.0, Micrometer + Prometheus

---

### 16. siem-service
**Port:** 8016 | **Language:** Java 17 | **Framework:** Spring Boot 3.3

**Role:**
Security Information and Event Management (SIEM) integration bridge. Forwards security-relevant platform events (breach detections, anomalous access, policy violations) to enterprise SIEM systems — Splunk (HEC), IBM QRadar (Syslog/CEF), and Microsoft Azure Sentinel (Log Analytics API).

**Tech Stack:**
- Spring Boot Web, Spring Data JPA, Spring Validation, Spring Actuator
- Spring Kafka — consumes security events from audit, breach, and anomaly-detection services
- PostgreSQL — SIEM forwarding logs, integration configs
- Lombok
- Logstash Logback Encoder 7.4
- SpringDoc OpenAPI 2.5.0, Micrometer + Prometheus

---

### 17. analytics-service
**Port:** 8017 | **Language:** Java | **Framework:** Spring Boot

**Role:**
Real-time compliance metrics and trend analytics. Aggregates events from Kafka to compute consent rates, rights request volumes, breach frequency, and SLA compliance percentages. Feeds data to the compliance dashboard frontend.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA
- Spring Kafka — consumes events from all services for aggregation
- PostgreSQL — aggregated metrics, time-series summaries
- Lombok
- common-lib
- SpringDoc OpenAPI

---

### 18. report-service
**Port:** 8018 | **Language:** Java | **Framework:** Spring Boot

**Role:**
Scheduled compliance report generation and delivery. Produces periodic reports (daily, weekly, monthly, quarterly) — consent summaries, DSAR completion rates, breach timelines, vendor compliance scores — and delivers them via email or S3 download links.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA
- PostgreSQL — report definitions, schedules, generated report metadata
- Lombok
- common-lib
- SpringDoc OpenAPI

---

### 19. retention-service
**Port:** 8019 | **Language:** Java | **Framework:** Spring Boot

**Role:**
Automated data retention policy enforcement under DPDP §11–§12. Evaluates retention rules against data records across all services, triggers deletion or anonymization workflows when retention periods expire, and logs all retention actions to audit-service.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA
- PostgreSQL — retention policies, scheduled jobs, execution logs
- Lombok
- common-lib
- SpringDoc OpenAPI

---

### 20. grievance-service
**Port:** 8020 | **Language:** Java | **Framework:** Spring Boot

**Role:**
Grievance redressal system under DPDP §18. Allows data principals to file complaints, tracks resolution within the 30-day statutory SLA, manages escalation to the Data Protection Board, and provides a full audit trail of grievance handling.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA
- PostgreSQL — grievance tickets, SLA timers, resolution notes
- Lombok
- common-lib
- SpringDoc OpenAPI

---

### 21. data-classification-service
**Port:** 8021 | **Language:** Java 17 | **Framework:** Spring Boot 3.2

**Role:**
Classifies data assets by sensitivity level (Public, Internal, Confidential, Restricted, PII, Sensitive PII) and enforces DLP (Data Loss Prevention) rules. Assigns classification labels to data discovered by data-discovery-service and enforces access controls based on classification.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA
- Spring Kafka — consumes discovery events, publishes classification results
- PostgreSQL — classification rules, data asset labels
- Lombok
- SpringDoc OpenAPI 2.2.0

---

### 22. data-discovery-service
**Port:** 8022 | **Language:** Java 17 | **Framework:** Spring Boot 3.2

**Role:**
Scans connected data sources (databases, file stores, APIs via connector-service) to discover and catalog data assets. Identifies PII fields, maps data flows, and feeds discovered assets to data-classification-service and data-lineage-service.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA, Spring Actuator
- Spring Kafka — publishes discovery scan results
- Elasticsearch REST High Level Client 7.17.13 — indexes discovered assets for search
- Commons IO 2.14.0 — file scanning utilities
- PostgreSQL — scan jobs, discovered asset catalog
- Lombok
- SpringDoc OpenAPI 2.2.0

---

### 23. data-lineage-service
**Port:** 8023 | **Language:** Java 17 | **Framework:** Spring Boot 3.2

**Role:**
Tracks data provenance and lineage — where data originated, how it was transformed, and where it flows. Builds a lineage graph of data movements across systems, which is used for DPDP impact assessments and erasure completeness verification.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA
- Spring Kafka — consumes data movement events from connector and discovery services
- PostgreSQL — lineage graph nodes and edges
- Lombok
- SpringDoc OpenAPI 2.2.0

---

### 24. pii-detection-service
**Port:** 8024 | **Language:** Java 17 | **Framework:** Spring Boot 3.2

**Role:**
Java-side orchestration layer for PII detection. Receives scan requests, coordinates with the Python pii-detection microservice for ML-based detection, aggregates results, and stores findings. Acts as the bridge between the Java platform and the Python ML runtime.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA, Spring Validation
- PostgreSQL — detection jobs, findings
- Lombok
- SpringDoc OpenAPI 2.2.0

---

### 25. ai-analysis-service
**Port:** 8025 | **Language:** Java 17 | **Framework:** Spring Boot 3.2

**Role:**
Java-side orchestration for AI/ML analysis tasks. Coordinates with the Python ai-analysis microservice for anomaly detection and predictive compliance analytics. Manages job queuing, result storage, and exposes REST APIs to the compliance dashboard.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA
- Spring Kafka — triggers analysis jobs from platform events
- PostgreSQL — analysis jobs, results
- Lombok
- SpringDoc OpenAPI 2.2.0

---

### 26. anomaly-detection-service
**Port:** 8026 | **Language:** Java 17 | **Framework:** Spring Boot 3.2

**Role:**
Java-side orchestration for behavioral anomaly detection. Coordinates with the Python anomaly-detection microservice to detect unusual data access patterns, potential insider threats, and policy violations. Feeds alerts to siem-service and audit-service.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA, Spring Validation
- PostgreSQL — anomaly alerts, baseline profiles
- Lombok
- SpringDoc OpenAPI 2.2.0

---

### 27. risk-scoring-service
**Port:** 8027 | **Language:** Java 17 | **Framework:** Spring Boot 3.2

**Role:**
Java-side orchestration for vendor and data risk scoring. Coordinates with the Python risk-scoring microservice to compute composite risk scores for vendors, data assets, and processing activities. Scores feed into vendor-service and the compliance dashboard.

**Tech Stack:**
- Spring Boot Web, Spring Data JPA, Spring Validation
- PostgreSQL — risk scores, scoring history, trend data
- Lombok
- SpringDoc OpenAPI 2.2.0

---

## Python ML Microservices

These are standalone FastAPI services that run alongside their Java counterparts and handle the ML/AI workloads.

### pii-detection (Python)
**Role:** ML-based PII detection in unstructured text and structured data. Uses NLP models (spaCy, NLTK) and scikit-learn classifiers to identify names, Aadhaar numbers, PAN, phone numbers, email addresses, and other PII types defined under DPDP.

**Tech Stack:** FastAPI, Uvicorn, SQLAlchemy 2.0, PostgreSQL (psycopg2-binary), Pydantic v2, scikit-learn 1.3.2, NumPy 1.26.2, pandas 2.1.3, NLTK 3.8.1, spaCy 3.7.2, kafka-python 2.0.2, python-jose (JWT auth)

---

### ai-analysis (Python)
**Role:** Predictive compliance analytics — forecasts consent expiry volumes, predicts breach likelihood from access patterns, and generates compliance health scores using time-series analysis and ML models.

**Tech Stack:** FastAPI, Uvicorn, SQLAlchemy 2.0, PostgreSQL, Pydantic v2, scikit-learn 1.3.2, NumPy, pandas, NLTK, spaCy, kafka-python, python-jose

---

### anomaly-detection (Python)
**Role:** Behavioral anomaly detection on data access logs. Uses unsupervised ML (Isolation Forest, DBSCAN via scikit-learn) to detect unusual access patterns, bulk data exports, off-hours access, and other indicators of data misuse or breach.

**Tech Stack:** FastAPI, Uvicorn, SQLAlchemy 2.0, PostgreSQL, Pydantic v2, scikit-learn 1.3.2, NumPy, pandas, NLTK, spaCy, kafka-python, python-jose

---

### risk-scoring (Python)
**Role:** Composite risk scoring engine for vendors and data processing activities. Computes weighted risk scores from multiple signals (DPA status, breach history, data sensitivity, access controls) using ML regression models.

**Tech Stack:** FastAPI, Uvicorn, SQLAlchemy 2.0, PostgreSQL, Pydantic v2, scikit-learn 1.3.2, NumPy, pandas, NLTK, spaCy, kafka-python, python-jose

---

## Shared Libraries

### common-lib
**Role:** Shared Java library used by all Spring Boot services. Contains: global exception handling, standardized API response wrappers, structured logging utilities, tenant context propagation (ThreadLocal), base entity classes, and common validation utilities.

**Tech Stack:** Java 21, Spring Boot 3.3, Maven

---

### event-schemas
**Role:** Shared Avro schema definitions for all Kafka events across the platform. Ensures schema compatibility between producers and consumers. Contains schemas for: ConsentGranted, ConsentWithdrawn, RightsRequest, BreachDetected, AuditEvent, and more.

**Tech Stack:** Java 21, Apache Avro, Maven

---

## Frontend Applications

### compliance-dashboard
**Role:** Angular SPA for DPOs (Data Protection Officers) and compliance teams. Provides real-time dashboards for consent metrics, rights request queues, breach timelines, vendor compliance scores, and audit log search.

**Tech Stack:** Angular 17, TypeScript, TailwindCSS, Angular Material, RxJS, Chart.js

---

### data-principal-portal
**Role:** Self-service portal for data principals (end users). Allows users to view and manage their consents, submit rights requests (access, correction, erasure), track request status, and file grievances.

**Tech Stack:** Angular 17, TypeScript, TailwindCSS, RxJS

---

### consent-widget (SDK)
**Role:** Embeddable JavaScript SDK that any website can drop in to show a DPDP-compliant consent banner. Handles purpose-specific consent collection, stores consent locally and syncs to consent-service API, supports light/dark themes and multiple languages.

**Tech Stack:** TypeScript, Rollup 4.x (UMD + ESM output), @rollup/plugin-typescript, @rollup/plugin-terser, tslib

---

## Infrastructure

| Component | Technology | Purpose |
|---|---|---|
| Container runtime | Docker 20.10+ | Local dev and CI |
| Orchestration | Kubernetes 1.24+ | Production deployment |
| Message broker | Apache Kafka (AWS MSK) | Async event bus |
| Primary database | PostgreSQL (AWS RDS) | All service data, schema-per-tenant |
| Cache | Redis (AWS ElastiCache) | Sessions, consent cache, config cache |
| Search | Elasticsearch 8.x | Audit search, data asset catalog |
| Object storage | AWS S3 + Object Lock | Audit WORM archive, report storage |
| Metrics | Prometheus + Grafana | Platform observability |
| Tracing | OpenTelemetry + Jaeger | Distributed tracing |
| Logging | Logstash Logback Encoder → ELK | Structured JSON logs |
| Secrets | AWS Secrets Manager / HSM | Per-tenant encryption keys |
| CDN / Gateway | AWS API Gateway + CloudFront | API routing, rate limiting |

---

## Port Reference

| Service | Port |
|---|---|
| auth-service | 8001 |
| consent-service | 8002 |
| rights-service | 8003 |
| breach-service | 8004 |
| dpbi-service | 8005 |
| notification-service | 8006 |
| audit-service | 8007 |
| tenant-service | 8008 |
| vendor-service | 8009 |
| policy-service | 8010 |
| workflow-service | 8011 |
| config-service | 8012 |
| connector-service | 8013 |
| search-service | 8014 |
| webhook-service | 8015 |
| siem-service | 8016 |
| analytics-service | 8017 |
| report-service | 8018 |
| retention-service | 8019 |
| grievance-service | 8020 |
| data-classification-service | 8021 |
| data-discovery-service | 8022 |
| data-lineage-service | 8023 |
| pii-detection-service | 8024 |
| ai-analysis-service | 8025 |
| anomaly-detection-service | 8026 |
| risk-scoring-service | 8027 |
| pii-detection (Python) | 8101 |
| ai-analysis (Python) | 8102 |
| anomaly-detection (Python) | 8103 |
| risk-scoring (Python) | 8104 |
| compliance-dashboard (Angular) | 4200 |
| data-principal-portal (Angular) | 4201 |

---

## DPDP Act 2023 Coverage Map

| DPDP Section | Service(s) Responsible |
|---|---|
| §4 — Grounds for processing | consent-service, policy-service |
| §6 — Consent | consent-service |
| §7 — Notice | notification-service, consent-service |
| §8 — Obligations of fiduciary | breach-service, audit-service, retention-service |
| §9 — Children's data | consent-service |
| §9.1–9.2 — Data processors | vendor-service |
| §11–12 — Retention & erasure | retention-service, rights-service |
| §13–14A — Data principal rights | rights-service |
| §18 — Grievance redressal | grievance-service |
| Cross-cutting | auth-service, tenant-service, audit-service |

---

**Version:** 2.0
**Last Updated:** 2026-07-03
**Status:** In Development 🚀
