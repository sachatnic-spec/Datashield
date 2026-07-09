@echo off
setlocal EnableDelayedExpansion

set "ROOT=%~dp0"
set "LOGS=%ROOT%logs"
set "SERVICES=%ROOT%services"

set "MVN=%ROOT%tools\apache-maven-3.9.16\bin\mvn.cmd"
if not exist "%MVN%" set "MVN=mvn"

set "HEALTH_TIMEOUT=120"

set "STARTED_COUNT=0"
set "FAILED_COUNT=0"
set "FAILED_LIST="

if not exist "%LOGS%" mkdir "%LOGS%"
del /q "%LOGS%\startup.log" 2>nul

call :log "=================================================="
call :log " DataShield India - Build + Start All Services"
call :log " %DATE% %TIME%"
call :log "=================================================="
call :log ""
call :log "[PRE-FLIGHT] Checking prerequisites..."

java -version >nul 2>&1
if errorlevel 1 ( call :log "[ERROR] Java not found. Aborting." & goto :summary )
call :log "[OK]    Java found"

"%MVN%" -version >nul 2>&1
if errorlevel 1 ( call :log "[ERROR] Maven not found. Aborting." & goto :summary )
call :log "[OK]    Maven: %MVN%"

python --version >nul 2>&1
if errorlevel 1 (
    call :log "[WARN]  Python not found - Python services skipped"
    set "PYTHON_OK=0"
) else (
    call :log "[OK]    Python found"
    set "PYTHON_OK=1"
)

call :log ""
call :log "[PRE-FLIGHT] Done."
call :log ""

call :log "GROUP 1: Foundation"
call :build_and_start "tenant-service"    "8007"
call :build_and_start "auth-service"      "8001"
call :build_and_start "config-service"    "8026"

call :log ""
call :log "GROUP 2: Core Compliance"
call :build_and_start "consent-service"      "8002"
call :build_and_start "rights-service"       "8003"
call :build_and_start "breach-service"       "8004"
call :build_and_start "audit-service"        "8006"
call :build_and_start "notification-service" "8005"
call :build_and_start "dpbi-service"         "8025"

call :log ""
call :log "GROUP 3: Platform"
call :build_and_start "vendor-service"    "8010"
call :build_and_start "policy-service"    "8009"
call :build_and_start "retention-service" "8011"
call :build_and_start "grievance-service" "8012"
call :build_and_start "workflow-service"  "8008"
call :build_and_start "connector-service" "8022"
call :build_and_start "webhook-service"   "8023"
call :build_and_start "siem-service"      "8024"

call :log ""
call :log "GROUP 4: Analytics"
call :build_and_start "analytics-service" "8013"
call :build_and_start "report-service"    "8014"
call :build_and_start "search-service"    "8027"

call :log ""
call :log "GROUP 5: Data Intelligence"
call :build_and_start "data-discovery-service"      "8015"
call :build_and_start "data-classification-service" "8016"
call :build_and_start "data-lineage-service"        "8017"
call :build_and_start "pii-detection-service"       "8019"
call :build_and_start "ai-analysis-service"         "8018"
call :build_and_start "anomaly-detection-service"   "8021"
call :build_and_start "risk-scoring-service"        "8020"

call :log ""
call :log "GROUP 6: Python ML"
if "%PYTHON_OK%"=="1" (
    call :start_python "pii-detection"     "app.main:app" "8019"
    call :start_python "ai-analysis"       "app.main:app" "8018"
    call :start_python "anomaly-detection" "app.main:app" "8021"
    call :start_python "risk-scoring"      "app.main:app" "8020"
) else (
    call :log "[SKIP]  Python not available"
)

:summary
call :log ""
call :log "=================================================="
call :log " STARTUP SUMMARY"
call :log " Started : !STARTED_COUNT!"
call :log " Failed  : !FAILED_COUNT!"
if not "!FAILED_LIST!"=="" call :log " Failed  : !FAILED_LIST!"
call :log "=================================================="
call :log ""
call :log "  Auth     http://localhost:8001/swagger-ui.html"
call :log "  Consent  http://localhost:8002/swagger-ui.html"
call :log "  Rights   http://localhost:8003/swagger-ui.html"
call :log "  Breach   http://localhost:8004/swagger-ui.html"
call :log "  Tenant   http://localhost:8007/swagger-ui.html"
call :log "  Audit    http://localhost:8006/swagger-ui.html"
call :log "=================================================="

if "!FAILED_COUNT!" GTR "0" (
    echo.
    echo [!] Some services failed. Check %LOGS%\startup.log
)
echo.
echo Full log: %LOGS%\startup.log
pause
goto :eof

:: ============================================================
:: :build_and_start  <svc-name>  <port>
:: ============================================================
:build_and_start
set "SVC=%~1"
set "PORT=%~2"
set "SVC_DIR=%SERVICES%\%SVC%"
set "SVC_LOG=%LOGS%\%SVC%.log"

if not exist "%SVC_DIR%" (
    call :log "[SKIP]  %SVC% - dir not found"
    goto :eof
)

call :check_port %PORT%
if "!PORT_OPEN!"=="1" (
    call :log "[SKIP]  %SVC% - port %PORT% already in use"
    set /a STARTED_COUNT+=1
    goto :eof
)

call :log "[BUILD] %SVC% ..."
echo. > "%SVC_LOG%"

pushd "%SVC_DIR%"
"%MVN%" clean package -DskipTests -q >> "%SVC_LOG%" 2>&1
set "BERR=!errorlevel!"
popd

if !BERR! NEQ 0 (
    call :log "[ERROR] %SVC% - build FAILED. See %SVC_LOG%"
    set /a FAILED_COUNT+=1
    set "FAILED_LIST=!FAILED_LIST! %SVC%"
    goto :eof
)
call :log "        Build OK"

set "JAR=%SVC_DIR%\target\%SVC%.jar"
if not exist "!JAR!" (
    set "JAR="
    for /f "delims=" %%f in ('dir /b "%SVC_DIR%\target\*.jar" 2^>nul ^| findstr /v "original" ^| findstr /v "1\.0\.0"') do set "JAR=%SVC_DIR%\target\%%f"
    if "!JAR!"=="" (
        for /f "delims=" %%f in ('dir /b "%SVC_DIR%\target\*.jar" 2^>nul ^| findstr /v "original"') do set "JAR=%SVC_DIR%\target\%%f"
    )
)

if "!JAR!"=="" (
    call :log "[ERROR] %SVC% - no JAR found"
    set /a FAILED_COUNT+=1
    set "FAILED_LIST=!FAILED_LIST! %SVC%"
    goto :eof
)

call :log "        JAR: !JAR!"
start "DS-%SVC%" /MIN cmd /c java -jar "!JAR!" ^>^"%SVC_LOG%" 2^>^&1

set "WAIT_PORT=%PORT%"
set "WAIT_SVC=%SVC%"
call :wait_healthy
goto :eof

:: ============================================================
:: :start_python  <svc-name>  <module>  <port>
:: ============================================================
:start_python
set "SVC=%~1"
set "APP=%~2"
set "PORT=%~3"
set "SVC_DIR=%SERVICES%\%SVC%"
set "SVC_LOG=%LOGS%\%SVC%-py.log"
set "VENV=%SVC_DIR%\.venv"

if not exist "%SVC_DIR%" (
    call :log "[SKIP]  %SVC% - dir not found"
    goto :eof
)

call :check_port %PORT%
if "!PORT_OPEN!"=="1" (
    call :log "[SKIP]  %SVC% - port %PORT% already in use"
    set /a STARTED_COUNT+=1
    goto :eof
)

call :log "[PY]    %SVC% on :%PORT%"

if not exist "%VENV%\Scripts\python.exe" (
    python -m venv "%VENV%" >> "%SVC_LOG%" 2>&1
)
if exist "%SVC_DIR%\requirements.txt" (
    "%VENV%\Scripts\pip.exe" install -r "%SVC_DIR%\requirements.txt" -q >> "%SVC_LOG%" 2>&1
)

start "DS-%SVC%-py" /MIN cmd /c "cd /d "%SVC_DIR%" && "%VENV%\Scripts\uvicorn.exe" %APP% --host 0.0.0.0 --port %PORT% >> "%SVC_LOG%" 2>&1"

set "WAIT_PORT=%PORT%"
set "WAIT_SVC=%SVC%"
call :wait_healthy
goto :eof

:: ============================================================
:: :wait_healthy  (uses WAIT_PORT and WAIT_SVC)
:: ============================================================
:wait_healthy
set "WP=%WAIT_PORT%"
set "WS=%WAIT_SVC%"
set "WE=0"
call :log "        Waiting for %WS% on :%WP% ..."

:wh_loop
ping -n 4 127.0.0.1 >nul
set /a WE+=3
curl -sf "http://localhost:%WP%/actuator/health" >nul 2>&1
if not errorlevel 1 ( call :log "[OK]    %WS% UP (%WE%s)" & set /a STARTED_COUNT+=1 & goto :eof )
curl -sf "http://localhost:%WP%/health" >nul 2>&1
if not errorlevel 1 ( call :log "[OK]    %WS% UP (%WE%s)" & set /a STARTED_COUNT+=1 & goto :eof )
if %WE% LSS %HEALTH_TIMEOUT% goto :wh_loop

call :log "[FAIL]  %WS% not healthy after %HEALTH_TIMEOUT%s"
set /a FAILED_COUNT+=1
set "FAILED_LIST=!FAILED_LIST! %WS%"
goto :eof

:: ============================================================
:: :check_port  <port>  ->  PORT_OPEN=1|0
:: ============================================================
:check_port
set "PORT_OPEN=0"
powershell -NoProfile -Command "try{$t=New-Object Net.Sockets.TcpClient('localhost',%~1);$t.Close();exit 0}catch{exit 1}" >nul 2>&1
if not errorlevel 1 set "PORT_OPEN=1"
goto :eof

:: ============================================================
:: :log  <msg>
:: ============================================================
:log
echo [%TIME%] %~1
echo [%DATE% %TIME%] %~1 >> "%LOGS%\startup.log"
goto :eof
