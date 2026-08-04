@echo off
setlocal EnableDelayedExpansion

:: =============================================================================
:: DataShield India — Service Status Check
:: =============================================================================

echo.
echo  DataShield India — Service Status  [%DATE% %TIME%]
echo  =====================================================
echo  %-30s  %-6s  %-8s
echo.

set "UP=0"
set "DOWN=0"

call :check "auth-service"               8001
call :check "consent-service"            8002
call :check "rights-service"             8003
call :check "breach-service"             8004
call :check "dpbi-service"               8005
call :check "notification-service"       8006
call :check "audit-service"              8007
call :check "tenant-service"             8008
call :check "vendor-service"             8009
call :check "policy-service"             8010
call :check "workflow-service"           8011
call :check "config-service"             8012
call :check "connector-service"          8013
call :check "search-service"             8014
call :check "webhook-service"            8015
call :check "siem-service"               8016
call :check "analytics-service"          8017
call :check "data-classification-service" 8021
call :check "data-discovery-service"     8022
call :check "pii-detection-service"      8024
call :check "ai-analysis-service"        8025
call :check "anomaly-detection-service"  8026
call :check "risk-scoring-service"       8027
echo  --- Python ML Services ---
call :check "pii-detection (py)"         8101
call :check "ai-analysis (py)"           8102
call :check "anomaly-detection (py)"     8103
call :check "risk-scoring (py)"          8104

echo.
echo  =====================================================
echo  UP: !UP!   DOWN: !DOWN!
echo  =====================================================
echo.
pause >nul
goto :eof

:check
set "NAME=%~1"
set "PORT=%~2"
set "STATUS=DOWN"
set "COLOR=[DOWN]"

powershell -NoProfile -Command ^
  "try{$t=New-Object Net.Sockets.TcpClient('localhost',%PORT%);$t.Close();exit 0}catch{exit 1}" >nul 2>&1
if not errorlevel 1 (
    set "STATUS=UP  "
    set /a UP+=1
) else (
    set /a DOWN+=1
)

echo  %-38s  :%PORT%  !STATUS!
goto :eof
