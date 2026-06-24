# DataShield Local Development Setup Guide

## Environment Configuration

**Remote Infrastructure (10.197.214.105):**
- Redis: `:6379`
- Kafka: `:9092`
- Elasticsearch: `:9200`
- Kibana (optional): `:5601`

**Local Services:**
- PostgreSQL: `localhost:5432`
- Jaeger (tracing): `localhost:16686`
- Prometheus (metrics): `localhost:9090`
- Grafana (dashboards): `localhost:3000`

---

## Quick Start

### Step 1: Start Local PostgreSQL & Observability Stack
```bash
cd d:\Development Practice\Datasheild

# Start only PostgreSQL + Jaeger + Prometheus + Grafana
docker-compose -f docker-compose.local.yml up -d

# Verify PostgreSQL is healthy
docker exec datasheild-postgres pg_isready -U datasheild

# Wait ~10 seconds for full health checks
sleep 10
```

### Step 2: Build All Services
```bash
# Install dependencies and build all Maven modules
mvn clean install -DskipTests

# Or with tests
mvn clean install
```

### Step 3: Run Individual Services

Open separate terminal windows and run each service:

**Terminal 1 - Auth Service (Port 8001)**
```bash
cd services\auth-service
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.config.location=classpath:/,.env.local"
```

**Terminal 2 - Consent Service (Port 8002)**
```bash
cd services\consent-service
mvn spring-boot:run
```

**Terminal 3 - Rights Service (Port 8003)**
```bash
cd services\rights-service
mvn spring-boot:run
```

**Terminal 4 - Breach Service (Port 8004)**
```bash
cd services\breach-service
mvn spring-boot:run
```

**Terminal 5 - Notification Service (Port 8005)**
```bash
cd services\notification-service
mvn spring-boot:run
```

---

## Verify Connection to Remote Infrastructure

### Check Redis
```bash
# Using redis-cli (if installed locally)
redis-cli -h 10.197.214.105 ping
# Should respond: PONG

# Or via PowerShell
pwsh -c "echo 'PING' | nc -w 1 10.197.214.105 6379"
```

### Check Kafka
```bash
# List topics
docker run --rm confluentinc/cp-kafka:7.5.0 kafka-topics --bootstrap-server 10.197.214.105:9092 --list

# Or check broker
telnet 10.197.214.105 9092
```

### Check Elasticsearch
```bash
# Health check
curl http://10.197.214.105:9200/_cluster/health

# Or PowerShell
Invoke-WebRequest -Uri http://10.197.214.105:9200/_cluster/health
```

---

## Access Points

| Service | URL | Status Endpoint |
|---------|-----|-----------------|
| Auth | http://localhost:8001 | `/v1/auth/health` |
| Consent | http://localhost:8002 | `/actuator/health` |
| Rights | http://localhost:8003 | `/actuator/health` |
| Breach | http://localhost:8004 | `/actuator/health` |
| Notification | http://localhost:8005 | `/actuator/health` |
| Postgres | localhost:5432 | N/A |
| Jaeger | http://localhost:16686 | UI |
| Prometheus | http://localhost:9090 | UI |
| Grafana | http://localhost:3000 | UI (admin/admin) |

---

## Stopping Services

### Stop All Docker Containers
```bash
docker-compose -f docker-compose.local.yml down

# Or with volume cleanup
docker-compose -f docker-compose.local.yml down -v
```

### Stop Individual Services
Just terminate the Maven process in each terminal (Ctrl+C)

---

## Troubleshooting

### Connection Refused to Remote Infrastructure
```bash
# Test network connectivity to 10.197.214.105
ping 10.197.214.105
telnet 10.197.214.105 6379   # Test Redis port
telnet 10.197.214.105 9092   # Test Kafka port
telnet 10.197.214.105 9200   # Test Elasticsearch port
```

### PostgreSQL Connection Issues
```bash
# Check PostgreSQL logs
docker logs datasheild-postgres

# Reset PostgreSQL
docker-compose -f docker-compose.local.yml down -v
docker-compose -f docker-compose.local.yml up postgres -d
```

### Services Can't Find Remote Infra
- Verify firewall rules allow outbound to `10.197.214.105`
- Check if Redis/Kafka/ES require authentication on remote server
- Update `.env.local` with credentials if needed

### Build Errors
```bash
# Clear Maven cache
mvn clean

# Rebuild with debug
mvn clean install -X
```

---

## Next Steps

1. ✅ Docker Compose configured for local PostgreSQL
2. ✅ Environment variables set for remote infrastructure
3. 🔄 **Run Step 1:** Start Docker Compose
4. 🔄 **Run Step 2:** Build Maven modules
5. 🔄 **Run Step 3:** Start individual services
6. Test API endpoints

