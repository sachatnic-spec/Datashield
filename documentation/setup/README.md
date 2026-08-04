# DataShield India

![Status](https://img.shields.io/badge/status-MVP-yellow)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring%20Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green)
![Kubernetes](https://img.shields.io/badge/Kubernetes-1.24%2B-blue)

Enterprise-grade **DPDP Act 2023 compliance** platform for India with granular consent management, data principal rights orchestration, and automated breach notification.

## 📋 Project Structure

```
datasheild/
├── services/                      # 27 microservices (Maven modules)
│   ├── auth-service/
│   ├── consent-service/
│   ├── rights-service/
│   ├── breach-service/
│   ├── vendor-service/
│   ├── notification-service/
│   ├── audit-service/
│   ├── tenant-service/
│   └── ... (19 more services)
├── libs/                          # Shared libraries
│   ├── common-lib/                # Utilities, error handling, logging
│   ├── event-schemas/             # Avro schemas for Kafka
│   └── openapi-spec/              # Shared OpenAPI definitions
├── infra/                         # Infrastructure as Code
│   ├── docker-compose.yml         # Local dev environment
│   ├── kubernetes/                # K8s manifests
│   └── terraform/                 # AWS infrastructure
├── frontend/                      # Angular SPA + React Data Principal Portal
├── docs/                          # Architecture ADRs, API specs
├── tests/                         # E2E and integration tests
├── DOC/                           # Original SRS, Architecture, SOPs docs
├── pom.xml                        # Maven parent POM
└── README.md

```

## 🎯 MVP Scope

**Phases 1–3:** 
- Auth + JWT/OAuth2
- Consent Management (granular, purpose-specific)
- Data Principal Rights (DSAR orchestration)
- Breach Incident + DPBI notification
- Vendor Registry
- Notifications & Audit Logging

**Excluded (Phase 4+):** Data Intelligence, AI Services, Advanced Reporting

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker 20.10+
- Docker Compose
- Git

### Local Development

```bash
# Clone and navigate
git clone https://github.com/your-org/datasheild.git
cd datasheild

# Start infrastructure (PostgreSQL, Redis, Kafka, Jaeger)
docker-compose -f infra/docker-compose.yml up -d

# Wait for services to be healthy
sleep 10

# Build all services
mvn clean install

# Start auth-service (on port 8001)
cd services/auth-service
mvn spring-boot:run

# Start consent-service (on port 8002) in another terminal
cd services/consent-service
mvn spring-boot:run

# Access Swagger UI
# - Auth: http://localhost:8001/swagger-ui.html
# - Consent: http://localhost:8002/swagger-ui.html
# - Kafka Topics: docker exec datasheild-kafka kafka-topics --list --bootstrap-server localhost:9092
```

## 📚 Documentation

- [SRS (Software Requirements Specification)](DOC/DataShield_India_SRS.md)
- [Architecture Document](DOC/DataShield_India_Architecture_Document.md)
- [Microservice SOPs](DOC/DataShield_India_Microservice_SOPs.md)
- [Implementation Plan](PLAN.md)
- [API Docs](#) (Swagger link)
- [ADRs](docs/adr/)

## 🏗️ Architecture Highlights

- **Schema-per-Tenant:** PostgreSQL with row-level security (RLS)
- **Event-Driven:** Kafka (MSK) with outbox pattern + CDC (Debezium)
- **Async Sagas:** Temporal.io for distributed erasure transactions
- **Security:** AES-256 encryption, TLS 1.3, HSM per-tenant keys (Enterprise)
- **Compliance:** 100% India data residency, DPDP §4–§20 coverage
- **Performance:** p95 < 200ms, p99 < 500ms, 100,000 TPS at peak
- **Observability:** OpenTelemetry + Jaeger, Prometheus + Grafana, structured logging

## 🔒 Security & Compliance

- ✅ DPDP Act 2023 (§4–§20) compliant
- ✅ RBI data localization (Mumbai + Hyderabad DR)
- ✅ OWASP ASVS L2 baseline
- ✅ Immutable audit logs (S3 Object Lock)
- ✅ Per-tenant encryption keys
- ✅ 72-hour breach notification (automated)
- ✅ WCAG 2.1 AA accessibility

## 📊 Performance Targets

| Metric | Target | Status |
|--------|--------|--------|
| API p95 latency | < 200ms | 🔨 Building |
| API p99 latency | < 500ms | 🔨 Building |
| Consent widget load | < 100ms | 🔨 Building |
| Peak consent TPS | 100,000 | 🔨 Building |
| Platform SLA | 99.99% | 🔨 Building |
| RTO (Disaster Recovery) | < 15 min | 🔨 Building |
| RPO | < 5 min | 🔨 Building |

## 🧪 Testing

```bash
# Run unit tests
mvn clean test

# Run integration tests
mvn clean verify -P integration-tests

# E2E tests (requires services running)
cd tests/e2e
npm install
npm run test
```

## 📈 Release Roadmap

- **V1.0 (MVP):** Core compliance services, basic dashboards — Jul 2026
- **V1.1:** Data Discovery & Classification — Sep 2026
- **V1.2:** AI-assisted risk scoring — Nov 2026
- **V2.0:** Full 27-service platform — Q1 2027

## 🤝 Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines.

## 📄 License

Proprietary — DataShield India Private Limited

## 📞 Support

- 📧 **Engineering:** eng@datasheild.in
- 🐛 **Bug Reports:** Create an issue on GitHub

---

**Version:** 1.0 (MVP)  
**Last Updated:** 2026-06-21  
**Status:** In Development 🚀
