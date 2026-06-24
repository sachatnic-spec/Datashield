# DataShield Local Development Startup Script (PowerShell)

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "DataShield - Local Development Startup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Navigate to project root
Set-Location "d:\Development Practice\Datasheild"

# Step 1: Check Docker
Write-Host "[1/5] Checking Docker..." -ForegroundColor Yellow
$dockerExists = $null -ne (Get-Command docker -ErrorAction SilentlyContinue)
if ($dockerExists) {
    Write-Host "[OK] Docker found. Starting PostgreSQL stack..." -ForegroundColor Green
    
    # Start Docker Compose
    docker-compose -f docker-compose.local.yml up -d 2>$null
    
    Write-Host "[WAIT] Waiting for PostgreSQL to be healthy (15 seconds)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 15
    
    Write-Host ""
    Write-Host "Running Docker containers:" -ForegroundColor Cyan
    docker ps --format "table {{.Names}}`t{{.Status}}" | Select-Object -Skip 1 | ForEach-Object { Write-Host "  $_" }
    Write-Host ""
} else {
    Write-Host "[WARNING] Docker not found. Please start Docker Desktop manually." -ForegroundColor Yellow
}

# Step 2: Check Java & Maven
Write-Host "[2/5] Checking Java and Maven..." -ForegroundColor Yellow
$javaExists = $null -ne (Get-Command java -ErrorAction SilentlyContinue)
$mvnExists = $null -ne (Get-Command mvn -ErrorAction SilentlyContinue)

if ($javaExists -and $mvnExists) {
    Write-Host "[OK] Java and Maven found" -ForegroundColor Green
} else {
    Write-Host "[WARNING] Java or Maven not found in PATH" -ForegroundColor Yellow
    if (-not $javaExists) { Write-Host "  - Java not found" }
    if (-not $mvnExists) { Write-Host "  - Maven not found" }
}

# Step 3: Check Python
Write-Host ""
Write-Host "[3/5] Checking Python..." -ForegroundColor Yellow
$pythonExists = $null -ne (Get-Command python -ErrorAction SilentlyContinue)
if ($pythonExists) {
    $pythonVersion = python --version 2>&1
    Write-Host "[OK] $pythonVersion found" -ForegroundColor Green
} else {
    Write-Host "[WARNING] Python not found. Required for AI/ML services." -ForegroundColor Yellow
}

# Step 4: Build Maven modules
if ($mvnExists) {
    Write-Host ""
    Write-Host "[4/5] Building Maven modules (Java services)..." -ForegroundColor Yellow
    Write-Host "[WAIT] This may take 5-10 minutes on first run..." -ForegroundColor Yellow
    
    mvn clean install -DskipTests -q
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Maven build successful" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Maven build failed" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[4/5] [SKIP] Skipping Maven build (Maven not found)" -ForegroundColor Gray
}

# Step 5: Display configuration and next steps
Write-Host ""
Write-Host "[5/5] Configuration Summary" -ForegroundColor Yellow
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Infrastructure" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "LOCAL (Docker):" -ForegroundColor Green
Write-Host "  • PostgreSQL:  localhost:5432" 
Write-Host "  • Jaeger UI:   http://localhost:16686"
Write-Host "  • Prometheus:  http://localhost:9090"
Write-Host "  • Grafana:     http://localhost:3000 (admin/admin)"
Write-Host ""
Write-Host "REMOTE (10.197.214.105):" -ForegroundColor Green
Write-Host "  • Redis:         10.197.214.105:6379"
Write-Host "  • Kafka:         10.197.214.105:9092"
Write-Host "  • Elasticsearch: 10.197.214.105:9200"
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "JAVA Services (Maven - 5 services)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Open separate PowerShell terminals and run:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Auth Service (Port 8001)" -ForegroundColor Magenta
Write-Host "   cd services\auth-service; mvn spring-boot:run" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Consent Service (Port 8002)" -ForegroundColor Magenta
Write-Host "   cd services\consent-service; mvn spring-boot:run" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Rights Service (Port 8003)" -ForegroundColor Magenta
Write-Host "   cd services\rights-service; mvn spring-boot:run" -ForegroundColor Gray
Write-Host ""
Write-Host "4. Breach Service (Port 8004)" -ForegroundColor Magenta
Write-Host "   cd services\breach-service; mvn spring-boot:run" -ForegroundColor Gray
Write-Host ""
Write-Host "5. Notification Service (Port 8005)" -ForegroundColor Magenta
Write-Host "   cd services\notification-service; mvn spring-boot:run" -ForegroundColor Gray
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PYTHON Services (FastAPI - 4 services)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Open separate PowerShell terminals and run:" -ForegroundColor Yellow
Write-Host ""
Write-Host "6. AI Analysis Service (Port 8018)" -ForegroundColor Magenta
Write-Host '   cd services\ai-analysis; python -m pip install -r requirements.txt; uvicorn app.main:app --host 0.0.0.0 --port 8018 --reload' -ForegroundColor Gray
Write-Host ""
Write-Host "7. PII Detection Service (Port 8019)" -ForegroundColor Magenta
Write-Host '   cd services\pii-detection; python -m pip install -r requirements.txt; uvicorn app.main:app --host 0.0.0.0 --port 8019 --reload' -ForegroundColor Gray
Write-Host ""
Write-Host "8. Risk Scoring Service (Port 8020)" -ForegroundColor Magenta
Write-Host '   cd services\risk-scoring; python -m pip install -r requirements.txt; uvicorn app.main:app --host 0.0.0.0 --port 8020 --reload' -ForegroundColor Gray
Write-Host ""
Write-Host "9. Anomaly Detection Service (Port 8021)" -ForegroundColor Magenta
Write-Host '   cd services\anomaly-detection; python -m pip install -r requirements.txt; uvicorn app.main:app --host 0.0.0.0 --port 8021 --reload' -ForegroundColor Gray
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "API Documentation (FastAPI Services)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  • AI Analysis:      http://localhost:8018/docs" -ForegroundColor Cyan
Write-Host "  • PII Detection:    http://localhost:8019/docs" -ForegroundColor Cyan
Write-Host "  • Risk Scoring:     http://localhost:8020/docs" -ForegroundColor Cyan
Write-Host "  • Anomaly Detection: http://localhost:8021/docs" -ForegroundColor Cyan
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "[COMPLETE] Setup Complete! Ready to Start Services" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next Step:" -ForegroundColor Yellow
Write-Host "  1. Open 9 new PowerShell/Terminal windows" -ForegroundColor Yellow
Write-Host "  2. Run the commands above (one per terminal)" -ForegroundColor Yellow
Write-Host "  3. Access services at URLs above" -ForegroundColor Yellow
Write-Host ""
Read-Host "Press Enter to exit"
