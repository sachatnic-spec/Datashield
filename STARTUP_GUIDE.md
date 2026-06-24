# 🚀 DataShield - Complete Service Startup Guide

## PHASE 1: Start Docker Infrastructure (30 seconds)

Run this command in PowerShell or Command Prompt:

```bash
docker-compose -f docker-compose.local.yml up -d
```

**Expected Output:**
```
Creating datasheild-postgres ...
Creating datasheild-redis ...
Creating datasheild-zookeeper ...
Creating datasheild-kafka ...
Creating datasheild-elasticsearch ...
Creating datasheild-kibana ...
Creating datasheild-jaeger ...
Creating datasheild-prometheus ...
Creating datasheild-grafana ...
```

**Verify:**
```bash
docker-compose -f docker-compose.local.yml ps
```

All should show `Up (healthy)` or `Up`

---

## PHASE 2: Build Maven Modules (5-10 minutes)

Run in one terminal:

```bash
cd "d:\Development Practice\Datasheild"
mvn clean install -DskipTests
```

**Expected Output:**
```
[INFO] Building DataShield Parent POM
[INFO] ─────────────────────────────────
[INFO] Building auth-service ...
[INFO] Building consent-service ...
[INFO] Building rights-service ...
[INFO] Building breach-service ...
[INFO] Building notification-service ...
[INFO] ────────────────────────────────
[INFO] BUILD SUCCESS
```

---

## PHASE 3: Start 9 Microservices (Open 9 NEW Terminal Windows)

### Option A: Quick Copy-Paste Method

**Terminal 1 - Auth Service (Port 8001)**
```bash
cd "d:\Development Practice\Datasheild\services\auth-service"
mvn spring-boot:run
```

**Terminal 2 - Consent Service (Port 8002)**
```bash
cd "d:\Development Practice\Datasheild\services\consent-service"
mvn spring-boot:run
```

**Terminal 3 - Rights Service (Port 8003)**
```bash
cd "d:\Development Practice\Datasheild\services\rights-service"
mvn spring-boot:run
```

**Terminal 4 - Breach Service (Port 8004)**
```bash
cd "d:\Development Practice\Datasheild\services\breach-service"
mvn spring-boot:run
```

**Terminal 5 - Notification Service (Port 8005)**
```bash
cd "d:\Development Practice\Datasheild\services\notification-service"
mvn spring-boot:run
```

**Terminal 6 - AI Analysis Service (Port 8018)**
```bash
cd "d:\Development Practice\Datasheild\services\ai-analysis"
python -m pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8018 --reload
```

**Terminal 7 - PII Detection Service (Port 8019)**
```bash
cd "d:\Development Practice\Datasheild\services\pii-detection"
python -m pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8019 --reload
```

**Terminal 8 - Risk Scoring Service (Port 8020)**
```bash
cd "d:\Development Practice\Datasheild\services\risk-scoring"
python -m pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8020 --reload
```

**Terminal 9 - Anomaly Detection Service (Port 8021)**
```bash
cd "d:\Development Practice\Datasheild\services\anomaly-detection"
python -m pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8021 --reload
```

---

## PHASE 4: Verify All Services Running (5 minutes)

### Test Database
```bash
docker exec datasheild-postgres psql -U datasheild -d datasheild -c "SELECT version();"
```

### Test Cache
```bash
docker exec datasheild-redis redis-cli ping
```

### Test APIs
Open these in browser - all should return 200 OK:
- http://localhost:8001/health (Auth)
- http://localhost:8002/actuator/health (Consent)
- http://localhost:8003/actuator/health (Rights)
- http://localhost:8004/actuator/health (Breach)
- http://localhost:8005/actuator/health (Notification)
- http://localhost:8018/health (AI Analysis)
- http://localhost:8019/health (PII Detection)
- http://localhost:8020/health (Risk Scoring)
- http://localhost:8021/health (Anomaly Detection)

---

## PHASE 5: Access Your Platform

### API Documentation (Interactive)
- **Auth Service**: http://localhost:8001/swagger-ui.html
- **AI Analysis**: http://localhost:8018/docs

### Monitoring
- **Jaeger** (Tracing): http://localhost:16686
- **Prometheus** (Metrics): http://localhost:9090
- **Grafana** (Dashboards): http://localhost:3000 (admin/admin)
- **Kibana** (Logs): http://localhost:5601

---

## ✅ Success Indicators

You'll know everything is working when:

1. ✅ All 9 Docker containers show as `Up`
2. ✅ Maven build completes with `BUILD SUCCESS`
3. ✅ All 9 services show startup logs
4. ✅ You can access http://localhost:8001/swagger-ui.html
5. ✅ You can see traces in http://localhost:16686
6. ✅ You can see metrics in http://localhost:9090

---

## 🛑 If Something Goes Wrong

### Docker containers won't start
```bash
# Check logs
docker-compose -f docker-compose.local.yml logs -f

# Restart
docker-compose -f docker-compose.local.yml restart
```

### Maven build fails
```bash
# Clean and rebuild
mvn clean install -DskipTests -X
```

### Service won't start
```bash
# Check specific logs
cd services/auth-service
mvn spring-boot:run -X
```

### Port already in use
```powershell
# Find process
Get-NetTCPConnection -LocalPort 8001

# Kill it
Stop-Process -Id <PID> -Force
```

---

## ⏱️ Expected Timeline

- Docker Stack: 30 seconds
- Maven Build: 5-10 minutes
- Service Startup: 2-3 minutes per service (9 in parallel)
- **Total: ~15-20 minutes**

---

## 📝 Service Startup Checklist

- [ ] Phase 1: Docker containers running
- [ ] Phase 2: Maven build successful
- [ ] Phase 3: All 9 services started
- [ ] Phase 4: All health checks passing
- [ ] Phase 5: Can access APIs

---

## 🎯 Next: Test an API

Once all services are running:

1. Open: http://localhost:8001/swagger-ui.html
2. Expand: Any endpoint (e.g., `/v1/auth/health`)
3. Click: "Try it out"
4. Click: "Execute"
5. See: Response 200 OK

---

## 💡 Pro Tips

1. **Keep all 9 terminals visible** - Makes it easy to see startup progress
2. **Check Docker logs first** - Most issues are Docker-related
3. **Use Jaeger for tracing** - See how requests flow through system
4. **Monitor Prometheus** - Watch resource usage
5. **Enable hot-reload** - Python services auto-refresh on file changes

---

**Ready? Follow these 5 phases and you'll have the complete platform running! 🚀**

