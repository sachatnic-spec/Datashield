# DataShield - Complete Local Docker Setup Guide

## What You Have Now

**All services running in Docker on localhost** (no remote infrastructure needed):
- ✅ PostgreSQL Database
- ✅ Redis Cache
- ✅ Kafka Message Broker
- ✅ Elasticsearch
- ✅ Kibana (Elasticsearch UI)
- ✅ Jaeger (Distributed Tracing)
- ✅ Prometheus (Metrics)
- ✅ Grafana (Dashboards)
- ✅ Zookeeper (Kafka dependency)

---

## Quick Start

### Step 1: Start Complete Docker Stack
```bash
# Run the batch file
START_DOCKER_STACK.bat

# OR manually
docker-compose -f docker-compose.local.yml up -d

# Verify all containers are running
docker-compose -f docker-compose.local.yml ps
```

**Expected Output:**
```
NAME                    STATUS
datasheild-postgres    Up (healthy)
datasheild-redis       Up (healthy)
datasheild-kafka       Up (healthy)
datasheild-elasticsearch Up (healthy)
datasheild-kibana      Up (healthy)
datasheild-jaeger      Up (healthy)
datasheild-prometheus  Up (healthy)
datasheild-grafana     Up (healthy)
datasheild-zookeeper   Up (healthy)
```

### Step 2: Build Java Services
```bash
cd d:\Development Practice\Datasheild
mvn clean install -DskipTests
# Takes 5-10 minutes on first run
```

### Step 3: Start Services (9 separate terminals)

**Terminal 1-5: Java Services**
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

**Terminal 6-9: Python Services**
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

## Service Access Points

### APIs (Interactive Documentation)
| Service | URL | Type |
|---------|-----|------|
| **Auth** | http://localhost:8001/swagger-ui.html | Java/Swagger |
| **Consent** | http://localhost:8002/swagger-ui.html | Java/Swagger |
| **Rights** | http://localhost:8003/swagger-ui.html | Java/Swagger |
| **Breach** | http://localhost:8004/swagger-ui.html | Java/Swagger |
| **Notification** | http://localhost:8005/swagger-ui.html | Java/Swagger |
| **AI Analysis** | http://localhost:8018/docs | Python/OpenAPI |
| **PII Detection** | http://localhost:8019/docs | Python/OpenAPI |
| **Risk Scoring** | http://localhost:8020/docs | Python/OpenAPI |
| **Anomaly Detection** | http://localhost:8021/docs | Python/OpenAPI |

### Databases & Infrastructure
| Service | Connection | Port | Type |
|---------|------------|------|------|
| **PostgreSQL** | localhost:5432 | 5432 | Database |
| **Redis** | localhost:6379 | 6379 | Cache |
| **Kafka** | localhost:9092 | 9092 | Message Broker |
| **Zookeeper** | localhost:2181 | 2181 | Kafka Coordinator |
| **Elasticsearch** | http://localhost:9200 | 9200 | Search Engine |

### Observability & Monitoring
| Tool | URL | Port | Purpose |
|------|-----|------|---------|
| **Jaeger** | http://localhost:16686 | 16686 | Distributed Tracing |
| **Prometheus** | http://localhost:9090 | 9090 | Metrics Collection |
| **Grafana** | http://localhost:3000 | 3000 | Dashboards (admin/admin) |
| **Kibana** | http://localhost:5601 | 5601 | Elasticsearch UI |

---

## Docker Compose Commands

### View Status
```bash
# Show all containers
docker-compose -f docker-compose.local.yml ps

# Show all services
docker-compose -f docker-compose.local.yml config --services

# Check health
docker-compose -f docker-compose.local.yml ps --all
```

### Manage Services
```bash
# Start all services
docker-compose -f docker-compose.local.yml up -d

# Stop all services
docker-compose -f docker-compose.local.yml down

# Stop and remove volumes (clean slate)
docker-compose -f docker-compose.local.yml down -v

# Restart all services
docker-compose -f docker-compose.local.yml restart

# Restart specific service
docker-compose -f docker-compose.local.yml restart postgres
```

### View Logs
```bash
# View all logs
docker-compose -f docker-compose.local.yml logs -f

# View specific service logs
docker-compose -f docker-compose.local.yml logs -f postgres
docker-compose -f docker-compose.local.yml logs -f kafka
docker-compose -f docker-compose.local.yml logs -f elasticsearch

# View last 50 lines
docker-compose -f docker-compose.local.yml logs --tail=50

# View logs with timestamps
docker-compose -f docker-compose.local.yml logs -f --timestamps
```

### Execute Commands in Container
```bash
# PostgreSQL
docker exec datasheild-postgres psql -U datasheild -d datasheild -c "SELECT version();"

# Redis
docker exec datasheild-redis redis-cli ping

# Kafka - List topics
docker exec datasheild-kafka kafka-topics --list --bootstrap-server localhost:9092

# Elasticsearch - Check health
docker exec datasheild-elasticsearch curl -s http://localhost:9200/_cluster/health | jq '.'
```

---

## Database Connections

### PostgreSQL
```
Host: localhost
Port: 5432
Username: datasheild
Password: datasheild_dev_pwd
Database: datasheild
```

**Connect via psql:**
```bash
psql -h localhost -U datasheild -d datasheild
# Password: datasheild_dev_pwd
```

**Connect via DBeaver/DataGrip:**
```
Server: localhost
Port: 5432
Database: datasheild
Username: datasheild
Password: datasheild_dev_pwd
```

### Redis
```
Host: localhost
Port: 6379
Database: 0 (default)
No password
```

**Test connection:**
```bash
docker exec datasheild-redis redis-cli ping
# Response: PONG
```

### Kafka
```
Bootstrap Servers: localhost:9092
Zookeeper: localhost:2181
```

**List topics:**
```bash
docker exec datasheild-kafka kafka-topics --list --bootstrap-server localhost:9092
```

**Create test topic:**
```bash
docker exec datasheild-kafka kafka-topics \
  --create \
  --topic test-topic \
  --bootstrap-server localhost:9092 \
  --partitions 1 \
  --replication-factor 1
```

### Elasticsearch
```
Host: localhost
Port: 9200
Scheme: http
```

**Check cluster health:**
```bash
curl http://localhost:9200/_cluster/health | jq '.'
```

**List indices:**
```bash
curl http://localhost:9200/_cat/indices?v
```

---

## Configuration

### Updated .env.local
All services now connect to localhost:
- PostgreSQL: postgres:5432
- Redis: redis:6379
- Kafka: kafka:9092
- Elasticsearch: elasticsearch:9200
- Jaeger: jaeger:14268
- Prometheus: prometheus:9090
- Grafana: grafana:3000

**Note:** When running in Docker, use the container names (postgres, redis, etc.) instead of localhost.

---

## Common Issues & Solutions

### Issue: "Docker daemon is not running"
```bash
# Start Docker Desktop (Windows/Mac) or Docker service (Linux)
# Windows: Start Docker Desktop application
# Linux: sudo systemctl start docker
```

### Issue: Port already in use
```bash
# Find process using port
netstat -ano | findstr :5432

# Kill process (replace PID with actual PID)
taskkill /PID <PID> /F

# OR change port in docker-compose.local.yml
```

### Issue: Container won't start
```bash
# Check logs
docker-compose -f docker-compose.local.yml logs postgres

# Rebuild container
docker-compose -f docker-compose.local.yml up -d --build postgres

# Remove and recreate
docker-compose -f docker-compose.local.yml rm postgres
docker-compose -f docker-compose.local.yml up -d postgres
```

### Issue: "No space left on device"
```bash
# Clean up Docker
docker system prune -a

# Remove unused volumes
docker volume prune

# Remove unused networks
docker network prune
```

### Issue: Kafka broker not responding
```bash
# Check Kafka logs
docker-compose -f docker-compose.local.yml logs kafka

# Verify Zookeeper is running first
docker-compose -f docker-compose.local.yml logs zookeeper

# Restart both
docker-compose -f docker-compose.local.yml restart zookeeper kafka
```

### Issue: Elasticsearch won't start
```bash
# Check logs
docker-compose -f docker-compose.local.yml logs elasticsearch

# Increase virtual memory (Linux only)
sudo sysctl -w vm.max_map_count=262144

# For permanent change, add to /etc/sysctl.conf
vm.max_map_count=262144
```

---

## Monitoring & Debugging

### Using Jaeger for Tracing
1. Open http://localhost:16686
2. Select service from dropdown
3. View distributed traces
4. Click on traces to see details

### Using Grafana for Metrics
1. Open http://localhost:3000
2. Login: admin/admin
3. Add Prometheus datasource: http://prometheus:9090
4. Create dashboards

### Using Kibana for Logs
1. Open http://localhost:5601
2. Configure index pattern for logs
3. Explore logs in Kibana UI

### Using Prometheus Queries
1. Open http://localhost:9090
2. Use query editor to run PromQL queries
3. Examples:
   ```
   up{job="prometheus"}
   rate(http_requests_total[5m])
   process_resident_memory_bytes
   ```

---

## Troubleshooting Health Checks

### Check All Health Endpoints
```bash
# PostgreSQL
docker exec datasheild-postgres pg_isready -U datasheild

# Redis
docker exec datasheild-redis redis-cli ping

# Kafka
docker exec datasheild-kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# Elasticsearch
curl -s http://localhost:9200/_cluster/health | jq '.status'

# Jaeger
curl -s http://localhost:16686/health | jq '.'

# Prometheus
curl -s http://localhost:9090/-/healthy

# Grafana
curl -s http://localhost:3000/api/health | jq '.database'
```

---

## Performance Tuning

### PostgreSQL
```sql
-- Increase connections
-- Edit docker-compose.local.yml: add max_connections parameter
```

### Elasticsearch
```bash
# Increase memory (already set to 512m)
# Change in docker-compose.local.yml ES_JAVA_OPTS

# Monitor heap usage
curl http://localhost:9200/_nodes/stats/jvm | jq '.nodes[].jvm.mem'
```

### Kafka
```bash
# Monitor broker metrics
docker exec datasheild-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --list

# Check broker health
docker exec datasheild-kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092
```

---

## Backup & Recovery

### Backup PostgreSQL
```bash
docker exec datasheild-postgres pg_dump -U datasheild datasheild > backup.sql
```

### Restore PostgreSQL
```bash
docker exec -i datasheild-postgres psql -U datasheild datasheild < backup.sql
```

### Backup Elasticsearch
```bash
# Create snapshot repository first
curl -X PUT http://localhost:9200/_snapshot/my_backup -H 'Content-Type: application/json' \
  -d '{"type": "fs", "settings": {"location": "/mount/backups"}}'

# Create snapshot
curl -X PUT http://localhost:9200/_snapshot/my_backup/snapshot_1
```

---

## Next Steps

1. ✅ Run `START_DOCKER_STACK.bat`
2. ✅ Verify all containers are healthy (`docker-compose ps`)
3. ✅ Build Maven modules (`mvn clean install`)
4. ✅ Start all services (9 terminals)
5. 🔄 Access services via Swagger UI
6. 🔄 Monitor with Jaeger & Grafana
7. 🔄 Check logs with Kibana

---

## Documentation

- **START_HERE.md** - Quick visual overview
- **QUICK_START.md** - Command reference
- **LOCAL_SETUP.md** - Java setup details
- **PYTHON_SETUP.md** - Python setup details
- **COMPLETE_DOCKER_SETUP.md** - This file

---

**Version:** 2.0 - Complete Local Docker Stack  
**Updated:** June 24, 2026  
**Status:** ✅ Ready to Deploy

