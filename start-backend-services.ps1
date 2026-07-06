# ============================================================================
# Datasheild Backend Services Startup Script
# Purpose: Build and start critical backend microservices
# ============================================================================

$ErrorActionPreference = "Continue"

# Configuration
$SERVICES = @(
    "auth-service",
    "tenant-service",
    "breach-service",
    "consent-service",
    "rights-service",
    "notification-service",
    "audit-service"
)

$ROOT_PATH = "d:\Development Practice\Datasheild\services"
$KAFKA_BOOTSTRAP = "localhost:9092"  # Set to empty string or unused host to disable Kafka
$DB_URL = "jdbc:postgresql://localhost:5432/datasheild"
$LOG_DIR = "$env:TEMP\datasheild-services"

# Create log directory
if (-not (Test-Path $LOG_DIR)) {
    New-Item -ItemType Directory -Path $LOG_DIR | Out-Null
}

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Datasheild Backend Services Startup" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Services to start: $($SERVICES.Count)"
Write-Host "Log directory: $LOG_DIR"
Write-Host ""

# Function to start a service
function Start-Service {
    param(
        [string]$ServiceName,
        [int]$Port
    )
    
    $ServicePath = Join-Path $ROOT_PATH $ServiceName
    
    if (-not (Test-Path $ServicePath)) {
        Write-Host "  ✗ Service directory not found: $ServiceName" -ForegroundColor Red
        return
    }
    
    Write-Host "  Starting $ServiceName on port $Port..." -ForegroundColor Yellow
    
    $LogFile = Join-Path $LOG_DIR "$ServiceName.log"
    
    # Build and start service in background
    $Command = @"
cd "$ServicePath"; `
mvn clean install -DskipTests -q 2>&1; `
`$env:KAFKA_BOOTSTRAP_SERVERS = '$KAFKA_BOOTSTRAP'; `
`$env:DB_URL = '$DB_URL'; `
java -jar target/$ServiceName-*.jar --server.port=$Port
"@
    
    Start-Process -FilePath "powershell.exe" -ArgumentList "-Command", $Command -RedirectStandardOutput $LogFile -RedirectStandardError $LogFile
    
    Write-Host "    ✓ Started (PID monitoring available in logs)" -ForegroundColor Green
}

# Port mapping
$PortMap = @{
    "auth-service"           = 8001
    "tenant-service"         = 8002
    "breach-service"         = 8004
    "consent-service"        = 8003
    "rights-service"         = 8006
    "notification-service"   = 8005
    "audit-service"          = 8007
}

# Start all services
foreach ($Service in $SERVICES) {
    $Port = $PortMap[$Service]
    Start-Service -ServiceName $Service -Port $Port
    Start-Sleep -Seconds 2
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Services startup initiated!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Log files:" -ForegroundColor Yellow
Get-ChildItem $LOG_DIR -Filter "*.log" | ForEach-Object {
    Write-Host "  - $($_.Name)" -ForegroundColor Cyan
}
Write-Host ""
Write-Host "Service Health Checks (after 30 seconds):" -ForegroundColor Yellow
Start-Sleep -Seconds 30

$Ports = 8001, 8002, 8003, 8004, 8005, 8006, 8007
foreach ($Port in $Ports) {
    try {
        $Response = Invoke-WebRequest -Uri "http://localhost:$Port/actuator/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($Response.StatusCode -eq 200) {
            Write-Host "  ✓ Port $Port: HEALTHY" -ForegroundColor Green
        } else {
            Write-Host "  ✗ Port $Port: UNHEALTHY ($($Response.StatusCode))" -ForegroundColor Red
        }
    } catch {
        Write-Host "  ? Port $Port: NOT RESPONDING" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Check logs in: $LOG_DIR"
Write-Host "2. Access services at their respective ports"
Write-Host "3. Verify database connectivity with: SELECT * FROM information_schema.tables;"
Write-Host "4. Check Kafka status if needed"
