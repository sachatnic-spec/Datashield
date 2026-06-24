# DataShield Local Development - Complete Setup Guide

## Quick Summary

You have a **27-microservice DPDP Act 2023 compliance platform** with:
- **5 Java services** (Spring Boot + Maven)
- **4 Python services** (FastAPI + Uvicorn)
- **Infrastructure:** PostgreSQL (local), Redis/Kafka/Elasticsearch (remote on 10.197.214.105)

---

## Project Structure

```
datasheild/
├── services/                    # 27 microservices
│   ├── auth-service/           # Java - Port 8001
│   ├── consent-service/        # Java - Port 8002
│   ├── rights-service/         # Java - Port 8003
│   ├── breach-service/         # Java - Port 8004
│   ├── notification-service/   # Java - Port 8005
│   ├── ai-analysis/            # Python - Port 8018
│   ├── pii-detection/          # Python - Port 8019
│   ├── risk-scoring/           # Python - Port 8020
│   └── anomaly-detection/      # Python - Port 8021
├── libs/                        # Shared libraries
├── infra/                       # Infrastructure configs
├── frontend/                    # Angular + React UIs
├── docker-compose.local.yml    # NEW - Local dev stack
├── .env.local                  # NEW - Environment config
├── start-local.ps1             # NEW - PowerShell startup
├── START_SERVICES.bat          # NEW - Batch startup
├── LOCAL_SETUP.md              # NEW - Local setup guide
├── PYTHON_SETUP.md             # NEW - Python guide
└── pom.xml                     # Maven parent POM
```

---

## Infrastructure Architecture

### Local Docker Containers
```
┌─────────────────────────────────────┐
│      PostgreSQL (localhost:5432)    │
│  - Datasheild database              │
│  - User: datasheild                 │
│  - Password: datasheild_dev_pwd     │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│   Observability Stack               │
│   - Jaeger (16686)                  │
│   - Prometheus (9090)               │
│   - Grafana (3000)                  │
└─────────────────────────────────────┘
```

### Remote Services (10.197.214.105)
```
┌────────────────────────────────────────┐
│   Redis (6379)                         │
│   - Session caching                    │
│   - Token blacklisting                 │
└────────────────────────────────────────┘

┌────────────────────────────────────────┐
│   Kafka (9092)                         │
│   - Event streaming                    │
│   - Consent events                     │
│   - Breach notifications               │
│   - Audit logs                         │
└────────────────────────────────────────┘

┌────────────────────────────────────────┐
│   Elasticsearch (9200)                 │
│   - Audit log search                   │
│   - Event indexing                     │
└────────────────────────────────────────┘
```

---

## Getting Started (3 Steps)

### Step 1: Start Local Infrastructure
```batch
# Run the batch file
START_SERVICES.bat

# OR using PowerShell
.\start-local.ps1

# OR manually with Docker Compose
docker-compose -f docker-compose.local.yml up -d
```

**Verify PostgreSQL is healthy:**
```powershell
docker exec datasheild-postgres pg_isready -U datasheild
# Expected output: accepting connections
```

### Step 2: Build Java Services
```bash
mvn clean install -DskipTests
# Takes 5-10 minutes on first run
```

### Step 3: Start Services (9 separate terminals)

#### Java Services
```bash
# Terminal 1 - Auth Service
cd services\auth-service
mvn spring-boot:run

# Terminal 2 - Consent Service
cd services\consent-service
mvn spring-boot:run

# Terminal 3 - Rights Service
cd services\rights-service
mvn spring-boot:run

# Terminal 4 - Breach Service
cd services\breach-service
mvn spring-boot:run

# Terminal 5 - Notification Service
cd services\notification-service
mvn spring-boot:run
```

#### Python Services
```bash
# Terminal 6 - AI Analysis
cd services\ai-analysis
python -m pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8018 --reload

# Terminal 7 - PII Detection
cd services\pii-detection
python -m pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8019 --reload

# Terminal 8 - Risk Scoring
cd services\risk-scoring
python -m pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8020 --reload

# Terminal 9 - Anomaly Detection
cd services\anomaly-detection
python -m pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8021 --reload
```

---

## Access Points

### API Endpoints

| Service | Port | Type | Docs |
|---------|------|------|------|
| **Auth** | 8001 | Spring Boot | http://localhost:8001/swagger-ui.html |
| **Consent** | 8002 | Spring Boot | http://localhost:8002/swagger-ui.html |
| **Rights** | 8003 | Spring Boot | http://localhost:8003/swagger-ui.html |
| **Breach** | 8004 | Spring Boot | http://localhost:8004/swagger-ui.html |
| **Notification** | 8005 | Spring Boot | http://localhost:8005/swagger-ui.html |
| **AI Analysis** | 8018 | FastAPI | http://localhost:8018/docs |
| **PII Detection** | 8019 | FastAPI | http://localhost:8019/docs |
| **Risk Scoring** | 8020 | FastAPI | http://localhost:8020/docs |
| **Anomaly Detection** | 8021 | FastAPI | http://localhost:8021/docs |

### Observability

| Tool | URL | Purpose |
|------|-----|---------|
| **Jaeger** | http://localhost:16686 | Distributed tracing |
| **Prometheus** | http://localhost:9090 | Metrics collection |
| **Grafana** | http://localhost:3000 | Dashboards (admin/admin) |

---

## Environment Variables

Located in `.env.local`:

```env
# PostgreSQL (Local)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/datasheild
SPRING_DATASOURCE_USERNAME=datasheild
SPRING_DATASOURCE_PASSWORD=datasheild_dev_pwd

# Redis (Remote)
SPRING_REDIS_HOST=10.197.214.105
SPRING_REDIS_PORT=6379

# Kafka (Remote)
SPRING_KAFKA_BOOTSTRAP_SERVERS=10.197.214.105:9092

# Elasticsearch (Remote)
SPRING_ELASTICSEARCH_REST_URIS=http://10.197.214.105:9200
```

---

## Common Commands

### Docker Management
```powershell
# Start all containers
docker-compose -f docker-compose.local.yml up -d

# Stop all containers
docker-compose -f docker-compose.local.yml down

# View logs
docker-compose -f docker-compose.local.yml logs -f

# Remove volumes (clean slate)
docker-compose -f docker-compose.local.yml down -v
```

### Java Services
```bash
# Build all
mvn clean install

# Build specific service
cd services/auth-service
mvn clean install

# Run with debug
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

# Run tests
mvn test
```

### Python Services
```bash
# Install dependencies
pip install -r requirements.txt

# Run with hot-reload (development)
uvicorn app.main:app --reload --port 8018

# Run without reload (production)
uvicorn app.main:app --workers 4 --port 8018

# Run tests
pytest tests/ -v

# Check coverage
pytest tests/ --cov=app --cov-report=html
```

---

## Testing Remote Infrastructure Connection

### Redis
```powershell
# Using redis-cli
redis-cli -h 10.197.214.105 ping
# Expected: PONG

# Using PowerShell
Test-NetConnection -ComputerName 10.197.214.105 -Port 6379
```

### Kafka
```powershell
# List topics
docker run --rm confluentinc/cp-kafka:7.5.0 \
  kafka-topics --bootstrap-server 10.197.214.105:9092 --list

# Test connection
Test-NetConnection -ComputerName 10.197.214.105 -Port 9092
```

### Elasticsearch
```powershell
# Health check
Invoke-WebRequest -Uri http://10.197.214.105:9200/_cluster/health

# Cluster status
Invoke-WebRequest -Uri http://10.197.214.105:9200/_cluster/stats
```

---

## Troubleshooting

### Docker Not Found
**Error:** `docker: The term 'docker' is not recognized`
- Ensure Docker Desktop is installed and running
- Add Docker to PATH: `C:\Program Files\Docker\Docker\resources\bin`

### PostgreSQL Connection Failed
```bash
# Check if container is running
docker ps | findstr postgres

# Check logs
docker logs datasheild-postgres

# Reset database
docker-compose -f docker-compose.local.yml down -v
docker-compose -f docker-compose.local.yml up postgres -d
```

### Python Module Not Found
```bash
# Install spacy language model
python -m spacy download en_core_web_sm

# Reinstall requirements
pip install --upgrade -r requirements.txt
```

### Port Already in Use
```powershell
# Find process using port 8018
Get-NetTCPConnection -LocalPort 8018

# Kill process
Stop-Process -Id <PID> -Force
```

### Kafka Connection Refused
- Verify network connectivity to 10.197.214.105
- Check Kafka is running: `telnet 10.197.214.105 9092`
- Check bootstrap servers in .env.local

---

## Performance Tips

1. **Use Docker for PostgreSQL** - Faster than separate installation
2. **Enable hot-reload for Python services** - Auto-refresh on file changes
3. **Run services in separate terminals** - Better visibility and control
4. **Use virtual environments for Python** - Avoid dependency conflicts
5. **Cache with Redis** - Reduce database queries
6. **Use Prometheus** - Monitor service metrics

---

## Next Steps

1. ✅ Run `START_SERVICES.bat`
2. ✅ Verify Docker containers are healthy
3. ✅ Build Maven modules
4. ✅ Start all services in separate terminals
5. 🔄 **Access APIs via Swagger/OpenAPI UI**
6. 🔄 Test endpoints
7. 🔄 Check observability dashboards

---

## Files Created for Local Development

- ✅ `docker-compose.local.yml` - Local PostgreSQL + observability
- ✅ `.env.local` - Environment variables for remote infrastructure
- ✅ `start-local.ps1` - PowerShell startup script
- ✅ `START_SERVICES.bat` - Batch startup script
- ✅ `LOCAL_SETUP.md` - Detailed local setup guide
- ✅ `PYTHON_SETUP.md` - Python services guide
- ✅ `SETUP_COMPLETE.md` - This file

---

## Support

For detailed guides, see:
- **LOCAL_SETUP.md** - Java services setup
- **PYTHON_SETUP.md** - Python services setup
- **README.md** - Project overview

