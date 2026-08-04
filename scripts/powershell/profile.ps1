# Quick launcher for individual services

# Java service launcher
function Start-JavaService {
    param([string]$Service, [int]$Port)
    
    Write-Host "Starting $Service on port $Port..." -ForegroundColor Cyan
    Set-Location "d:\Development Practice\Datasheild\services\$Service"
    mvn spring-boot:run
}

# Python service launcher
function Start-PythonService {
    param([string]$Service, [int]$Port)
    
    Write-Host "Starting $Service on port $Port..." -ForegroundColor Cyan
    Set-Location "d:\Development Practice\Datasheild\services\$Service"
    
    Write-Host "Installing dependencies..." -ForegroundColor Yellow
    python -m pip install -r requirements.txt -q
    
    Write-Host "Starting FastAPI server..." -ForegroundColor Yellow
    uvicorn app.main:app --host 0.0.0.0 --port $Port --reload
}

# Quick access functions
function Go-DataShield {
    Set-Location "d:\Development Practice\Datasheild"
}

function Start-Auth {
    Start-JavaService "auth-service" 8001
}

function Start-Consent {
    Start-JavaService "consent-service" 8002
}

function Start-Rights {
    Start-JavaService "rights-service" 8003
}

function Start-Breach {
    Start-JavaService "breach-service" 8004
}

function Start-Notification {
    Start-JavaService "notification-service" 8005
}

function Start-AIAnalysis {
    Start-PythonService "ai-analysis" 8018
}

function Start-PII {
    Start-PythonService "pii-detection" 8019
}

function Start-RiskScoring {
    Start-PythonService "risk-scoring" 8020
}

function Start-Anomaly {
    Start-PythonService "anomaly-detection" 8021
}

# Docker functions
function Start-Docker-Stack {
    Write-Host "Starting Docker containers..." -ForegroundColor Green
    docker-compose -f docker-compose.local.yml up -d
    Start-Sleep -Seconds 10
    docker ps --format "table {{.Names}}\t{{.Status}}"
}

function Stop-Docker-Stack {
    Write-Host "Stopping Docker containers..." -ForegroundColor Yellow
    docker-compose -f docker-compose.local.yml down
}

function Logs-Docker-Postgres {
    docker logs datasheild-postgres -f
}

# Health checks
function Check-Services {
    Write-Host ""
    Write-Host "=== Service Health Check ===" -ForegroundColor Cyan
    Write-Host ""
    
    $services = @(
        @{Name="Auth"; Port=8001; Type="Java"},
        @{Name="Consent"; Port=8002; Type="Java"},
        @{Name="Rights"; Port=8003; Type="Java"},
        @{Name="Breach"; Port=8004; Type="Java"},
        @{Name="Notification"; Port=8005; Type="Java"},
        @{Name="AI Analysis"; Port=8018; Type="Python"},
        @{Name="PII Detection"; Port=8019; Type="Python"},
        @{Name="Risk Scoring"; Port=8020; Type="Python"},
        @{Name="Anomaly Detection"; Port=8021; Type="Python"}
    )
    
    foreach ($service in $services) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)/health" -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host "[OK] $($service.Name) ($($service.Type) - :$($service.Port))" -ForegroundColor Green
            } else {
                Write-Host "[DOWN] $($service.Name) - Status: $($response.StatusCode)" -ForegroundColor Yellow
            }
        }
        catch {
            Write-Host "[DOWN] $($service.Name) - Connection refused" -ForegroundColor Red
        }
    }
    
    Write-Host ""
    Write-Host "Observability:" -ForegroundColor Cyan
    Write-Host "  Jaeger:     http://localhost:16686"
    Write-Host "  Prometheus: http://localhost:9090"
    Write-Host "  Grafana:    http://localhost:3000"
    Write-Host ""
}

# Usage info
function Show-Help {
    Write-Host ""
    Write-Host "DataShield Service Launcher" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Navigate:" -ForegroundColor Yellow
    Write-Host "  Go-DataShield               # Go to project root"
    Write-Host ""
    Write-Host "Docker:" -ForegroundColor Yellow
    Write-Host "  Start-Docker-Stack         # Start PostgreSQL + Jaeger + Prometheus + Grafana"
    Write-Host "  Stop-Docker-Stack          # Stop all Docker containers"
    Write-Host "  Logs-Docker-Postgres       # View PostgreSQL logs"
    Write-Host ""
    Write-Host "Java Services:" -ForegroundColor Yellow
    Write-Host "  Start-Auth                 # Auth Service (8001)"
    Write-Host "  Start-Consent              # Consent Service (8002)"
    Write-Host "  Start-Rights               # Rights Service (8003)"
    Write-Host "  Start-Breach               # Breach Service (8004)"
    Write-Host "  Start-Notification         # Notification Service (8005)"
    Write-Host ""
    Write-Host "Python Services:" -ForegroundColor Yellow
    Write-Host "  Start-AIAnalysis           # AI Analysis Service (8018)"
    Write-Host "  Start-PII                  # PII Detection Service (8019)"
    Write-Host "  Start-RiskScoring          # Risk Scoring Service (8020)"
    Write-Host "  Start-Anomaly              # Anomaly Detection Service (8021)"
    Write-Host ""
    Write-Host "Health & Info:" -ForegroundColor Yellow
    Write-Host "  Check-Services             # Check all service health"
    Write-Host "  Show-Help                  # Show this help"
    Write-Host ""
}

Write-Host ""
Write-Host "DataShield Local Development Environment Loaded!" -ForegroundColor Green
Write-Host "Type 'Show-Help' for available commands" -ForegroundColor Yellow
Write-Host ""
