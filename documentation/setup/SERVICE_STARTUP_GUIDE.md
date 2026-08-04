# Pre-Launch System Status & Service Startup Guide

## ✅ Current System Status

### Infrastructure Services (All Running)
```
✓ PostgreSQL (localhost:5432) - Database
✓ Redis (localhost:6379) - Cache
✓ Kafka (localhost:9092) - Event streaming  
✓ Zookeeper - Kafka coordinator
✓ Elasticsearch - Log aggregation
✓ Kibana - Log visualization
✓ Prometheus - Metrics
✓ Grafana - Dashboards
```

### Frontend Services (All Running)
```
✓ compliance-dashboard (http://localhost:4200) - Angular
✓ data-principal-portal (http://localhost:4201) - Angular
✓ consent-widget (dev mode) - TypeScript/Rollup
```

### Database Schema (Complete)
```
✓ 9 Schemas Created
✓ 14 Tables Created
✓ 30+ Performance Indexes
✓ Event Outbox Tables Ready (breach, consent, rights)
✓ Demo Tenant Provisioned (ID: 42f5caf7-5d7f-47f9-b2bc-8881e4c6118c)
```

### Configuration Fixes Applied
```
✓ Kafka bootstrap servers updated (all 5 services)
✓ Duplicate YAML keys removed (tenant-service)
✓ Retries reduced for dev mode (3 → 0)
✓ Connection timeout parameters added
✓ Environment variable support enabled
```

---

## 🚀 Starting Backend Services

### Option 1: IntelliJ IDEA (Recommended for Development)

1. **Open Each Service in IntelliJ**:
   - Right-click service folder → Open Module
   - Or: File → Project Structure → Modules → Add

2. **Run Configuration**:
   - Click Run → Edit Configurations
   - Add Spring Boot configuration for each service
   - VM options: `-Dspring.output.ansi.enabled=always`

3. **Start Services** (in order):
   ```
   1. auth-service (port 8001)
   2. tenant-service (port 8007 or 8002)
   3. breach-service (port 8004)
   4. consent-service (port 8003)
   5. rights-service (port 8006)
   6. notification-service (port 8005)
   7. audit-service (port 8008)
   ```

### Option 2: PowerShell Script (Batch Startup)

```powershell
# From project root
.\start-backend-services.ps1
```

This will:
- Build all 7 services
- Start each in a separate PowerShell window
- Log output to `$env:TEMP\datasheild-services\`
- Perform health checks after 30 seconds

### Option 3: Manual Maven Build & Run

```bash
# Auth Service
cd services/auth-service
mvn clean install -DskipTests
java -jar target/auth-service-*.jar

# In another terminal - Tenant Service
cd services/tenant-service
mvn clean install -DskipTests
java -jar target/tenant-service-*.jar

# Continue for remaining services...
```

---

## 📋 Service Port Mapping

| Service | Port | Health Check |
|---------|------|--------------|
| auth-service | 8001 | http://localhost:8001/actuator/health |
| tenant-service | 8007 | http://localhost:8007/actuator/health |
| breach-service | 8004 | http://localhost:8004/actuator/health |
| consent-service | 8003 | http://localhost:8003/actuator/health |
| rights-service | 8006 | http://localhost:8006/actuator/health |
| notification-service | 8005 | http://localhost:8005/actuator/health |
| audit-service | 8008 | http://localhost:8008/actuator/health |

---

## 🔍 Verifying Services Are Running

### Quick Health Check Script
```powershell
$ports = 8001, 8002, 8003, 8004, 8005, 8006, 8007, 8008
foreach ($port in $ports) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$port/actuator/health" `
          -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ Port $port: HEALTHY" -ForegroundColor Green
        }
    } catch {
        Write-Host "✗ Port $port: NOT RESPONDING" -ForegroundColor Red
    }
}
```

### Check Logs
```bash
# Windows
cat $env:TEMP\datasheild-services\auth-service.log | tail -50

# Linux/Mac
tail -50 /tmp/datasheild-services/auth-service.log
```

### Database Connectivity
```bash
# Connect to PostgreSQL
psql -h localhost -U datasheild -d datasheild

# Verify tables
SELECT schema_name, COUNT(*) FROM information_schema.tables 
WHERE table_schema NOT IN ('pg_catalog', 'information_schema')
GROUP BY schema_name ORDER BY schema_name;
```

---

## 🐛 Troubleshooting

### Kafka Connection Errors
**Error**: `UnknownHostException: kafka`  
**Fix**: Kafka is optional in dev mode. Services will operate without event publishing.  
**Status**: ✅ Already fixed (environment variable support added)

### Database Connection Errors
**Error**: `FATAL: database "datasheild" does not exist`  
**Fix**: Ensure PostgreSQL container is running:
```powershell
docker ps | Select-String postgres
```

### YAML Parse Errors
**Error**: `DuplicateKeyException: found duplicate key`  
**Fix**: Check application.yml for duplicate keys  
**Status**: ✅ Already fixed (tenant-service corrected)

### Port Already in Use
**Error**: `Address already in use: PORT`  
**Fix**: Kill existing process or change port:
```powershell
# Find process using port
netstat -ano | findstr :8001

# Kill by PID
taskkill /PID <PID> /F
```

---

## 📝 Configuration Details

### Database Connection
- **Host**: localhost:5432 (or via Docker)
- **Database**: datasheild
- **Username**: datasheild
- **Password**: datasheild_dev_pwd
- **Schemas**: tenant, auth, breach, consent, rights, audit, policy, notification, config

### Kafka Configuration
- **Bootstrap Server**: localhost:9092 (configurable via `KAFKA_BOOTSTRAP_SERVERS` env var)
- **Fallback**: localhost:9092 if environment variable not set
- **Optional**: Services can operate without Kafka in dev mode

### Redis Configuration
- **Host**: localhost:6379
- **Password**: datasheild_dev_pwd
- **Purpose**: Caching, session storage, rate limiting

---

## ✅ Pre-Launch Checklist

- [x] Database initialized with all required tables
- [x] Event outbox tables created (breach, consent, rights)
- [x] Multi-tenant schema ready
- [x] Kafka configuration fixed (all services)
- [x] YAML syntax errors resolved
- [x] Environment variable support enabled
- [x] PostgreSQL running and healthy
- [x] Redis running and healthy
- [x] Frontend services running
- [x] Demo tenant provisioned

**Status**: 🟢 Ready for Backend Deployment

---

## 🎯 Next Steps

1. **Start Backend Services** (use one of the 3 options above)
2. **Verify Health Checks** (all services should return HTTP 200)
3. **Check Logs** (for any warning messages)
4. **Test API Connectivity** (use Swagger UI at `/swagger-ui.html` on each port)
5. **Monitor Metrics** (Prometheus at http://localhost:9090, Grafana at http://localhost:3000)
6. **End-to-End Testing** (frontend → backend API calls)

---

## 📚 Additional Resources

- **API Docs**: http://localhost:PORT/swagger-ui.html
- **Metrics**: http://localhost:9090 (Prometheus)
- **Dashboards**: http://localhost:3000 (Grafana)
- **Logs**: http://localhost:5601 (Kibana)
- **Traces**: http://localhost:16686 (Jaeger)

---

Last Updated: 2026-07-02  
Status: ✅ All systems ready for deployment
