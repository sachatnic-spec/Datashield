# 📚 DataShield Documentation Index

## Where to Start?

### 🚀 **First Time? Read This First**
- **[START_HERE.md](START_HERE.md)** ← Start here! Visual overview with all quick commands

### ⚡ **Quick Reference (5 minutes)**
- **[QUICK_START.md](QUICK_START.md)** - Services list, common commands, access points

### 📖 **Complete Setup Guides**
- **[SETUP_COMPLETE.md](SETUP_COMPLETE.md)** - Full setup with architecture, troubleshooting
- **[LOCAL_SETUP.md](LOCAL_SETUP.md)** - Java services specific setup
- **[PYTHON_SETUP.md](PYTHON_SETUP.md)** - Python services specific setup

---

## Files Created for Local Development

### Configuration Files
- ✅ **docker-compose.local.yml** - PostgreSQL + Observability stack
- ✅ **.env.local** - Environment variables for remote infrastructure

### Startup Scripts
- ✅ **START_SERVICES.bat** - Easiest way to start (Windows batch)
- ✅ **start-local.ps1** - PowerShell startup script
- ✅ **profile.ps1** - PowerShell helper functions

### Documentation
- ✅ **START_HERE.md** - Visual quick start guide
- ✅ **QUICK_START.md** - Command reference
- ✅ **SETUP_COMPLETE.md** - Comprehensive guide
- ✅ **LOCAL_SETUP.md** - Local setup details
- ✅ **PYTHON_SETUP.md** - Python services guide
- ✅ **README_INDEX.md** - This file

---

## Architecture Overview

```
Your Machine (Local)           Remote Server (10.197.214.105)
═══════════════════════════════════════════════════════════
                               
PostgreSQL                     Redis
(Port 5432)    ────────────    (Port 6379)
                        │
                    Connects
                        │
Jaeger, Prometheus,     Kafka
Grafana                (Port 9092)
(Local)    ────────────────
                │
            Connects
                │
         Elasticsearch
         (Port 9200)
```

---

## Services at a Glance

### Java Services (Spring Boot)
| # | Service | Port | Purpose |
|---|---------|------|---------|
| 1 | Auth | 8001 | JWT/OAuth2 authentication |
| 2 | Consent | 8002 | Granular consent tracking |
| 3 | Rights | 8003 | DSAR orchestration |
| 4 | Breach | 8004 | Incident management |
| 5 | Notification | 8005 | Event notifications |

### Python Services (FastAPI)
| # | Service | Port | Purpose |
|---|---------|------|---------|
| 6 | AI Analysis | 8018 | ML model insights |
| 7 | PII Detection | 8019 | Data detection |
| 8 | Risk Scoring | 8020 | Risk assessment |
| 9 | Anomaly Detection | 8021 | Outlier detection |

---

## How to Get Started

### Option A: Easiest (Recommended)
```batch
# Windows Command Prompt or PowerShell
START_SERVICES.bat
```
This will:
1. Start Docker containers
2. Display all configuration
3. Show startup commands for each service

### Option B: PowerShell
```powershell
.\start-local.ps1
```

### Option C: Manual with Helpers
```powershell
. .\profile.ps1
Start-Docker-Stack
Start-Auth
Start-AIAnalysis
Check-Services
```

### Option D: Complete Manual
```bash
# Terminal 1
docker-compose -f docker-compose.local.yml up -d

# Terminal 2
mvn clean install

# Terminal 3-11: One service per terminal
cd services\auth-service
mvn spring-boot:run
```

---

## Access Your Services

### API Documentation
- Auth: http://localhost:8001/swagger-ui.html
- Consent: http://localhost:8002/swagger-ui.html
- Rights: http://localhost:8003/swagger-ui.html
- Breach: http://localhost:8004/swagger-ui.html
- Notification: http://localhost:8005/swagger-ui.html
- AI Analysis: http://localhost:8018/docs
- PII Detection: http://localhost:8019/docs
- Risk Scoring: http://localhost:8020/docs
- Anomaly Detection: http://localhost:8021/docs

### Monitoring & Observability
- Jaeger: http://localhost:16686 (Distributed tracing)
- Prometheus: http://localhost:9090 (Metrics)
- Grafana: http://localhost:3000 (Dashboards - admin/admin)

---

## Common Commands

### Docker Management
```bash
# Start containers
docker-compose -f docker-compose.local.yml up -d

# Stop containers
docker-compose -f docker-compose.local.yml down

# View logs
docker-compose -f docker-compose.local.yml logs -f

# Clean everything
docker-compose -f docker-compose.local.yml down -v
```

### Java Services
```bash
# Build all
mvn clean install

# Run specific service
cd services/auth-service
mvn spring-boot:run

# Run tests
mvn test

# Build with debug
mvn clean install -X
```

### Python Services
```bash
# Install dependencies
pip install -r requirements.txt

# Run with hot-reload
uvicorn app.main:app --reload --port 8018

# Run tests
pytest tests/ -v

# Check coverage
pytest tests/ --cov=app --cov-report=html
```

---

## Infrastructure Configuration

### Local (Your Machine)
- **PostgreSQL:** localhost:5432
  - User: datasheild
  - Password: datasheild_dev_pwd
  - Database: datasheild

- **Jaeger:** http://localhost:16686
- **Prometheus:** http://localhost:9090
- **Grafana:** http://localhost:3000

### Remote (10.197.214.105)
- **Redis:** 10.197.214.105:6379
- **Kafka:** 10.197.214.105:9092
- **Elasticsearch:** http://10.197.214.105:9200

---

## Troubleshooting Quick Links

### Common Issues
| Issue | Solution |
|-------|----------|
| Docker not found | Install Docker Desktop, add to PATH |
| Port already in use | `Get-NetTCPConnection -LocalPort 8001` |
| PostgreSQL won't start | `docker-compose down -v && up postgres -d` |
| Python module errors | `python -m spacy download en_core_web_sm` |
| Kafka connection refused | Check: `Test-NetConnection 10.197.214.105 -Port 9092` |
| Maven not found | Add Maven to PATH or install |

For detailed troubleshooting, see **SETUP_COMPLETE.md**

---

## Project Statistics

- **Total Microservices:** 27
- **Running Locally:** 9 services
- **Java Services:** 5 (Spring Boot)
- **Python Services:** 4 (FastAPI)
- **Local Containers:** 4 (PostgreSQL, Jaeger, Prometheus, Grafana)
- **Remote Services:** 3 (Redis, Kafka, Elasticsearch)
- **API Ports:** 8001-8005 (Java), 8018-8021 (Python)
- **Observability Ports:** 16686, 9090, 3000

---

## Documentation File Structure

```
/
├── START_HERE.md           ← Begin here!
├── QUICK_START.md          ← 5-min reference
├── SETUP_COMPLETE.md       ← Full guide
├── LOCAL_SETUP.md          ← Java details
├── PYTHON_SETUP.md         ← Python details
├── README_INDEX.md         ← This file
├── README.md               ← Project overview
├── MASTER_PLAN.md          ← Architecture
└── Configuration Files
    ├── docker-compose.local.yml
    ├── .env.local
    ├── START_SERVICES.bat
    ├── start-local.ps1
    └── profile.ps1
```

---

## Next Steps

1. ✅ **Review START_HERE.md** (2 minutes)
2. ✅ **Run START_SERVICES.bat** (instant setup)
3. ✅ **Wait for Docker to start** (30 seconds)
4. ✅ **Build Maven** (5-10 minutes)
5. 🔄 **Start services in 9 terminals** (one command each)
6. 🔄 **Test APIs** via Swagger UI
7. 🔄 **Monitor** with Grafana
8. 🔄 **Trace** with Jaeger

---

## Quick Tips

- **Use separate terminals** for each service
- **Enable hot-reload** in Python services
- **Monitor Prometheus** for metrics
- **Check Jaeger** for traces
- **Cache with Redis** for performance
- **Use Grafana** for dashboards

---

## Support & Help

- **Quick questions?** Check **QUICK_START.md**
- **Setup issues?** See **SETUP_COMPLETE.md**
- **Java help?** Read **LOCAL_SETUP.md**
- **Python help?** Read **PYTHON_SETUP.md**
- **Architecture?** Read **MASTER_PLAN.md**

---

**Last Updated:** June 24, 2026  
**Setup Status:** ✅ Complete & Ready  
**Environment:** Local Development with Remote Infrastructure

