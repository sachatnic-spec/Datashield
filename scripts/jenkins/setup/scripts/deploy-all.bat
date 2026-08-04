@echo off
setlocal EnableDelayedExpansion

:: ============================================================
:: DataShield India - DEPLOY (all services, or just one)
:: Assumes build-all.bat already ran for whatever you're deploying
:: (jar in target\, or venv ready for python).
::
:: Usage:
::   deploy-all.bat                  -> deploys everything
::   deploy-all.bat consent-service  -> restarts ONLY consent-service
::   deploy-all.bat pii-detection    -> restarts ONLY that Python service
:: ============================================================

set "ROOT=%~dp0.."
set "SERVICES=%ROOT%\services"
set "LOGS=%ROOT%\logs"
set "RUNLOGS=%LOGS%\runtime"
set "HEALTH_TIMEOUT=120"
set "STARTED_COUNT=0"
set "FAILED_COUNT=0"
set "FAILED_LIST="
set "TARGET=%~1"
set "FOUND=0"

if not exist "%RUNLOGS%" mkdir "%RUNLOGS%"

call :log "=================================================="
if "%TARGET%"=="" (
    call :log " DataShield India - DEPLOY ALL - %DATE% %TIME%"
) else (
    call :log " DataShield India - DEPLOY ONE (%TARGET%) - %DATE% %TIME%"
)
call :log "=================================================="

curl --version >nul 2>&1
if errorlevel 1 (set "CURL_OK=0") else (set "CURL_OK=1")

call :maybe_deploy_java "tenant-service"    "8007"
call :maybe_deploy_java "auth-service"      "8001"
call :maybe_deploy_java "config-service"    "8026"
call :maybe_deploy_java "consent-service"      "8002"
call :maybe_deploy_java "rights-service"       "8003"
call :maybe_deploy_java "breach-service"       "8004"
call :maybe_deploy_java "audit-service"        "8006"
call :maybe_deploy_java "notification-service" "8005"
call :maybe_deploy_java "dpbi-service"         "8025"
call :maybe_deploy_java "vendor-service"    "8010"
call :maybe_deploy_java "policy-service"    "8009"
call :maybe_deploy_java "retention-service" "8011"
call :maybe_deploy_java "grievance-service" "8012"
call :maybe_deploy_java "workflow-service"  "8008"
call :maybe_deploy_java "connector-service" "8022"
call :maybe_deploy_java "webhook-service"   "8023"
call :maybe_deploy_java "siem-service"      "8024"
call :maybe_deploy_java "analytics-service" "8013"
call :maybe_deploy_java "report-service"    "8014"
call :maybe_deploy_java "search-service"    "8027"
call :maybe_deploy_java "data-discovery-service"      "8015"
call :maybe_deploy_java "data-classification-service" "8016"
call :maybe_deploy_java "data-lineage-service"        "8017"

call :maybe_deploy_python "pii-detection"     "app.main:app" "8019"
call :maybe_deploy_python "ai-analysis"       "app.main:app" "8018"
call :maybe_deploy_python "anomaly-detection" "app.main:app" "8021"
call :maybe_deploy_python "risk-scoring"      "app.main:app" "8020"

if not "%TARGET%"=="" if !FOUND! EQU 0 (
    call :log "[ERROR] Unknown service name: %TARGET%"
    set /a FAILED_COUNT+=1
    set "FAILED_LIST=!FAILED_LIST! unknown-target:%TARGET%"
)

call :log ""
call :log "=================================================="
call :log " DEPLOY SUMMARY - %DATE% %TIME%"
call :log "  Started: !STARTED_COUNT!   Failed: !FAILED_COUNT!"
if not "!FAILED_LIST!"=="" call :log "  Failed:!FAILED_LIST!"
call :log "=================================================="

if !FAILED_COUNT! GTR 0 (
    echo DEPLOY completed with !FAILED_COUNT! failure(s^) - see %RUNLOGS%\deploy.log
    exit /b 1
)
echo DEPLOY OK - !STARTED_COUNT! service(s^) started/confirmed running
exit /b 0

:: ============================================================
:maybe_deploy_java  <svc-name>  <port>
:: ============================================================
:maybe_deploy_java
if "%TARGET%"=="" (
    call :deploy_java "%~1" "%~2"
    goto :eof
)
if /i "%TARGET%"=="%~1" (
    set "FOUND=1"
    call :deploy_java "%~1" "%~2"
)
goto :eof

:: ============================================================
:maybe_deploy_python  <svc-name>  <module>  <port>
:: ============================================================
:maybe_deploy_python
if "%TARGET%"=="" (
    call :deploy_python "%~1" "%~2" "%~3"
    goto :eof
)
if /i "%TARGET%"=="%~1" (
    set "FOUND=1"
    call :deploy_python "%~1" "%~2" "%~3"
)
goto :eof

:: ============================================================
:deploy_java  <svc-name>  <port>
:: ============================================================
:deploy_java
set "SVC=%~1"
set "PORT=%~2"
set "SVC_DIR=%SERVICES%\%SVC%"
set "SVC_LOG=%RUNLOGS%\%SVC%.log"

if not exist "%SVC_DIR%" (
    call :log "[SKIP]  %SVC% - directory not found"
    goto :eof
)

call :kill_port %PORT%

set "JAR="
for /f "delims=" %%f in ('dir /b "%SVC_DIR%\target\*.jar" 2^>nul ^| findstr /v "original"') do set "JAR=%SVC_DIR%\target\%%f"

if "!JAR!"=="" (
    call :log "[ERROR] %SVC% - no jar found in target\ (build it first: scripts\build-all.bat %SVC%)"
    set /a FAILED_COUNT+=1
    set "FAILED_LIST=!FAILED_LIST! %SVC%"
    goto :eof
)

echo Runtime log - %SVC% - %DATE% %TIME% > "%SVC_LOG%"
set "RUNNER=%RUNLOGS%\run_%SVC%.bat"
> "%RUNNER%" echo @echo off
>> "%RUNNER%" echo java -jar "!JAR!" ^>^> "%SVC_LOG%" 2^>^&1
start "DS-%SVC%" /MIN "%RUNNER%"
call :log "[START] %SVC% (port %PORT%) launched"

set "WAIT_PORT=%PORT%"
set "WAIT_SVC=%SVC%"
call :wait_healthy
goto :eof

:: ============================================================
:deploy_python  <svc-name>  <module>  <port>
:: ============================================================
:deploy_python
set "SVC=%~1"
set "APP=%~2"
set "PORT=%~3"
set "SVC_DIR=%SERVICES%\%SVC%"
set "SVC_LOG=%RUNLOGS%\%SVC%-py.log"
set "VENV=%SVC_DIR%\.venv"

if not exist "%SVC_DIR%" (
    call :log "[SKIP]  %SVC% (Python) - directory not found"
    goto :eof
)
if not exist "%VENV%\Scripts\uvicorn.exe" (
    call :log "[ERROR] %SVC% - venv/uvicorn missing (build it first: scripts\build-all.bat %SVC%)"
    set /a FAILED_COUNT+=1
    set "FAILED_LIST=!FAILED_LIST! %SVC%"
    goto :eof
)

call :kill_port %PORT%

echo Runtime log - %SVC% - %DATE% %TIME% > "%SVC_LOG%"
set "RUNNER=%RUNLOGS%\run_%SVC%.bat"
> "%RUNNER%" echo @echo off
>> "%RUNNER%" echo cd /d "%SVC_DIR%"
>> "%RUNNER%" echo "%VENV%\Scripts\uvicorn.exe" %APP% --host 0.0.0.0 --port %PORT% --log-level info ^>^> "%SVC_LOG%" 2^>^&1
start "DS-%SVC%-py" /MIN "%RUNNER%"
call :log "[START] %SVC% (Python, port %PORT%) launched"

set "WAIT_PORT=%PORT%"
set "WAIT_SVC=%SVC%"
call :wait_healthy
goto :eof

:: ============================================================
:kill_port  <port>
:: ============================================================
:kill_port
set "KP=%~1"
for /f "tokens=5" %%P in ('netstat -ano ^| findstr ":%KP% " ^| findstr "LISTENING"') do (
    call :log "        [KILL]  freeing port %KP% (PID %%P)"
    taskkill /PID %%P /F >nul 2>&1
)
goto :eof

:: ============================================================
:wait_healthy  (reads WAIT_PORT, WAIT_SVC)
:: ============================================================
:wait_healthy
set "WP=%WAIT_PORT%"
set "WS=%WAIT_SVC%"
set "WE=0"

:wh_loop
ping -n 4 127.0.0.1 >nul 2>&1
set /a WE+=3

if "%CURL_OK%"=="0" (
    call :log "[OK]    %WS% started (no curl - health check skipped)"
    set /a STARTED_COUNT+=1
    goto :eof
)

curl -sf "http://localhost:%WP%/actuator/health" >nul 2>&1
if not errorlevel 1 (
    call :log "[OK]    %WS% is UP on port %WP% (%WE%s)"
    set /a STARTED_COUNT+=1
    goto :eof
)
curl -sf "http://localhost:%WP%/health" >nul 2>&1
if not errorlevel 1 (
    call :log "[OK]    %WS% is UP on port %WP% (%WE%s)"
    set /a STARTED_COUNT+=1
    goto :eof
)

if %WE% LSS %HEALTH_TIMEOUT% goto :wh_loop

call :log "[FAIL]  %WS% did not become healthy within %HEALTH_TIMEOUT%s"
set /a FAILED_COUNT+=1
set "FAILED_LIST=!FAILED_LIST! %WS%"
goto :eof

:: ============================================================
:log  <message>
:: ============================================================
:log
echo [%TIME%] %~1
echo [%DATE% %TIME%] %~1 >> "%RUNLOGS%\deploy.log"
goto :eof
