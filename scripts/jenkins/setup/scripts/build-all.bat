@echo off
setlocal EnableDelayedExpansion

:: ============================================================
:: DataShield India - BUILD (all services, or just one)
::
:: Usage:
::   build-all.bat                  -> builds root + every service + frontend
::   build-all.bat consent-service  -> builds ONLY consent-service
::   build-all.bat frontend         -> builds ONLY the Angular frontend
::   build-all.bat pii-detection    -> builds ONLY that Python service
::
:: Every run gets its own timestamped log folder:
::   logs\build_YYYY-MM-DD_HH-MM-SS\
:: ============================================================

set "ROOT=%~dp0.."
set "SERVICES=%ROOT%\services"
set "FRONTEND=%ROOT%\frontend"
set "TARGET=%~1"
set "FOUND=0"

for /f "usebackq delims=" %%T in (`powershell -NoProfile -Command "Get-Date -Format yyyy-MM-dd_HH-mm-ss"`) do set "STAMP=%%T"
set "LOGS=%ROOT%\logs\build_%STAMP%"
mkdir "%LOGS%" 2>nul

set "SUMMARY=%LOGS%\summary.log"
set "FAIL_COUNT=0"
set "FAIL_LIST="

set "MVN=%ROOT%\tools\apache-maven-3.9.16\bin\mvn.cmd"
if not exist "%MVN%" set "MVN=mvn"

call :log "=================================================="
if "%TARGET%"=="" (
    call :log " DataShield India - BUILD ALL - %DATE% %TIME%"
) else (
    call :log " DataShield India - BUILD ONE (%TARGET%) - %DATE% %TIME%"
)
call :log " Log folder: %LOGS%"
call :log "=================================================="
call :log ""

:: ── Root build (parent POM + shared libs) - only in full-build mode ──
if "%TARGET%"=="" (
    call :log "[ROOT BUILD] mvn clean install -DskipTests ..."
    set "ROOT_LOG=%LOGS%\00-root-build.log"
    echo Root build - %DATE% %TIME% > "%ROOT_LOG%"
    pushd "%ROOT%"
    "%MVN%" clean install -DskipTests >> "%ROOT_LOG%" 2>&1
    set "ROOT_ERR=!errorlevel!"
    popd

    if !ROOT_ERR! NEQ 0 (
        call :log "[ERROR] Root build FAILED - see %ROOT_LOG%"
        call :tail_log "%ROOT_LOG%" 20
        set /a FAIL_COUNT+=1
        set "FAIL_LIST=!FAIL_LIST! root-build"
        goto :frontend
    )
    call :log "[OK]    Root build complete"
) else (
    call :log "[SKIP]  root build (single-service mode - run with no args to refresh shared libs)"
)
call :log ""

:: ── Microservices (only runs if TARGET is empty or matches) ──
call :maybe_build_service "tenant-service"
call :maybe_build_service "auth-service"
call :maybe_build_service "config-service"
call :maybe_build_service "consent-service"
call :maybe_build_service "rights-service"
call :maybe_build_service "breach-service"
call :maybe_build_service "audit-service"
call :maybe_build_service "notification-service"
call :maybe_build_service "dpbi-service"
call :maybe_build_service "vendor-service"
call :maybe_build_service "policy-service"
call :maybe_build_service "retention-service"
call :maybe_build_service "grievance-service"
call :maybe_build_service "workflow-service"
call :maybe_build_service "connector-service"
call :maybe_build_service "webhook-service"
call :maybe_build_service "siem-service"
call :maybe_build_service "analytics-service"
call :maybe_build_service "report-service"
call :maybe_build_service "search-service"
call :maybe_build_service "data-discovery-service"
call :maybe_build_service "data-classification-service"
call :maybe_build_service "data-lineage-service"
call :maybe_build_service "pii-detection-service"
call :maybe_build_service "ai-analysis-service"
call :maybe_build_service "anomaly-detection-service"
call :maybe_build_service "risk-scoring-service"

:: ── Python ML services ────────────────────────────────────────
call :maybe_build_python "pii-detection"
call :maybe_build_python "ai-analysis"
call :maybe_build_python "anomaly-detection"
call :maybe_build_python "risk-scoring"

:: ── Angular frontend ──────────────────────────────────────────
:frontend
if not "%TARGET%"=="" if /i not "%TARGET%"=="frontend" goto :summary

call :log ""
call :log "========== FRONTEND (Angular) =========="
set "FOUND=1"
if not exist "%FRONTEND%" (
    call :log "[SKIP]  frontend - directory not found: %FRONTEND%"
    goto :summary
)
set "FE_LOG=%LOGS%\90-frontend.log"
echo Frontend build - %DATE% %TIME% > "%FE_LOG%"
pushd "%FRONTEND%"
call npm ci >> "%FE_LOG%" 2>&1
set "FE_ERR=!errorlevel!"
if !FE_ERR! EQU 0 (
    call npm run build >> "%FE_LOG%" 2>&1
    set "FE_ERR=!errorlevel!"
)
popd

if !FE_ERR! NEQ 0 (
    call :log "[ERROR] frontend build FAILED - see %FE_LOG%"
    call :tail_log "%FE_LOG%" 20
    set /a FAIL_COUNT+=1
    set "FAIL_LIST=!FAIL_LIST! frontend"
) else (
    call :log "[OK]    frontend build complete"
)

:: ── Summary ───────────────────────────────────────────────────
:summary
if not "%TARGET%"=="" if !FOUND! EQU 0 (
    call :log "[ERROR] Unknown service name: %TARGET%"
    set /a FAIL_COUNT+=1
    set "FAIL_LIST=!FAIL_LIST! unknown-target:%TARGET%"
)

call :log ""
call :log "=================================================="
call :log " BUILD SUMMARY - %DATE% %TIME%"
call :log "  Failed: !FAIL_COUNT!"
if not "!FAIL_LIST!"=="" call :log "  Failed items:!FAIL_LIST!"
call :log "  Logs: %LOGS%"
call :log "=================================================="

if !FAIL_COUNT! GTR 0 (
    echo BUILD FAILED - see %LOGS%
    exit /b 1
)
echo BUILD OK - see %LOGS%
exit /b 0

:: ============================================================
:maybe_build_service  <svc-name>  -> builds only if TARGET matches/blank
:: ============================================================
:maybe_build_service
set "MSVC=%~1"
if "%TARGET%"=="" (
    call :build_service "%MSVC%"
    goto :eof
)
if /i "%TARGET%"=="%MSVC%" (
    set "FOUND=1"
    call :build_service "%MSVC%"
)
goto :eof

:: ============================================================
:maybe_build_python  <svc-name>
:: ============================================================
:maybe_build_python
set "MSVC=%~1"
if "%TARGET%"=="" (
    call :build_python "%MSVC%"
    goto :eof
)
if /i "%TARGET%"=="%MSVC%" (
    set "FOUND=1"
    call :build_python "%MSVC%"
)
goto :eof

:: ============================================================
:build_service  <svc-name>
:: ============================================================
:build_service
set "SVC=%~1"
set "SVC_DIR=%SERVICES%\%SVC%"
set "SVC_LOG=%LOGS%\%SVC%.log"

if not exist "%SVC_DIR%" (
    call :log "[SKIP]  %SVC% - directory not found"
    goto :eof
)

call :log "[BUILD] %SVC% ..."
echo %SVC% build - %DATE% %TIME% > "%SVC_LOG%"
pushd "%SVC_DIR%"
"%MVN%" clean package -DskipTests >> "%SVC_LOG%" 2>&1
set "BERR=!errorlevel!"
popd

if !BERR! NEQ 0 (
    call :log "[ERROR] %SVC% - Maven build FAILED (exit !BERR!)"
    call :log "        Log: %SVC_LOG%"
    call :log "        --- last 15 lines (look for compilation errors) ---"
    call :tail_log "%SVC_LOG%" 15
    set /a FAIL_COUNT+=1
    set "FAIL_LIST=!FAIL_LIST! %SVC%"
) else (
    call :log "[OK]    %SVC% build complete"
)
goto :eof

:: ============================================================
:build_python  <svc-name>   (module app.main:app assumed)
:: ============================================================
:build_python
set "SVC=%~1"
set "SVC_DIR=%SERVICES%\%SVC%"
set "SVC_LOG=%LOGS%\%SVC%-py.log"
set "VENV=%SVC_DIR%\.venv"

if not exist "%SVC_DIR%" (
    call :log "[SKIP]  %SVC% (Python) - directory not found"
    goto :eof
)

call :log "[BUILD] %SVC% (Python) ..."
echo %SVC% python build - %DATE% %TIME% > "%SVC_LOG%"

if not exist "%VENV%\Scripts\python.exe" (
    python -m venv "%VENV%" >> "%SVC_LOG%" 2>&1
    if errorlevel 1 (
        call :log "[ERROR] %SVC% - venv creation failed"
        set /a FAIL_COUNT+=1
        set "FAIL_LIST=!FAIL_LIST! %SVC%"
        goto :eof
    )
)

if exist "%SVC_DIR%\requirements.txt" (
    "%VENV%\Scripts\pip.exe" install -r "%SVC_DIR%\requirements.txt" -q >> "%SVC_LOG%" 2>&1
    if errorlevel 1 (
        call :log "[ERROR] %SVC% - pip install FAILED"
        call :tail_log "%SVC_LOG%" 15
        set /a FAIL_COUNT+=1
        set "FAIL_LIST=!FAIL_LIST! %SVC%"
        goto :eof
    )
)
call :log "[OK]    %SVC% (Python) ready"
goto :eof

:: ============================================================
:tail_log  <file>  <n-lines>
:: ============================================================
:tail_log
set "TL_FILE=%~1"
set "TL_N=%~2"
for /f "usebackq delims=" %%L in (`powershell -NoProfile -Command "Get-Content -LiteralPath '%TL_FILE%' -Tail %TL_N% -ErrorAction SilentlyContinue"`) do (
    call :log "        %%L"
)
goto :eof

:: ============================================================
:log  <message>
:: ============================================================
:log
echo [%TIME%] %~1
echo [%DATE% %TIME%] %~1 >> "%SUMMARY%"
goto :eof
