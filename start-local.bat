@echo off
REM DataShield Local Development Startup Script

cd /d "d:\Development Practice\Datasheild"

echo.
echo ========================================
echo DataShield - Local Development Startup
echo ========================================
echo.

REM Check if Docker is available
where docker >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] Docker not found in PATH. Skipping container startup.
    echo Please ensure Docker Desktop is running before starting Java/Python services.
) else (
    echo [1/6] Starting PostgreSQL and observability stack...
    docker-compose -f docker-compose.local.yml up -d
    
    echo [2/6] Waiting for PostgreSQL health check (15 seconds)...
    timeout /t 15 /nobreak
    
    docker ps --format "table {{.Names}}\t{{.Status}}"
)

echo.
echo [3/6] Building Maven modules (Java services)...
echo This may take 5-10 minutes on first run...
call mvn clean install -DskipTests -q

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven build failed!
    exit /b 1
)

echo [3/6] ✓ Maven build successful

echo.
echo ========================================
echo Services Configuration
echo ========================================
echo.
echo LOCAL SERVICES (via Docker):
echo   - PostgreSQL: localhost:5432
echo   - Jaeger:     http://localhost:16686
echo   - Prometheus: http://localhost:9090
echo   - Grafana:    http://localhost:3000 (admin/admin)
echo.
echo REMOTE SERVICES (10.197.214.105):
echo   - Redis:         10.197.214.105:6379
echo   - Kafka:         10.197.214.105:9092
echo   - Elasticsearch: 10.197.214.105:9200
echo.

echo ========================================
echo Starting Services
echo ========================================
echo.
echo [INFO] Open separate terminals for each service:
echo.
echo JAVA Services (Maven):
echo   1. Auth Service (8001)
echo      cd services\auth-service ^& mvn spring-boot:run
echo.
echo   2. Consent Service (8002)
echo      cd services\consent-service ^& mvn spring-boot:run
echo.
echo   3. Rights Service (8003)
echo      cd services\rights-service ^& mvn spring-boot:run
echo.
echo   4. Breach Service (8004)
echo      cd services\breach-service ^& mvn spring-boot:run
echo.
echo   5. Notification Service (8005)
echo      cd services\notification-service ^& mvn spring-boot:run
echo.
echo Python Services (FastAPI + Uvicorn):
echo   6. AI Analysis Service (8018)
echo      cd services\ai-analysis ^& python -m pip install -r requirements.txt ^& uvicorn app.main:app --host 0.0.0.0 --port 8018 --reload
echo.
echo   7. PII Detection Service (8019)
echo      cd services\pii-detection ^& python -m pip install -r requirements.txt ^& uvicorn app.main:app --host 0.0.0.0 --port 8019 --reload
echo.
echo   8. Risk Scoring Service (8020)
echo      cd services\risk-scoring ^& python -m pip install -r requirements.txt ^& uvicorn app.main:app --host 0.0.0.0 --port 8020 --reload
echo.
echo   9. Anomaly Detection Service (8021)
echo      cd services\anomaly-detection ^& python -m pip install -r requirements.txt ^& uvicorn app.main:app --host 0.0.0.0 --port 8021 --reload
echo.
echo ========================================
echo Setup Complete! Ready to Start Services
echo ========================================
echo.
pause
