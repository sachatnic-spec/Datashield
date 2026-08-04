# DataShield Python Services Setup Guide

## Python Services Overview

You have 4 AI/ML Python services using **FastAPI** + **Uvicorn**:

| Service | Port | Purpose | Dependencies |
|---------|------|---------|--------------|
| **AI Analysis** | 8018 | ML model analysis & insights | scikit-learn, pandas, nltk, spacy |
| **PII Detection** | 8019 | PII/sensitive data detection | spacy, regex patterns, ML models |
| **Risk Scoring** | 8020 | Risk assessment scoring | scikit-learn, numpy, ML models |
| **Anomaly Detection** | 8021 | Anomaly & outlier detection | scikit-learn, pandas, isolation forest |

All services connect to:
- **PostgreSQL** (local): `localhost:5432`
- **Kafka** (remote): `10.197.214.105:9092` (for event streaming)

---

## Prerequisites

### Ensure Python 3.11+ is installed:
```powershell
python --version
# Should show Python 3.11.x or higher
```

If not, download from [python.org](https://www.python.org/downloads/)

### Create Virtual Environment (Recommended)

For each Python service, create a virtual environment:

```powershell
# Navigate to service directory
cd services\ai-analysis

# Create virtual environment
python -m venv venv

# Activate it
# On Windows:
.\venv\Scripts\Activate.ps1

# On macOS/Linux:
source venv/bin/activate
```

---

## Quick Start for Python Services

### Option A: Run Individually (For Development)

**Terminal 1 - AI Analysis Service**
```powershell
cd services\ai-analysis
python -m pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8018 --reload
```

**Terminal 2 - PII Detection Service**
```powershell
cd services\pii-detection
python -m pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8019 --reload
```

**Terminal 3 - Risk Scoring Service**
```powershell
cd services\risk-scoring
python -m pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8020 --reload
```

**Terminal 4 - Anomaly Detection Service**
```powershell
cd services\anomaly-detection
python -m pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8021 --reload
```

---

## Environment Configuration for Python Services

Create `.env` file in each service root:

```bash
# Database
DATABASE_URL=postgresql://datasheild:datasheild_dev_pwd@localhost:5432/datasheild

# Kafka (Remote)
KAFKA_BOOTSTRAP_SERVERS=10.197.214.105:9092

# Service
SERVICE_PORT=8018

# Redis (Optional caching)
REDIS_URL=redis://:@10.197.214.105:6379/0

# Elasticsearch (for logging)
ELASTICSEARCH_HOST=10.197.214.105
ELASTICSEARCH_PORT=9200

# Logging
LOG_LEVEL=INFO
```

---

## Project Structure

### AI Analysis Service
```
services/ai-analysis/
├── app/
│   ├── main.py           # FastAPI app entry point
│   ├── routes/           # API endpoints
│   ├── models/           # ML models & DB models
│   └── services/         # Business logic
├── models/               # Pre-trained ML models (*.pkl, *.h5)
├── utils/
│   ├── ml.py            # ML utilities
│   ├── kafka.py         # Kafka producer/consumer
│   └── database.py      # DB connection pool
├── tests/               # Unit & integration tests
└── requirements.txt
```

### API Endpoints (Example)
```
GET  /health                    # Health check
POST /analyze                   # Analyze data
GET  /models                    # List available models
POST /predict                   # Run prediction
```

---

## Testing Python Services

### Run Unit Tests
```powershell
cd services\ai-analysis
pytest tests/ -v
```

### Run with Coverage
```powershell
pytest tests/ --cov=app --cov-report=html
# Open htmlcov/index.html in browser
```

### Manual API Test
```powershell
# Test AI Analysis Service
$uri = "http://localhost:8018/health"
Invoke-WebRequest -Uri $uri

# Or using curl
curl http://localhost:8018/health
```

---

## Common Issues & Solutions

### Issue: `ModuleNotFoundError: No module named 'spacy'`
```powershell
# Download spacy language model
python -m spacy download en_core_web_sm
```

### Issue: `psycopg2-binary` installation fails
```powershell
# Install with build tools
pip install psycopg2-binary --no-binary psycopg2-binary
```

### Issue: Kafka connection refused
```powershell
# Verify remote Kafka is accessible
Test-NetConnection -ComputerName 10.197.214.105 -Port 9092
```

### Issue: Port already in use (e.g., 8018)
```powershell
# Find process using port 8018
Get-NetTCPConnection -LocalPort 8018

# Kill process by PID
Stop-Process -Id <PID> -Force
```

---

## Performance Tips

1. **Enable hot-reload (development):**
   ```bash
   uvicorn app.main:app --reload  # Watches for file changes
   ```

2. **Disable hot-reload (production):**
   ```bash
   uvicorn app.main:app --workers 4  # Use multiple workers
   ```

3. **Use async endpoints for better concurrency**

4. **Cache predictions with Redis** for repeated queries

---

## Dependency Management

### Update all requirements
```powershell
pip install --upgrade pip setuptools wheel
pip install -r requirements.txt --upgrade
pip freeze > requirements.txt  # Update lock file
```

### Audit for vulnerabilities
```powershell
pip install safety
safety check
```

---

## Access Points

| Service | URL | Docs |
|---------|-----|------|
| AI Analysis | http://localhost:8018 | http://localhost:8018/docs |
| PII Detection | http://localhost:8019 | http://localhost:8019/docs |
| Risk Scoring | http://localhost:8020 | http://localhost:8020/docs |
| Anomaly Detection | http://localhost:8021 | http://localhost:8021/docs |

Each FastAPI service auto-generates **Swagger UI** at `/docs` and **ReDoc** at `/redoc`.

---

## Next Steps

1. ✅ Install Python 3.11+
2. 🔄 Install requirements for each Python service
3. 🔄 Start Python services in separate terminals
4. 🔄 Access http://localhost:8018/docs (example)
5. Test endpoints via Swagger UI

