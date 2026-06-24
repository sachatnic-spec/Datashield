@echo off
REM DataShield - Automated Startup Script
REM This script starts Docker containers and shows instructions for starting services

setlocal enabledelayedexpansion

cd /d "d:\Development Practice\Datasheild"

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║    DataShield - Automated Docker Stack Startup               ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Check Docker
echo [1] Checking Docker...
docker --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker not found!
    echo Please install Docker Desktop and add it to PATH
    pause
    exit /b 1
)
echo [OK] Docker found
echo.

REM Start Docker Stack
echo [2] Starting Docker containers...
docker-compose -f docker-compose.local.yml pull --quiet
docker-compose -f docker-compose.local.yml up -d

echo.
echo [3] Waiting for services to be healthy (20 seconds)...
timeout /t 20 /nobreak

echo.
echo [4] Container Status:
docker-compose -f docker-compose.local.yml ps

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║            ✅ Docker Stack Running                            ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

echo ═══════════════════════════════════════════════════════════════════
echo PHASE 2: Build Maven Modules
echo ═══════════════════════════════════════════════════════════════════
echo.
echo Run this command:
echo   mvn clean install -DskipTests
echo.
echo (Takes 5-10 minutes)
echo.

echo ═══════════════════════════════════════════════════════════════════
echo PHASE 3: Start 9 Microservices (9 New Terminals)
echo ═══════════════════════════════════════════════════════════════════
echo.
echo Copy each command below into a separate terminal:
echo.
echo JAVA Services:
echo ───────────────
echo Terminal 1 - Auth Service (8001):
echo   cd services\auth-service ^& mvn spring-boot:run
echo.
echo Terminal 2 - Consent Service (8002):
echo   cd services\consent-service ^& mvn spring-boot:run
echo.
echo Terminal 3 - Rights Service (8003):
echo   cd services\rights-service ^& mvn spring-boot:run
echo.
echo Terminal 4 - Breach Service (8004):
echo   cd services\breach-service ^& mvn spring-boot:run
echo.
echo Terminal 5 - Notification Service (8005):
echo   cd services\notification-service ^& mvn spring-boot:run
echo.
echo PYTHON Services:
echo ─────────────────
echo Terminal 6 - AI Analysis (8018):
echo   cd services\ai-analysis ^& python -m pip install -r requirements.txt ^& uvicorn app.main:app --port 8018 --reload
echo.
echo Terminal 7 - PII Detection (8019):
echo   cd services\pii-detection ^& python -m pip install -r requirements.txt ^& uvicorn app.main:app --port 8019 --reload
echo.
echo Terminal 8 - Risk Scoring (8020):
echo   cd services\risk-scoring ^& python -m pip install -r requirements.txt ^& uvicorn app.main:app --port 8020 --reload
echo.
echo Terminal 9 - Anomaly Detection (8021):
echo   cd services\anomaly-detection ^& python -m pip install -r requirements.txt ^& uvicorn app.main:app --port 8021 --reload
echo.

echo ═══════════════════════════════════════════════════════════════════
echo PHASE 4: Access Your Services
echo ═══════════════════════════════════════════════════════════════════
echo.
echo Java APIs (Swagger):
echo   http://localhost:8001/swagger-ui.html  (Auth)
echo   http://localhost:8002/swagger-ui.html  (Consent)
echo   http://localhost:8003/swagger-ui.html  (Rights)
echo   http://localhost:8004/swagger-ui.html  (Breach)
echo   http://localhost:8005/swagger-ui.html  (Notification)
echo.
echo Python APIs (OpenAPI):
echo   http://localhost:8018/docs  (AI Analysis)
echo   http://localhost:8019/docs  (PII Detection)
echo   http://localhost:8020/docs  (Risk Scoring)
echo   http://localhost:8021/docs  (Anomaly Detection)
echo.
echo Monitoring:
echo   http://localhost:16686  (Jaeger - Tracing)
echo   http://localhost:9090   (Prometheus - Metrics)
echo   http://localhost:3000   (Grafana - Dashboards)
echo   http://localhost:5601   (Kibana - Logs)
echo.

echo ═══════════════════════════════════════════════════════════════════
echo NEXT STEPS:
echo ═══════════════════════════════════════════════════════════════════
echo.
echo 1. Open new terminal and run:
echo    mvn clean install -DskipTests
echo.
echo 2. Open 9 more terminals and run the commands above (one per terminal)
echo.
echo 3. Once all services are running, access:
echo    http://localhost:8001/swagger-ui.html
echo.
echo 4. Monitor with:
echo    http://localhost:16686 (Jaeger)
echo    http://localhost:3000  (Grafana)
echo.

pause
