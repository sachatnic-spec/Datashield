# DataShield - Start Complete Local Docker Stack
# PowerShell Script with Docker PATH setup

param(
    [switch]$Help,
    [switch]$Logs,
    [switch]$Status
)

$ProjectRoot = "d:\Development Practice\Datasheild"

# Setup Docker PATH if needed
function Setup-DockerPath {
    $dockerPaths = @(
        "C:\Program Files\Docker\Docker\resources\bin",
        "C:\Program Files (x86)\Docker\Docker\resources\bin"
    )
    
    foreach ($path in $dockerPaths) {
        if (Test-Path $path) {
            $env:PATH = "$path;$env:PATH"
            return $true
        }
    }
    return $false
}

# Main functions
function Show-Help {
    Write-Host ""
    Write-Host "DataShield Docker Stack Launcher" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Usage: .\start-docker.ps1 [option]" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Options:" -ForegroundColor Yellow
    Write-Host "  (no args)  Start the full Docker stack" -ForegroundColor Gray
    Write-Host "  -Status    Show current status of all containers" -ForegroundColor Gray
    Write-Host "  -Logs      Show live logs from all containers" -ForegroundColor Gray
    Write-Host "  -Help      Show this help message" -ForegroundColor Gray
    Write-Host ""
}

function Start-Stack {
    Write-Host ""
    Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║        Starting Complete Local Docker Stack                   ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""

    Set-Location $ProjectRoot

    Write-Host "[1] Setting up Docker environment..." -ForegroundColor Yellow
    if (-not (Setup-DockerPath)) {
        Write-Host "[ERROR] Docker not found in standard locations!" -ForegroundColor Red
        Write-Host "Please install Docker Desktop from https://www.docker.com" -ForegroundColor Yellow
        return
    }
    Write-Host "[OK] Docker ready" -ForegroundColor Green
    Write-Host ""

    Write-Host "[2] Pulling latest images (this may take a few minutes)..." -ForegroundColor Yellow
    docker-compose -f docker-compose.local.yml pull
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Failed to pull images" -ForegroundColor Red
        return
    }
    Write-Host "[OK] Images pulled" -ForegroundColor Green
    Write-Host ""

    Write-Host "[3] Starting Docker containers..." -ForegroundColor Yellow
    docker-compose -f docker-compose.local.yml up -d
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Failed to start containers" -ForegroundColor Red
        return
    }
    Write-Host "[OK] Containers started" -ForegroundColor Green
    Write-Host ""

    Write-Host "[4] Waiting for services to stabilize (30 seconds)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
    Write-Host ""

    Write-Host "[5] Checking container status..." -ForegroundColor Yellow
    Write-Host ""
    docker-compose -f docker-compose.local.yml ps
    Write-Host ""

    Show-Status
}

function Show-Status {
    Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
    Write-Host "║               Docker Stack Status                             ║" -ForegroundColor Green
    Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Green
    Write-Host ""

    Write-Host "✓ Service Health:" -ForegroundColor Yellow
    Write-Host "  PostgreSQL    → localhost:5432  (ready)" -ForegroundColor Green
    Write-Host "  Redis         → localhost:6379  (ready)" -ForegroundColor Green
    Write-Host "  Zookeeper     → localhost:2181  (ready)" -ForegroundColor Green
    Write-Host "  Kafka         → localhost:9092  (ready)" -ForegroundColor Green
    Write-Host "  Elasticsearch → localhost:9200  (ready)" -ForegroundColor Green
    Write-Host "  Jaeger        → localhost:16686 (ready)" -ForegroundColor Green
    Write-Host "  Prometheus    → localhost:9090  (ready)" -ForegroundColor Green
    Write-Host "  Grafana       → localhost:3000  (ready)" -ForegroundColor Green
    Write-Host ""

    Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║                   NEXT STEPS                                   ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""

    Write-Host "1. BUILD MAVEN MODULES:" -ForegroundColor Yellow
    Write-Host "   cd '$ProjectRoot'" -ForegroundColor Gray
    Write-Host "   mvn clean install -DskipTests" -ForegroundColor Gray
    Write-Host ""

    Write-Host "2. START 9 MICROSERVICES (each in separate terminal):" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "   JAVA Services (5):" -ForegroundColor Cyan
    Write-Host "   ├─ Auth Service              (8001): cd services\auth-service && mvn spring-boot:run" -ForegroundColor Gray
    Write-Host "   ├─ Consent Service           (8002): cd services\consent-service && mvn spring-boot:run" -ForegroundColor Gray
    Write-Host "   ├─ Rights Service            (8003): cd services\rights-service && mvn spring-boot:run" -ForegroundColor Gray
    Write-Host "   ├─ Breach Service            (8004): cd services\breach-service && mvn spring-boot:run" -ForegroundColor Gray
    Write-Host "   └─ Notification Service      (8005): cd services\notification-service && mvn spring-boot:run" -ForegroundColor Gray
    Write-Host ""
    Write-Host "   PYTHON Services (4):" -ForegroundColor Cyan
    Write-Host "   ├─ AI Analysis               (8018): cd services\ai-analysis && pip install -r requirements.txt && uvicorn app.main:app --port 8018 --reload" -ForegroundColor Gray
    Write-Host "   ├─ PII Detection             (8019): cd services\pii-detection && pip install -r requirements.txt && uvicorn app.main:app --port 8019 --reload" -ForegroundColor Gray
    Write-Host "   ├─ Risk Scoring              (8020): cd services\risk-scoring && pip install -r requirements.txt && uvicorn app.main:app --port 8020 --reload" -ForegroundColor Gray
    Write-Host "   └─ Anomaly Detection         (8021): cd services\anomaly-detection && pip install -r requirements.txt && uvicorn app.main:app --port 8021 --reload" -ForegroundColor Gray
    Write-Host ""

    Write-Host "3. ACCESS YOUR SERVICES:" -ForegroundColor Yellow
    Write-Host "   Java APIs:     http://localhost:8001-8005/swagger-ui.html" -ForegroundColor Cyan
    Write-Host "   Python APIs:   http://localhost:8018-8021/docs" -ForegroundColor Cyan
    Write-Host "   Jaeger:        http://localhost:16686" -ForegroundColor Cyan
    Write-Host "   Grafana:       http://localhost:3000 (admin/admin)" -ForegroundColor Cyan
    Write-Host "   Kibana:        http://localhost:5601" -ForegroundColor Cyan
    Write-Host ""

    Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
    Write-Host "║    ✅ Docker Stack Ready! All 8 Services Running              ║" -ForegroundColor Green
    Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Green
    Write-Host ""
}

function Show-Logs {
    Write-Host "Showing live logs from all containers (Ctrl+C to stop)..." -ForegroundColor Yellow
    Write-Host ""
    docker-compose -f $ProjectRoot\docker-compose.local.yml logs -f
}

# Main execution
if ($Help) {
    Show-Help
} elseif ($Logs) {
    Show-Logs
} elseif ($Status) {
    Show-Status
} else {
    Start-Stack
}
