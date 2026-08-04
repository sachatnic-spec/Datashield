@echo off
setlocal EnableDelayedExpansion

REM =====================================================================
REM  DataShield India - Start All Services (prebuilt artifacts)
REM  Place this .bat next to the "build" folder (the one containing
REM  frontend_*, jars_*, python_* subfolders) and just run it.
REM =====================================================================

set "ROOT=%~dp0..\.."
set "BUILD=%ROOT%\build"
set "LOGS=%ROOT%\logs"

set "HEALTH_TIMEOUT=90"

set "STARTED_COUNT=0"
set "FAILED_COUNT=0"
set "FAILED_LIST="

if not exist "%LOGS%" mkdir "%LOGS%"
del /q "%LOGS%\startup.log" 2>nul

REM ---- locate the latest timestamped build folders (name sorts by date) ----
set "JARS_DIR="
for /f "delims=" %%d in ('dir /b /ad /o-n "%BUILD%\jars_*" 2^>nul') do if not defined JARS_DIR set "JARS_DIR=%BUILD%\%%d"

set "PY_DIR="
for /f "delims=" %%d in ('dir /b /ad /o-n "%BUILD%\python_*" 2^>nul') do if not defined PY_DIR set "PY_DIR=%BUILD%\%%d"

set "FE_DIR="
for /f "delims=" %%d in ('dir /b /ad /o-n "%BUILD%\frontend_*" 2^>nul') do if not defined FE_DIR set "FE_DIR=%BUILD%\%%d"

call :log "=================================================="
call :log " DataShield India - Start All Services (prebuilt)"
call :log " %DATE% %TIME%"
call :log "=================================================="
call :log " JARS_DIR : %JARS_DIR%"
call :log " PY_DIR   : %PY_DIR%"
call :log " FE_DIR   : %FE_DIR%"
call :log ""

if not defined JARS_DIR (
    call :log "[ERROR] No jars_* folder found under %BUILD%. Aborting."
    goto :summary
)

call :log "[PRE-FLIGHT] Checking prerequisites..."

java -version >nul 2>&1
if errorlevel 1 ( call :log "[ERROR] Java not found. Aborting." & goto :summary )
call :log "[OK]    Java found"

python --version >nul 2>&1
if errorlevel 1 (
    call :log "[WARN]  Python not found - Python services and static frontends skipped"
    set "PYTHON_OK=0"
) else (
    call :log "[OK]    Python found"
    set "PYTHON_OK=1"
)

call :log ""
call :log "[PRE-FLIGHT] Done."
call :log ""

call :log "GROUP 1: Foundation"
call :start_jar "service-registry" "8761"
call :start_jar "config-service"   "8026"
call :start_jar "tenant-service"   "8007"
call :start_jar "auth-service"     "8001"

call :log ""
call :log "GROUP 2: Core Compliance"
call :start_jar "consent-service"      "8002"
call :start_jar "rights-service"       "8003"
call :start_jar "breach-service"       "8004"
call :start_jar "audit-service"        "8006"
call :start_jar "notification-service" "8005"
call :start_jar "dpbi-service"         "8025"

call :log ""
call :log "GROUP 3: Platform"
call :start_jar "vendor-service"    "8010"
call :start_jar "policy-service"    "8009"
call :start_jar "retention-service" "8011"
call :start_jar "grievance-service" "8012"
call :start_jar "connector-service" "8022"
call :start_jar "webhook-service"   "8023"
call :start_jar "siem-service"      "8024"

call :log ""
call :log "GROUP 4: Analytics"
call :start_jar "analytics-service" "8013"
call :start_jar "report-service"    "8014"
call :start_jar "search-service"    "8027"

call :log ""
call :log "GROUP 5: Data Intelligence (Java)"
call :start_jar "data-discovery-service"      "8015"
call :start_jar "data-classification-service" "8016"
call :start_jar "data-lineage-service"        "8017"

call :log ""
call :log "GROUP 6: API Gateway"
call :start_jar "api-gateway" "8080"

call :log ""
call :log "GROUP 7: Python ML services"
if "%PYTHON_OK%"=="1" (
    call :start_python "pii-detection"     "app.main:app" "8019"
    call :start_python "ai-analysis"       "app.main:app" "8018"
    call :start_python "anomaly-detection" "app.main:app" "8021"
    call :start_python "risk-scoring"      "app.main:app" "8020"
) else (
    call :log "[SKIP]  Python not available"
)

call :log ""
call :log "GROUP 8: Frontend (prebuilt static bundles)"
if "%PYTHON_OK%"=="1" (
    call :start_frontend "compliance-dashboard"  "4200"
    call :start_frontend "data-principal-portal" "4300"
) else (
    call :log "[SKIP]  Python not available - cannot serve static frontend"
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
call :log "  Auth       http://localhost:8001/swagger-ui.html"
call :log "  Consent    http://localhost:8002/swagger-ui.html"
call :log "  Rights     http://localhost:8003/swagger-ui.html"
call :log "  Breach     http://localhost:8004/swagger-ui.html"
call :log "  Tenant     http://localhost:8007/swagger-ui.html"
call :log "  Audit      http://localhost:8006/swagger-ui.html"
call :log "  Gateway    http://localhost:8080"
call :log "  Dashboard  http://localhost:4200"
call :log "  Portal     http://localhost:4300"
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
:: :start_jar  <svc-name>  <port>
:: Looks for "<svc>.jar" first, then any "<svc>-*.jar" in JARS_DIR
:: (no build step - these are the artifacts already produced by Jenkins)
:: ============================================================
:start_jar
set "SVC=%~1"
set "PORT=%~2"
set "SVC_LOG=%LOGS%\%SVC%.log"

call :check_port %PORT%
if "!PORT_OPEN!"=="1" (
    call :log "[SKIP]  %SVC% - port %PORT% already in use"
    set /a STARTED_COUNT+=1
    goto :eof
)

set "JAR="
if exist "%JARS_DIR%\%SVC%.jar" set "JAR=%JARS_DIR%\%SVC%.jar"
if not defined JAR (
    for /f "delims=" %%f in ('dir /b "%JARS_DIR%\%SVC%-*.jar" 2^>nul') do if not defined JAR set "JAR=%JARS_DIR%\%%f"
)

if not defined JAR (
    call :log "[SKIP]  %SVC% - no JAR found in %JARS_DIR%"
    goto :eof
)

call :log "[START] %SVC% -^> !JAR!"
echo. > "%SVC_LOG%"
start "DS-%SVC%" /MIN cmd /c java -jar "!JAR!" --server.port=%PORT% ^>^"%SVC_LOG%" 2^>^&1

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
set "SVC_DIR=%PY_DIR%\%SVC%"
set "SVC_LOG=%LOGS%\%SVC%-py.log"
set "VENV=%SVC_DIR%\.venv"

if not exist "%SVC_DIR%" (
    call :log "[SKIP]  %SVC% - dir not found: %SVC_DIR%"
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
    call :log "        Creating venv..."
    python -m venv "%VENV%" >> "%SVC_LOG%" 2>&1
)
if exist "%SVC_DIR%\requirements.txt" (
    call :log "        Installing requirements..."
    "%VENV%\Scripts\pip.exe" install -r "%SVC_DIR%\requirements.txt" -q >> "%SVC_LOG%" 2>&1
)

start "DS-%SVC%-py" /MIN cmd /c "cd /d "%SVC_DIR%" && "%VENV%\Scripts\uvicorn.exe" %APP% --host 0.0.0.0 --port %PORT% >> "%SVC_LOG%" 2>&1"

set "WAIT_PORT=%PORT%"
set "WAIT_SVC=%SVC%"
call :wait_healthy
goto :eof

:: ============================================================
:: :start_frontend  <app-name>  <port>
:: Serves a prebuilt Angular dist folder as static files via
:: Python's built-in http.server (simplest option, no extra installs)
:: ============================================================
:start_frontend
set "APPN=%~1"
set "PORT=%~2"
set "APP_DIR=%FE_DIR%\%APPN%"
set "APP_LOG=%LOGS%\%APPN%.log"

if not exist "%APP_DIR%\index.html" (
    call :log "[SKIP]  %APPN% - index.html not found in %APP_DIR%"
    goto :eof
)

call :check_port %PORT%
if "!PORT_OPEN!"=="1" (
    call :log "[SKIP]  %APPN% - port %PORT% already in use"
    set /a STARTED_COUNT+=1
    goto :eof
)

call :log "[STATIC] %APPN% -^> %APP_DIR% on :%PORT%"
start "DS-%APPN%" /MIN cmd /c "cd /d "%APP_DIR%" && python -m http.server %PORT% >> "%APP_LOG%" 2>&1"
set /a STARTED_COUNT+=1
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