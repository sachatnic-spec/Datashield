# DataShield - Ready to Go! 🚀

## Your Setup at a Glance

```
┌─────────────────────────────────────────────────────────────────────┐
│                  DataShield India - Local Development              │
│              (27 Microservices DPDP Compliance Platform)          │
└─────────────────────────────────────────────────────────────────────┘

┌──────────────────────┐         ┌──────────────────────────┐
│   LOCAL DOCKER       │         │  REMOTE (10.197.214.105) │
├──────────────────────┤         ├──────────────────────────┤
│ ✓ PostgreSQL:5432    │         │ ✓ Redis:6379            │
│ ✓ Jaeger:16686       │         │ ✓ Kafka:9092            │
│ ✓ Prometheus:9090    │         │ ✓ Elasticsearch:9200    │
│ ✓ Grafana:3000       │         │                          │
└──────────────────────┘         └──────────────────────────┘

┌────────────────────────────────────────────────────────────────┐
│                    9 SERVICES READY TO RUN                     │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  JAVA Services (Spring Boot) - Ports 8001-8005               │
│  ──────────────────────────────────────────────────────────  │
│  1. Auth Service              :8001 → JWT/OAuth2            │
│  2. Consent Service           :8002 → Granular consent      │
│  3. Rights Service            :8003 → DSAR orchestration    │
│  4. Breach Service            :8004 → Incident management   │
│  5. Notification Service      :8005 → Event notifications   │
│                                                              │
│  PYTHON Services (FastAPI) - Ports 8018-8021               │
│  ──────────────────────────────────────────────────────────  │
│  6. AI Analysis              :8018 → ML insights           │
│  7. PII Detection            :8019 → Sensitive data detect │
│  8. Risk Scoring             :8020 → Risk assessment       │
│  9. Anomaly Detection        :8021 → Outlier detection     │
│                                                            │
└────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────┐
│                        STARTUP OPTIONS                        │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  🟢 EASIEST (Windows)                                         │
│     $ START_SERVICES.bat                                      │
│                                                                │
│  🔵 PowerShell                                                │
│     PS> .\start-local.ps1                                     │
│                                                                │
│  🟣 With Helper Functions                                     │
│     PS> . .\profile.ps1                                       │
│     PS> Start-Docker-Stack                                    │
│     PS> Start-Auth                                            │
│     PS> Start-AIAnalysis                                      │
│                                                                │
│  🟡 Manual (Step by step)                                     │
│     $ docker-compose -f docker-compose.local.yml up -d       │
│     # Open 9 terminals for each service                      │
│                                                                │
└────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────┐
│                      ACCESS YOUR SERVICES                     │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  Java APIs (Swagger UI):                                      │
│  ✓ http://localhost:8001/swagger-ui.html   (Auth)           │
│  ✓ http://localhost:8002/swagger-ui.html   (Consent)        │
│  ✓ http://localhost:8003/swagger-ui.html   (Rights)         │
│  ✓ http://localhost:8004/swagger-ui.html   (Breach)         │
│  ✓ http://localhost:8005/swagger-ui.html   (Notification)   │
│                                                              │
│  Python APIs (OpenAPI UI):                                   │
│  ✓ http://localhost:8018/docs  (AI Analysis)               │
│  ✓ http://localhost:8019/docs  (PII Detection)             │
│  ✓ http://localhost:8020/docs  (Risk Scoring)              │
│  ✓ http://localhost:8021/docs  (Anomaly Detection)         │
│                                                              │
│  Observability Dashboards:                                   │
│  ✓ http://localhost:16686  (Jaeger Tracing)                │
│  ✓ http://localhost:9090   (Prometheus Metrics)            │
│  ✓ http://localhost:3000   (Grafana Dashboards)            │
│                                                              │
│  Databases:                                                  │
│  ✓ localhost:5432 (PostgreSQL)                              │
│  ✓ 10.197.214.105:6379 (Redis)                              │
│  ✓ 10.197.214.105:9092 (Kafka)                              │
│  ✓ 10.197.214.105:9200 (Elasticsearch)                      │
│                                                              │
└────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────┐
│                         QUICK COMMANDS                        │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  # Start Docker containers                                    │
│  $ docker-compose -f docker-compose.local.yml up -d          │
│                                                                │
│  # Build all Java modules                                     │
│  $ mvn clean install -DskipTests                              │
│                                                                │
│  # Stop Docker containers                                     │
│  $ docker-compose -f docker-compose.local.yml down            │
│                                                                │
│  # View PostgreSQL logs                                       │
│  $ docker logs datasheild-postgres -f                         │
│                                                                │
│  # Install Python dependencies                                │
│  $ cd services\ai-analysis && pip install -r requirements.txt │
│                                                                │
└────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────┐
│                    FILES CREATED FOR YOU                      │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  📄 docker-compose.local.yml   Local PostgreSQL + monitoring  │
│  📄 .env.local                 Remote infra configuration     │
│  📄 START_SERVICES.bat         Easy startup (Windows)         │
│  📄 start-local.ps1            PowerShell startup             │
│  📄 profile.ps1                Helper functions              │
│  📖 QUICK_START.md             5-minute quick start           │
│  📖 SETUP_COMPLETE.md          Full setup documentation       │
│  📖 LOCAL_SETUP.md             Java services details          │
│  📖 PYTHON_SETUP.md            Python services details        │
│                                                                │
└────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────┐
│                         NEXT STEPS                            │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  ✅ Setup Configuration          [COMPLETE]                   │
│  ✅ Environment Files Created     [COMPLETE]                   │
│  ✅ Documentation Generated       [COMPLETE]                   │
│  ⏳ Start Docker Containers       [DO THIS FIRST]              │
│  ⏳ Build Maven Modules           [Then do this]               │
│  ⏳ Start Individual Services      [9 terminals]               │
│  ⏳ Test APIs via Swagger UI       [Verify everything]         │
│  ⏳ Monitor with Grafana           [Optional - bonus]          │
│                                                                │
│  👉 RUN: START_SERVICES.bat  (or .\start-local.ps1)           │
│                                                                │
└────────────────────────────────────────────────────────────────┘

═══════════════════════════════════════════════════════════════════════
                    YOU'RE ALL SET - HAPPY CODING! 🎉
═══════════════════════════════════════════════════════════════════════
```

---

## What You're Running

### The DataShield Platform
A **DPDP Act 2023 Compliance** platform built with **microservices architecture** featuring:
- ✅ Enterprise-grade authentication (JWT/OAuth2)
- ✅ Granular consent management with purpose-specific tracking
- ✅ Data Subject Access Request (DSAR) orchestration
- ✅ Automated breach incident management
- ✅ AI-powered data insights and anomaly detection
- ✅ Real-time event streaming (Kafka)
- ✅ Full audit logging and traceability
- ✅ Multi-tenant support with RLS
- ✅ 100% India data residency

### Your Local Environment
- **Development Ready** - PostgreSQL on your machine, remote services on 10.197.214.105
- **Full Stack** - 9 microservices running locally
- **Hot Reload** - Python services auto-refresh on file changes
- **Observability** - Built-in tracing, metrics, and dashboards
- **Fast Feedback Loop** - Quick iteration and testing

---

## Tips for Success

1. **One Service Per Terminal** - Makes debugging easier
2. **Use Swagger UI** - Interactive API testing built-in
3. **Watch Jaeger** - See distributed traces in real-time
4. **Monitor Grafana** - Watch service metrics as you test
5. **Keep `.env.local`** - All config is centralized there
6. **Use Helper Functions** - Load `profile.ps1` for quick commands

---

## Still Need Help?

📖 **Read these in order:**
1. `QUICK_START.md` - 5-minute overview
2. `SETUP_COMPLETE.md` - Full setup details
3. `LOCAL_SETUP.md` - Java services specifics
4. `PYTHON_SETUP.md` - Python services specifics

---

**Version:** 1.0 Setup Complete
**Date:** June 24, 2026
**Status:** ✅ Ready to Run

