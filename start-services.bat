@echo off
setlocal EnableDelayedExpansion

:: ============================================================
:: DataShield India - START ONLY
:: Reads a build-manifest.txt produced by build-all.bat and
:: launches everything that built successfully.
::
:: Usage:
::   start-services.bat            -> uses most recent build
::   start-services.bat <folder>   -> uses a specific build folder
:: ============================================================

set "ROOT=%~dp0"
set "LOGS=%ROOT%logs"
set "HEALTH_TIMEOUT=120"
set "STARTED_COUNT=0"
set "FAILED_COUNT=0"
set "SKIPPED_COUNT=0"
set "FAILED_LIST="

:: ── Resolve which build to use ────────────────────────────────
if not "%~1"=="" (
    set "BUILD_DIR=%~1"
) else (
    if not exist "%LOGS%\latest-build.txt" (
        echo [ERROR] No latest-build.txt found in %LOGS%
        echo         Run build-all.bat first, or pass a build folder as argument.
        pause
        exit /b 1
    )
    set /p BUILD_DIR=<"%LOGS%\latest-build.txt"
)

set "MANIFEST=%BUILD_DIR%\build-manifest.txt"
if not exist "%MANIFEST%" (
    echo [ERROR] Manifest not found: %MANIFEST%
    echo         Run build-all.bat first.
    pause
    exit /b 1
)

set "RUN_LOG=%BUILD_DIR%\run.log"
type nul > "%RUN_LOG%"

curl --version >nul 2>&1
if errorlevel 1 (set "CURL_OK=0") else (set "CURL_OK=1")

call :log "=================================================="
call :log " DataShield India - START SERVICES"
call :log " Using build: %BUILD_DIR%"
call :log "=================================================="
call :log ""

:: ── Port table (must match build-all.bat's service list) ─────
set "PORT_tenant-service=8007"
set "PORT_auth-service=8001"
set "PORT_config-service=8026"
set "PORT_consent-service=8002"
set "PORT_rights-service=8003"
set "PORT_breach-service=8004"
set "PORT_audit-service=8006"
set "PORT_notification-service=8005"
set "PORT_dpbi-service=8025"
set "PORT_vendor-service=8010"
set "PORT_policy-service=8009"
set "PORT_retention-service=8011"
set "PORT_grievance-service=8012"
set "PORT_workflow-service=8008"
set "PORT_connector-service=8022"
set "PORT_webhook-service=8023"
set "PORT_siem-service=8024"
set "PORT_analytics-service=8013"
set "PORT_report-service=8014"
set "PORT_search-service=8027"
set "PORT_data-discovery-service=8015"
set "PORT_data-classification-service=8016"
set "PORT_data-lineage-service=8017"
set "PORT_pii-detection-service=8019"
set "PORT_ai-analysis-service=8018"
set "PORT_anomaly-detection-service=8021"
set "PORT_risk-scoring-service=8020"
:: Python services (NOTE: these intentionally share ports with their
:: Java counterparts above - only one of each pair will actually bind)
set "PORT_pii-detection=8019"
set "PORT_ai-analysis=8018"
set "PORT_anomaly-detection=8021"
set "PORT_risk-scoring=8020"
set "PORT_frontend=4200"

:: ── Walk the manifest ──────────────────────────────────────────
for /f "usebackq tokens=1,2,3,4 delims=|" %%A in ("%MANIFEST%") do (
    call :handle_entry "%%A" "%%B" "%%C" "%%D"
)

:: ── Summary ────────────────────────────────────────────────────
call :log ""
call :log "=================================================="
call :log " START SUMMARY"
call :log "  Started : !STARTED_COUNT!"
call :log "  Failed  : !FAILED_COUNT!"
call :log "  Skipped (not built) : !SKIPPED_COUNT!"
if not "!FAILED_LIST!"=="" call :log "  Failed to start: !FAILED_LIST!"
call :log "=================================================="

echo.
if "!FAILED_COUNT!" GTR "0" (
    echo  [!] !FAILED_COUNT! service(s^) failed to start: !FAILED_LIST!
    echo      Check logs under: %BUILD_DIR%
) else (
    echo  [OK] All !STARTED_COUNT! built components started.
)
echo.
pause
goto :eof


:: ============================================================
:: :handle_entry  <TYPE> <name> <status> <extra>
:: ============================================================
:handle_entry
set "TYPE=%~1"
set "NAME=%~2"
set "STATUS=%~3"
set "EXTRA=%~4"

if "%TYPE%"=="ROOT" goto :eof

if not "%STATUS%"=="SUCCESS" (
    call :log "[SKIP]  %NAME% - build status was %STATUS%, not starting"
    set /a SKIPPED_COUNT+=1
    goto :eof
)

if "%TYPE%"=="SERVICE"  call :start_jar "%NAME%" "%EXTRA%"
if "%TYPE%"=="PYTHON"   call :start_python "%NAME%"
if "%TYPE%"=="FRONTEND" call :start_frontend "%EXTRA%"
goto :eof


:: ============================================================
:: :start_jar  <svc-name>  <jar-path>
:: ============================================================
:start_jar
set "SVC=%~1"
set "JAR=%~2"
call set "PORT=%%PORT_%SVC%%%"

if "%PORT%"=="" (
    call :log "[ERROR] %SVC% - no port configured, skipping"
    set /a FAILED_COUNT+=1
    set "FAILED_LIST=!FAILED_LIST! %SVC%"
    goto :eof
)

call :check_port %PORT%
if "!PORT_OPEN!"=="1" (
    call :log "[SKIP]  %SVC% - port %PORT% already in use (assuming running)"
    set /a STARTED_COUNT+=1
    goto :eof
)

set "SVC_LOG=%BUILD_DIR%\run-%SVC%.log"
echo DataShield Run Log - %SVC% - %DATE% %TIME% > "%SVC_LOG%"

set "RUNNER=%BUILD_DIR%\runner-%SVC%.bat"
> "%RUNNER%" echo @echo off
>> "%RUNNER%" echo java -jar "%JAR%" ^>^> "%SVC_LOG%" 2^>^&1

start "DS-%SVC%" /MIN "%RUNNER%"
call :log "[START] %SVC% (port %PORT%) launched"

set "WAIT_PORT=%PORT%"
set "WAIT_SVC=%SVC%"
call :wait_healthy
goto :eof


:: ============================================================
:: :start_python  <svc-name>   (module fixed: app.main:app)
:: ============================================================
:start_python
set "SVC=%~1"
call set "PORT=%%PORT_%SVC%%%"
set "SVC_DIR=%ROOT%services\%SVC%"
set "VENV=%SVC_DIR%\.venv"

if "%PORT%"=="" (
    call :log "[ERROR] %SVC% - no port configured, skipping"
    set /a FAILED_COUNT+=1
    set "FAILED_LIST=!FAILED_LIST! %SVC%"
    goto :eof
)

if not exist "%VENV%\Scripts\uvicorn.exe" (
    call :log "[ERROR] %SVC% - venv/uvicorn not found, was build-all.bat run?"
    set /a FAILED_COUNT+=1
    set "FAILED_LIST=!FAILED_LIST! %SVC%"
    goto :eof
)

call :check_port %PORT%
if "!PORT_OPEN!"=="1" (
    call :log "[SKIP]  %SVC% (Python) - port %PORT% already in use"
    set /a STARTED_COUNT+=1
    goto :eof
)

set "SVC_LOG=%BUILD_DIR%\run-%SVC%-py.log"
echo DataShield Python Run Log - %SVC% - %DATE% %TIME% > "%SVC_LOG%"

set "RUNNER=%BUILD_DIR%\runner-%SVC%-py.bat"
> "%RUNNER%" echo @echo off
>> "%RUNNER%" echo cd /d "%SVC_DIR%"
>> "%RUNNER%" echo "%VENV%\Scripts\uvicorn.exe" app.main:app --host 0.0.0.0 --port %PORT% --log-level info ^>^> "%SVC_LOG%" 2^>^&1

start "DS-%SVC%-py" /MIN "%RUNNER%"
call :log "[START] %SVC% (Python, port %PORT%) launched"

set "WAIT_PORT=%PORT%"
set "WAIT_SVC=%SVC%"
call :wait_healthy
goto :eof


:: ============================================================
:: :start_frontend  <dist-dir>
:: Serves the built Angular dist folder as static files via
:: npx http-server (installed on the fly if missing).
:: ============================================================
:start_frontend
set "DIST=%~1"
set "PORT=%PORT_frontend%"

if "%DIST%"=="" (
    call :log "[ERROR] frontend - no dist path recorded in manifest"
    set /a FAILED_COUNT+=1
    set "FAILED_LIST=!FAILED_LIST! frontend"
    goto :eof
)

call :check_port %PORT%
if "!PORT_OPEN!"=="1" (
    call :log "[SKIP]  frontend - port %PORT% already in use"
    set /a STARTED_COUNT+=1
    goto :eof
)

set "SVC_LOG=%BUILD_DIR%\run-frontend.log"
echo DataShield Frontend Run Log - %DATE% %TIME% > "%SVC_LOG%"

set "RUNNER=%BUILD_DIR%\runner-frontend.bat"
> "%RUNNER%" echo @echo off
>> "%RUNNER%" echo npx --yes http-server "%DIST%" -p %PORT% -c-1 ^>^> "%SVC_LOG%" 2^>^&1

start "DS-frontend" /MIN "%RUNNER%"
call :log "[START] frontend (port %PORT%) launched, serving %DIST%"

set "WAIT_PORT=%PORT%"
set "WAIT_SVC=frontend"
call :wait_healthy
goto :eof


:: ============================================================
:: :wait_healthy  (reads WAIT_PORT, WAIT_SVC)
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
curl -sf "http://localhost:%WP%/" >nul 2>&1
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
call :log "        Check log: %BUILD_DIR%\run-%WS%.log"
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
:: :log  <message>
:: ============================================================
:log
echo [%TIME%] %~1
echo [%DATE% %TIME%] %~1 >> "%RUN_LOG%"
goto :eof
