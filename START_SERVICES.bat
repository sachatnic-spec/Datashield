@echo off
REM DataShield Local Development - Simple Batch Startup

setlocal enabledelayedexpansion

cd /d "d:\Development Practice\Datasheild"

echo.
echo ========================================
echo DataShield - Local Development Startup
echo ========================================
echo.

REM Test if Docker is available
where docker >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [1] Starting PostgreSQL and Docker services...
    docker-compose -f docker-compose.local.yml up -d
    
    echo.
    echo [2] Waiting 15 seconds for PostgreSQL to be healthy...
    timeout /t 15 /nobreak
    
    echo.
    echo Docker containers running:
    docker ps --format "table {{.Names}}\t{{.Status}}"
) else (
    echo [WARNING] Docker not found in PATH
    echo Please ensure Docker Desktop is running manually
)

echo.
echo ========================================
echo Services Configuration
echo ========================================
echo.
echo LOCAL (Docker):
echo   - PostgreSQL: localhost:5432
echo   - Jaeger:     http://localhost:16686
echo   - Prometheus: http://localhost:9090
echo   - Grafana:    http://localhost:3000
echo.
echo REMOTE (10.197.214.105):
echo   - Redis:         10.197.214.105:6379
echo   - Kafka:         10.197.214.105:9092
echo   - Elasticsearch: 10.197.214.105:9200
echo.

echo ========================================
echo JAVA Services - Run in separate terminals
echo ========================================
echo.
echo 1. Auth Service (8001)
echo    cd services\auth-service ^& mvn spring-boot:run
echo.
echo 2. Consent Service (8002)
echo    cd services\consent-service ^& mvn spring-boot:run
echo.
echo 3. Rights Service (8003)
echo    cd services\rights-service ^& mvn spring-boot:run
echo.
echo 4. Breach Service (8004)
echo    cd services\breach-service ^& mvn spring-boot:run
echo.
echo 5. Notification Service (8005)
echo    cd services\notification-service ^& mvn spring-boot:run
echo.

echo ========================================
echo PYTHON Services - Run in separate terminals
echo ========================================
echo.
echo 6. AI Analysis Service (8018)
echo    cd services\ai-analysis
echo    python -m pip install -r requirements.txt
echo    uvicorn app.main:app --host 0.0.0.0 --port 8018 --reload
echo.
echo 7. PII Detection Service (8019)
echo    cd services\pii-detection
echo    python -m pip install -r requirements.txt
echo    uvicorn app.main:app --host 0.0.0.0 --port 8019 --reload
echo.
echo 8. Risk Scoring Service (8020)
echo    cd services\risk-scoring
echo    python -m pip install -r requirements.txt
echo    uvicorn app.main:app --host 0.0.0.0 --port 8020 --reload
echo.
echo 9. Anomaly Detection Service (8021)
echo    cd services\anomaly-detection
echo    python -m pip install -r requirements.txt
echo    uvicorn app.main:app --host 0.0.0.0 --port 8021 --reload
echo.

echo ========================================
echo FastAPI Interactive Docs
echo ========================================
echo.
echo   AI Analysis:      http://localhost:8018/docs
echo   PII Detection:    http://localhost:8019/docs
echo   Risk Scoring:     http://localhost:8020/docs
echo   Anomaly Detection: http://localhost:8021/docs
echo.

pause
