@echo off
setlocal EnableDelayedExpansion

:: ============================================================
:: DataShield India - Build + Start All Services
:: Logs: logs\startup.log  |  Per-service: logs\<svc>.log
:: ============================================================

set "ROOT=%~dp0"
set "LOGS=%ROOT%logs"
set "SERVICES=%ROOT%services"
set "HEALTH_TIMEOUT=120"
set "STARTED_COUNT=0"
set "FAILED_COUNT=0"
set "FAILED_LIST="

:: Maven: prefer bundled, fall back to system
set "MVN=%ROOT%tools\apache-maven-3.9.16\bin\mvn.cmd"
if not exist "%MVN%" set "MVN=mvn"

:: ── Setup ────────────────────────────────────────────────────
if not exist "%LOGS%" mkdir "%LOGS%"
del /q "%LOGS%\startup.log" 2>nul
del /q "%LOGS%\run_*.bat" 2>nul

call :log "=================================================="
call :log " DataShield India - Build + Start All Services"
call :log " Started: %DATE% %TIME%"
call :log "=================================================="
call :log ""

:: ── Pre-flight ───────────────────────────────────────────────
call :log "[PRE-FLIGHT] Checking prerequisites..."

java -version >nul 2>&1
if errorlevel 1 (
    call :log "[ERROR] Java not found in PATH"
    call :log "[ABORT] Install Java 21 and ensure it is on PATH"
    goto :summary
)
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    call :log "[OK]    Java %%v"
)

"%MVN%" -version >nul 2>&1
if errorlevel 1 (
    call :log "[ERROR] Maven not found: %MVN%"
    call :log "[ABORT] Install Maven 3.9+ or place it in tools\apache-maven-3.9.16"
    goto :summary
)
for /f "tokens=1-3" %%a in ('"%MVN%" -version 2^>^&1 ^| findstr /i "apache maven"') do (
    call :log "[OK]    %%a %%b %%c"
)

python --version >nul 2>&1
if errorlevel 1 (
    call :log "[WARN]  Python not found - Python ML services will be skipped"
    set "PYTHON_OK=0"
) else (
    for /f "tokens=*" %%v in ('python --version 2^>^&1') do call :log "[OK]    %%v"
    set "PYTHON_OK=1"
)

curl --version >nul 2>&1
if errorlevel 1 (
    call :log "[WARN]  curl not found - health checks will be skipped"
    set "CURL_OK=0"
) else (
    call :log "[OK]    curl found"
    set "CURL_OK=1"
)

call :log "[PRE-FLIGHT] Done."
call :log ""

:: ── Root build (installs parent POM + shared libs to .m2) ────
call :log "[ROOT BUILD] mvn clean install -DskipTests ..."
set "ROOT_LOG=%LOGS%\root-build.log"
echo DataShield Root Build - %DATE% %TIME% > "%ROOT_LOG%"

pushd "%ROOT%"
"%MVN%" clean install -DskipTests >> "%ROOT_LOG%" 2>&1
set "ROOT_ERR=!errorlevel!"
popd

if !ROOT_ERR! NEQ 0 (
    call :log "[ERROR] Root build FAILED - shared libs not installed to .m2"
    call :log "        See: %ROOT_LOG%"
    call :log "[ABORT] Fix build errors before starting services"
    goto :summary
)
call :log "[OK]    Root build complete - parent POM + libs installed"
call :log ""

:: ── Group 1: Foundation ──────────────────────────────────────
call :log "========== GROUP 1: Foundation =========="
call :build_and_start "tenant-service"    "8007"
call :build_and_start "auth-service"      "8001"
call :build_and_start "config-service"    "8026"

:: ── Group 2: Core Compliance ─────────────────────────────────
call :log ""
call :log "========== GROUP 2: Core Compliance =========="
call :build_and_start "consent-service"      "8002"
call :build_and_start "rights-service"       "8003"
call :build_and_start "breach-service"       "8004"
call :build_and_start "audit-service"        "8006"
call :build_and_start "notification-service" "8005"
call :build_and_start "dpbi-service"         "8025"

:: ── Group 3: Platform ────────────────────────────────────────
call :log ""
call :log "========== GROUP 3: Platform =========="
call :build_and_start "vendor-service"    "8010"
call :build_and_start "policy-service"    "8009"
call :build_and_start "retention-service" "8011"
call :build_and_start "grievance-service" "8012"
call :build_and_start "workflow-service"  "8008"
call :build_and_start "connector-service" "8022"
call :build_and_start "webhook-service"   "8023"
call :build_and_start "siem-service"      "8024"

:: ── Group 4: Analytics ───────────────────────────────────────
call :log ""
call :log "========== GROUP 4: Analytics =========="
call :build_and_start "analytics-service" "8013"
call :build_and_start "report-service"    "8014"
call :build_and_start "search-service"    "8027"

:: ── Group 5: Data Intelligence ───────────────────────────────
call :log ""
call :log "========== GROUP 5: Data Intelligence =========="
call :build_and_start "data-discovery-service"      "8015"
call :build_and_start "data-classification-service" "8016"
call :build_and_start "data-lineage-service"        "8017"
call :build_and_start "pii-detection-service"       "8019"
call :build_and_start "ai-analysis-service"         "8018"
call :build_and_start "anomaly-detection-service"   "8021"
call :build_and_start "risk-scoring-service"        "8020"

:: ── Group 6: Python ML ───────────────────────────────────────
call :log ""
call :log "========== GROUP 6: Python ML =========="
if "%PYTHON_OK%"=="1" (
    call :start_python "pii-detection"     "app.main:app" "8019"
    call :start_python "ai-analysis"       "app.main:app" "8018"
    call :start_python "anomaly-detection" "app.main:app" "8021"
    call :start_python "risk-scoring"      "app.main:app" "8020"
) else (
    call :log "[SKIP]  Python not available - skipping all Python ML services"
)

:: ── Summary ──────────────────────────────────────────────────
:summary
call :log ""
call :log "=================================================="
call :log " STARTUP SUMMARY  -  %DATE% %TIME%"
call :log "  Started OK : !STARTED_COUNT!"
call :log "  Failed     : !FAILED_COUNT!"
if not "!FAILED_LIST!"=="" (
    call :log "  Failed svcs: !FAILED_LIST!"
)
call :log ""
call :log "  Service URLs:"
call :log "    Auth Service        http://localhost:8001/swagger-ui.html"
call :log "    Consent Service     http://localhost:8002/swagger-ui.html"
call :log "    Rights Service      http://localhost:8003/swagger-ui.html"
call :log "    Breach Service      http://localhost:8004/swagger-ui.html"
call :log "    Notification Svc    http://localhost:8005/swagger-ui.html"
call :log "    Audit Service       http://localhost:8006/swagger-ui.html"
call :log "    Tenant Service      http://localhost:8007/swagger-ui.html"
call :log "    Workflow Service    http://localhost:8008/swagger-ui.html"
call :log "    Policy Service      http://localhost:8009/swagger-ui.html"
call :log "    Vendor Service      http://localhost:8010/swagger-ui.html"
call :log "    Retention Service   http://localhost:8011/swagger-ui.html"
call :log "    Grievance Service   http://localhost:8012/swagger-ui.html"
call :log "    Analytics Service   http://localhost:8013/swagger-ui.html"
call :log "    Report Service      http://localhost:8014/swagger-ui.html"
call :log "    Discovery Service   http://localhost:8015/swagger-ui.html"
call :log "    Classif. Service    http://localhost:8016/swagger-ui.html"
call :log "    Lineage Service     http://localhost:8017/swagger-ui.html"
call :log "    AI Analysis (Py)    http://localhost:8018/docs"
call :log "    PII Detection (Py)  http://localhost:8019/docs"
call :log "    Risk Scoring (Py)   http://localhost:8020/docs"
call :log "    Anomaly Det. (Py)   http://localhost:8021/docs"
call :log "    Connector Service   http://localhost:8022/swagger-ui.html"
call :log "    Webhook Service     http://localhost:8023/swagger-ui.html"
call :log "    SIEM Service        http://localhost:8024/swagger-ui.html"
call :log "    DPBI Service        http://localhost:8025/swagger-ui.html"
call :log "    Config Service      http://localhost:8026/swagger-ui.html"
call :log "    Search Service      http://localhost:8027/swagger-ui.html"
call :log "=================================================="
call :log "  Full log: %LOGS%\startup.log"
call :log "=================================================="

echo.
if "!FAILED_COUNT!" GTR "0" (
    echo  [!] !FAILED_COUNT! service(s^) failed: !FAILED_LIST!
    echo      Check individual logs in: %LOGS%\
) else (
    echo  [OK] All !STARTED_COUNT! services started successfully.
)
echo.
echo  Full log: %LOGS%\startup.log
echo.
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
    call :log "[SKIP]  %SVC% - directory not found: %SVC_DIR%"
    goto :eof
)

call :check_port %PORT%
if "!PORT_OPEN!"=="1" (
    call :log "[SKIP]  %SVC% (port %PORT% already in use - assuming running)"
    set /a STARTED_COUNT+=1
    goto :eof
)

:: ── Build ────────────────────────────────────────────────────
call :log "[BUILD] %SVC% (port %PORT%) ..."
echo DataShield Build Log - %SVC% - %DATE% %TIME% > "%SVC_LOG%"
echo. >> "%SVC_LOG%"

pushd "%SVC_DIR%"
"%MVN%" clean package -DskipTests >> "%SVC_LOG%" 2>&1
set "BERR=!errorlevel!"
popd

if !BERR! NEQ 0 (
    call :log "[ERROR] %SVC% - Maven build FAILED (exit !BERR!)"
    call :log "        Log: %SVC_LOG%"
    call :log "        --- last build output ---"
    call :tail_log "%SVC_LOG%" 15
    set /a FAILED_COUNT+=1
    set "FAILED_LIST=!FAILED_LIST! %SVC%"
    goto :eof
)
call :log "        [BUILD OK] %SVC%"

:: ── Find JAR ─────────────────────────────────────────────────
set "JAR=%SVC_DIR%\target\%SVC%.jar"
if not exist "!JAR!" (
    set "JAR="
    for /f "delims=" %%f in ('dir /b "%SVC_DIR%\target\*.jar" 2^>nul ^| findstr /v "original" ^| findstr /v "1\.0\.0"') do (
        set "JAR=%SVC_DIR%\target\%%f"
    )
    if "!JAR!"=="" (
        for /f "delims=" %%f in ('dir /b "%SVC_DIR%\target\*.jar" 2^>nul ^| findstr /v "original"') do (
            set "JAR=%SVC_DIR%\target\%%f"
        )
    )
)

if "!JAR!"=="" (
    call :log "[ERROR] %SVC% - no executable JAR found in %SVC_DIR%\target\"
    call :log "        Expected: %SVC%.jar (fat JAR from spring-boot:repackage)"
    set /a FAILED_COUNT+=1
    set "FAILED_LIST=!FAILED_LIST! %SVC%"
    goto :eof
)
call :log "        [JAR]   !JAR!"

:: ── Start (via generated runner .bat - avoids nested-quote issues with spaces in paths) ──
echo. >> "%SVC_LOG%"
echo ===== SERVICE STARTUP - %DATE% %TIME% ===== >> "%SVC_LOG%"
echo. >> "%SVC_LOG%"

set "RUNNER=%LOGS%\run_%SVC%.bat"
> "%RUNNER%" echo @echo off
>> "%RUNNER%" echo java -jar "!JAR!" ^>^> "%SVC_LOG%" 2^>^&1

start "DS-%SVC%" /MIN "%RUNNER%"
call :log "        [START] %SVC% launched in background"

:: ── Wait for health ──────────────────────────────────────────
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
    call :log "[SKIP]  %SVC% (Python) - directory not found: %SVC_DIR%"
    goto :eof
)

call :check_port %PORT%
if "!PORT_OPEN!"=="1" (
    call :log "[SKIP]  %SVC% (Python) - port %PORT% already in use"
    set /a STARTED_COUNT+=1
    goto :eof
)

call :log "[PY]    %SVC% on port %PORT% ..."
echo DataShield Python Log - %SVC% - %DATE% %TIME% > "%SVC_LOG%"

:: Create venv if missing
if not exist "%VENV%\Scripts\python.exe" (
    call :log "        Creating venv at %VENV% ..."
    python -m venv "%VENV%" >> "%SVC_LOG%" 2>&1
    if errorlevel 1 (
        call :log "[ERROR] %SVC% - venv creation failed"
        set /a FAILED_COUNT+=1
        set "FAILED_LIST=!FAILED_LIST! %SVC%"
        goto :eof
    )
    call :log "        [VENV OK]"
)

:: Install requirements
if exist "%SVC_DIR%\requirements.txt" (
    call :log "        Installing requirements ..."
    "%VENV%\Scripts\pip.exe" install -r "%SVC_DIR%\requirements.txt" -q >> "%SVC_LOG%" 2>&1
    if errorlevel 1 (
        call :log "[WARN]  %SVC% - pip install had errors (may still work)"
    ) else (
        call :log "        [PIP OK]"
    )
)

:: ── Start (via generated runner .bat - avoids nested-quote issues with spaces in paths) ──
echo. >> "%SVC_LOG%"
echo ===== SERVICE STARTUP - %DATE% %TIME% ===== >> "%SVC_LOG%"

set "RUNNER=%LOGS%\run_%SVC%.bat"
> "%RUNNER%" echo @echo off
>> "%RUNNER%" echo cd /d "%SVC_DIR%"
>> "%RUNNER%" echo "%VENV%\Scripts\uvicorn.exe" %APP% --host 0.0.0.0 --port %PORT% --log-level info ^>^> "%SVC_LOG%" 2^>^&1

start "DS-%SVC%-py" /MIN "%RUNNER%"
call :log "        [START] %SVC% launched in background"

set "WAIT_PORT=%PORT%"
set "WAIT_SVC=%SVC%"
call :wait_healthy
goto :eof

:: ============================================================
:: :wait_healthy  (reads WAIT_PORT, WAIT_SVC)
:: Uses ping for delay - works in non-interactive cmd /c context
:: ============================================================
:wait_healthy
set "WP=%WAIT_PORT%"
set "WS=%WAIT_SVC%"
set "WE=0"
call :log "        [WAIT]  %WS% on port %WP% (timeout %HEALTH_TIMEOUT%s) ..."

:wh_loop
ping -n 4 127.0.0.1 >nul 2>&1
set /a WE+=3

if "%CURL_OK%"=="0" (
    call :log "[OK]    %WS% started (no curl - skipping health check)"
    set /a STARTED_COUNT+=1
    goto :eof
)

curl -sf "http://localhost:%WP%/actuator/health" >nul 2>&1
if not errorlevel 1 (
    call :log "[OK]    %WS% is UP on port %WP% (%WE%s elapsed)"
    set /a STARTED_COUNT+=1
    goto :eof
)

curl -sf "http://localhost:%WP%/health" >nul 2>&1
if not errorlevel 1 (
    call :log "[OK]    %WS% is UP on port %WP% (%WE%s elapsed)"
    set /a STARTED_COUNT+=1
    goto :eof
)

if %WE% LSS %HEALTH_TIMEOUT% (
    call :log "        [WAIT]  %WS% not ready yet (%WE%s / %HEALTH_TIMEOUT%s) ..."
    goto :wh_loop
)

call :log "[FAIL]  %WS% did not become healthy within %HEALTH_TIMEOUT%s"
call :log "        Check log: %LOGS%\%WS%.log"
set /a FAILED_COUNT+=1
set "FAILED_LIST=!FAILED_LIST! %WS%"
goto :eof

:: ============================================================
:: :check_port  <port>  ->  sets PORT_OPEN=1|0
:: ============================================================
:check_port
set "PORT_OPEN=0"
powershell -NoProfile -Command ^
    "try { $t = New-Object Net.Sockets.TcpClient('localhost', %~1); $t.Close(); exit 0 } catch { exit 1 }" >nul 2>&1
if not errorlevel 1 set "PORT_OPEN=1"
goto :eof

:: ============================================================
:: :tail_log  <file>  <n-lines>   -> prints last N lines via PowerShell
:: ============================================================
:tail_log
set "TL_FILE=%~1"
set "TL_N=%~2"
for /f "usebackq delims=" %%L in (`powershell -NoProfile -Command "Get-Content -LiteralPath '%TL_FILE%' -Tail %TL_N% -ErrorAction SilentlyContinue"`) do (
    call :log "        %%L"
)
goto :eof

:: ============================================================
:: :log  <message>
:: ============================================================
:log
echo [%TIME%] %~1
echo [%DATE% %TIME%] %~1 >> "%LOGS%\startup.log"
goto :eof