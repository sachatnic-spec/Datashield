# 🚀 DataShield Local Development - Quick Reference

## What You Have

- **27 Microservices** (DPDP Act 2023 compliance platform)
- **5 Java Services** (Spring Boot) - Ports 8001-8005
- **4 Python Services** (FastAPI) - Ports 8018-8021
- **Remote Infrastructure** on `10.197.214.105` (Redis, Kafka, Elasticsearch)
- **Local PostgreSQL** via Docker

---

## 30-Second Start

```bash
# Terminal 1: Start Docker containers
docker-compose -f docker-compose.local.yml up -d

# Terminal 2: Build Java services
mvn clean install -DskipTests

# Then: Open 9 more terminals and run services individually
```

---

## Windows Shortcuts

### Batch File (Easiest)
```bash
START_SERVICES.bat
```
This will:
1. Start Docker containers
2. Display configuration
3. Show all startup commands

### PowerShell Profile
```powershell
# Add to PowerShell profile
. .\profile.ps1

# Then use commands:
Start-Docker-Stack          # Start Docker
Start-Auth                  # Start Auth Service
Start-AIAnalysis            # Start Python service
Check-Services              # Health check all services
```

---

## Services at a Glance

### Java Services (Spring Boot)
| Service | Port | Command |
|---------|------|---------|
| **Auth** | 8001 | `cd services\auth-service && mvn spring-boot:run` |
| **Consent** | 8002 | `cd services\consent-service && mvn spring-boot:run` |
| **Rights** | 8003 | `cd services\rights-service && mvn spring-boot:run` |
| **Breach** | 8004 | `cd services\breach-service && mvn spring-boot:run` |
| **Notification** | 8005 | `cd services\notification-service && mvn spring-boot:run` |

### Python Services (FastAPI)
| Service | Port | Command |
|---------|------|---------|
| **AI Analysis** | 8018 | `cd services\ai-analysis && pip install -r requirements.txt && uvicorn app.main:app --port 8018 --reload` |
| **PII Detection** | 8019 | `cd services\pii-detection && pip install -r requirements.txt && uvicorn app.main:app --port 8019 --reload` |
| **Risk Scoring** | 8020 | `cd services\risk-scoring && pip install -r requirements.txt && uvicorn app.main:app --port 8020 --reload` |
| **Anomaly Detection** | 8021 | `cd services\anomaly-detection && pip install -r requirements.txt && uvicorn app.main:app --port 8021 --reload` |

---

## Access Everything

### API Documentation
- **Auth**: http://localhost:8001/swagger-ui.html
- **Consent**: http://localhost:8002/swagger-ui.html
- **Rights**: http://localhost:8003/swagger-ui.html
- **Breach**: http://localhost:8004/swagger-ui.html
- **Notification**: http://localhost:8005/swagger-ui.html
- **AI Analysis**: http://localhost:8018/docs
- **PII Detection**: http://localhost:8019/docs
- **Risk Scoring**: http://localhost:8020/docs
- **Anomaly Detection**: http://localhost:8021/docs

### Observability
- **Jaeger (Tracing)**: http://localhost:16686
- **Prometheus (Metrics)**: http://localhost:9090
- **Grafana (Dashboards)**: http://localhost:3000 (admin/admin)

### Databases
- **PostgreSQL**: `localhost:5432` (datasheild/datasheild_dev_pwd)
- **Redis**: `10.197.214.105:6379`
- **Elasticsearch**: http://10.197.214.105:9200

---

## Configuration

### `.env.local` (Already Created)
Points all services to:
- PostgreSQL: `localhost:5432`
- Redis: `10.197.214.105:6379`
- Kafka: `10.197.214.105:9092`
- Elasticsearch: `10.197.214.105:9200`

---

## Common Issues

| Problem | Solution |
|---------|----------|
| **Docker not found** | Install Docker Desktop, add to PATH |
| **Port already in use** | `Get-NetTCPConnection -LocalPort 8001` then kill PID |
| **PostgreSQL won't start** | `docker-compose -f docker-compose.local.yml down -v && docker-compose -f docker-compose.local.yml up postgres -d` |
| **Python module errors** | `python -m spacy download en_core_web_sm` |
| **Kafka connection refused** | Check: `Test-NetConnection -ComputerName 10.197.214.105 -Port 9092` |
| **mvn not found** | Add Maven to PATH or use `mvn.cmd` |

---

## Documentation Files

- **SETUP_COMPLETE.md** - Comprehensive setup guide
- **LOCAL_SETUP.md** - Detailed local development setup
- **PYTHON_SETUP.md** - Python services guide
- **README.md** - Project overview
- **MASTER_PLAN.md** - Project architecture

---

## Performance Tips

1. **Use separate terminals** for each service
2. **Enable hot-reload** in Python services (--reload flag)
3. **Cache with Redis** for frequently accessed data
4. **Monitor with Grafana** for real-time insights
5. **Use virtual environments** for Python

---

## Next Steps

1. ✅ Run `START_SERVICES.bat`
2. ✅ Verify Docker containers
3. ✅ Build Maven modules
4. ✅ Start services in separate terminals
5. 🔄 Access http://localhost:8001 (or your service)
6. 🔄 Test APIs via Swagger/OpenAPI UI
7. 🔄 Monitor with Jaeger/Grafana

---

## Quick Help

```powershell
# Load helper functions
. .\profile.ps1

# Available commands:
Show-Help
Go-DataShield
Start-Docker-Stack
Start-Auth
Start-PII
Check-Services
```

---

**Happy coding! 🎉**

