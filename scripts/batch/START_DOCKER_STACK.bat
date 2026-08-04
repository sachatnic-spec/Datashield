@echo off
REM DataShield - Full Local Docker Stack Startup

setlocal enabledelayedexpansion

cd /d "d:\Development Practice\Datasheild"

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║    DataShield - Complete Local Docker Stack Startup           ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Check if Docker is available
where docker >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker not found in PATH!
    echo Please ensure Docker Desktop is installed and running.
    pause
    exit /b 1
)

echo [1] Checking Docker status...
docker ps >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker daemon is not running!
    echo Please start Docker Desktop and try again.
    pause
    exit /b 1
)
echo [OK] Docker is running

echo.
echo [2] Pulling latest images...
docker-compose -f docker-compose.local.yml pull

echo.
echo [3] Starting all services...
docker-compose -f docker-compose.local.yml up -d

echo.
echo [4] Waiting for services to be healthy (30 seconds)...
timeout /t 5 /nobreak

echo.
echo [5] Checking service status...
docker-compose -f docker-compose.local.yml ps

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                  All Services Configuration                    ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo LOCAL DOCKER SERVICES (All on localhost):
echo ─────────────────────────────────────────
echo.
echo Database:
echo   PostgreSQL      → localhost:5432 (datasheild/datasheild_dev_pwd)
echo.
echo Cache & Messaging:
echo   Redis           → localhost:6379
echo   Kafka           → localhost:9092
echo   Zookeeper       → localhost:2181
echo.
echo Search & Analytics:
echo   Elasticsearch   → http://localhost:9200
echo   Kibana          → http://localhost:5601
echo.
echo Observability:
echo   Jaeger          → http://localhost:16686 (Tracing)
echo   Prometheus      → http://localhost:9090 (Metrics)
echo   Grafana         → http://localhost:3000 (Dashboards - admin/admin)
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                       NEXT STEPS                               ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo 1. VERIFY ALL SERVICES:
echo    docker-compose -f docker-compose.local.yml logs -f
echo.
echo 2. BUILD MAVEN MODULES:
echo    mvn clean install -DskipTests
echo.
echo 3. START JAVA SERVICES (9 separate terminals):
echo    Terminal 1: cd services\auth-service ^& mvn spring-boot:run
echo    Terminal 2: cd services\consent-service ^& mvn spring-boot:run
echo    Terminal 3: cd services\rights-service ^& mvn spring-boot:run
echo    Terminal 4: cd services\breach-service ^& mvn spring-boot:run
echo    Terminal 5: cd services\notification-service ^& mvn spring-boot:run
echo.
echo 4. START PYTHON SERVICES (4 separate terminals):
echo    Terminal 6: cd services\ai-analysis
echo                python -m pip install -r requirements.txt
echo                uvicorn app.main:app --port 8018 --reload
echo.
echo    Terminal 7: cd services\pii-detection
echo                python -m pip install -r requirements.txt
echo                uvicorn app.main:app --port 8019 --reload
echo.
echo    Terminal 8: cd services\risk-scoring
echo                python -m pip install -r requirements.txt
echo                uvicorn app.main:app --port 8020 --reload
echo.
echo    Terminal 9: cd services\anomaly-detection
echo                python -m pip install -r requirements.txt
echo                uvicorn app.main:app --port 8021 --reload
echo.
echo 5. ACCESS YOUR SERVICES:
echo    Java APIs:      http://localhost:8001-8005/swagger-ui.html
echo    Python APIs:    http://localhost:8018-8021/docs
echo    Jaeger:         http://localhost:16686
echo    Grafana:        http://localhost:3000
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                    USEFUL COMMANDS                             ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo # View logs
echo docker-compose -f docker-compose.local.yml logs -f
echo.
echo # Check specific service
echo docker-compose -f docker-compose.local.yml logs postgres
echo.
echo # Stop all containers
echo docker-compose -f docker-compose.local.yml down
echo.
echo # Stop and remove volumes (clean slate)
echo docker-compose -f docker-compose.local.yml down -v
echo.
echo # Restart all services
echo docker-compose -f docker-compose.local.yml restart
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                  Stack Ready to Go! You're All Set!            ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

pause
