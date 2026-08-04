@echo off
setlocal EnableDelayedExpansion

:: ============================================================
:: DataShield India - BUILD ONLY (Maven + Angular + Python)
:: PARALLEL VERSION: Java services build in waves of MAX_PARALLEL.
:: Frontend + Python builds run in the background the whole time.
:: Every run creates a fresh timestamped folder under logs\
:: Produces build-manifest.txt that start-services.bat consumes.
:: ============================================================

set "ROOT=%~dp0"
set "LOGS=%ROOT%logs"
set "SERVICES=%ROOT%services"
set "FRONTEND_DIR=%ROOT%frontend"

:: How many Maven builds to run at the same time. Each one can use
:: roughly 500MB-1GB RAM. Raise this if you have a strong CPU/RAM,
:: lower it if the machine chokes.
set "MAX_PARALLEL=4"

set "MVN=%ROOT%tools\apache-maven-3.9.16\bin\mvn.cmd"
if not exist "%MVN%" set "MVN=mvn"

set "OK_COUNT=0"
set "FAIL_COUNT=0"
set "SKIP_COUNT=0"

:: ── Timestamped build folder ──────────────────────────────────
if not exist "%LOGS%" mkdir "%LOGS%"
for /f "usebackq delims=" %%T in (`powershell -NoProfile -Command "Get-Date -Format yyyyMMdd_HHmmss"`) do set "TS=%%T"
set "BUILD_DIR=%LOGS%\build_%TS%"
mkdir "%BUILD_DIR%"
set "MANIFEST=%BUILD_DIR%\build-manifest.txt"
set "SUMMARY=%BUILD_DIR%\build-summary.txt"
type nul > "%MANIFEST%"
type nul > "%SUMMARY%"

call :log "=================================================="
call :log " DataShield India - BUILD  (%TS%)  [parallel x%MAX_PARALLEL%]"
call :log " Build folder: %BUILD_DIR%"
call :log "=================================================="
call :log ""

:: ── Pre-flight ───────────────────────────────────────────────
set "JAVA_OK=1" & set "MVN_OK=1" & set "NODE_OK=1" & set "PY_OK=1"

java -version >nul 2>&1
if errorlevel 1 (call :log "[ERROR] Java not found in PATH" & set "JAVA_OK=0")

call "%MVN%" -version >nul 2>&1
if errorlevel 1 (call :log "[ERROR] Maven not found: %MVN%" & set "MVN_OK=0")

node --version >nul 2>&1
if errorlevel 1 (call :log "[WARN]  Node/npm not found - frontend build will be skipped" & set "NODE_OK=0")

set "PYTHON=C:\Users\NIC\AppData\Local\Programs\Python\Python311\python.exe"

if not exist "%PYTHON%" (
    call :log "[ERROR] Python not found: %PYTHON%"
    set "PY_OK=0"
)

if "!JAVA_OK!"=="0" if "!MVN_OK!"=="0" goto :finalize
if "!MVN_OK!"=="0" goto :finalize
call :log ""

:: ── Launch Frontend + Python builds in the background NOW ────
:: (they don't depend on Maven, so let them run while Java builds)
call :log "========== Launching Frontend + Python in background =========="
set "FRONTEND_LAUNCHED=0"
if "!NODE_OK!"=="1" if exist "%FRONTEND_DIR%" (
    call :launch_frontend_async
    set "FRONTEND_LAUNCHED=1"
) else (
    call :log "[SKIP]  Frontend - node/npm not available or dir missing"
    echo FRONTEND^|frontend^|SKIPPED^| >> "%MANIFEST%"
    set /a SKIP_COUNT+=1
)

set "PY_LAUNCHED="
if "!PY_OK!"=="1" (
    for %%P in (pii-detection ai-analysis anomaly-detection risk-scoring) do (
        call :launch_python_async %%P
        set "PY_LAUNCHED=!PY_LAUNCHED! %%P"
    )
) else (
    call :log "[SKIP]  All Python services - python not available"
)
call :log ""

:: ── Root build (parent POM + shared libs into local .m2) ─────
:: Must be sequential and must finish before Java services build.
call :log "[ROOT BUILD] mvn clean install -DskipTests ..."
set "ROOT_LOG=%BUILD_DIR%\root-build.log"
pushd "%ROOT%"
call "%MVN%" clean install -DskipTests > "%ROOT_LOG%" 2>&1
set "ROOT_ERR=!errorlevel!"
popd

if !ROOT_ERR! NEQ 0 (
    call :log "[ERROR] Root build FAILED - see %ROOT_LOG%"
    call :extract_maven_errors "%ROOT_LOG%" "ROOT-BUILD"
    echo ROOT^|ROOT^|FAILED^| >> "%MANIFEST%"
    call :log "[ABORT] Cannot proceed with Java services - fix root build errors first"
    goto :collect_background
)
call :log "[OK]    Root build complete"
echo ROOT^|ROOT^|SUCCESS^| >> "%MANIFEST%"
call :log ""

:: ── Java microservices - parallel waves ───────────────────────
call :log "========== Building Java Microservices (parallel x%MAX_PARALLEL%) =========="
set "WAVE_COUNT=0"
set "PENDING="

for %%S in (tenant-service auth-service config-service consent-service rights-service breach-service audit-service notification-service dpbi-service vendor-service policy-service retention-service grievance-service workflow-service connector-service webhook-service siem-service analytics-service report-service search-service data-discovery-service data-classification-service data-lineage-service pii-detection-service ai-analysis-service anomaly-detection-service risk-scoring-service) do (
    if exist "%SERVICES%\%%S" (
        call :launch_build_async %%S
        set /a WAVE_COUNT+=1
        set "PENDING=!PENDING! %%S"
    ) else (
        call :log "[SKIP]  %%S - directory not found: %SERVICES%\%%S"
        echo SERVICE^|%%S^|SKIPPED^| >> "%MANIFEST%"
        set /a SKIP_COUNT+=1
    )
    if !WAVE_COUNT! GEQ %MAX_PARALLEL% (
        call :wait_wave "!PENDING!"
        set "WAVE_COUNT=0"
        set "PENDING="
    )
)
if not "!PENDING!"=="" call :wait_wave "!PENDING!"
call :log ""

:: ── Collect frontend + python results (launched way above) ───
:collect_background
call :log "========== Collecting Frontend + Python build results =========="
if "!FRONTEND_LAUNCHED!"=="1" (
    call :wait_single "%BUILD_DIR%\frontend.done" 900
    call :collect_frontend
)
if not "!PY_LAUNCHED!"=="" call :wait_python "!PY_LAUNCHED!"

:: ── Finalize ───────────────────────────────────────────────────
:finalize
> "%LOGS%\latest-build.txt" echo %BUILD_DIR%

call :log ""
call :log "=================================================="
call :log " BUILD SUMMARY"
call :log "  Succeeded : !OK_COUNT!"
call :log "  Failed    : !FAIL_COUNT!"
call :log "  Skipped   : !SKIP_COUNT!"
call :log "  Manifest  : %MANIFEST%"
call :log "  Details   : %SUMMARY%  (compile errors, if any)"
call :log "=================================================="

echo.
if "!FAIL_COUNT!" GTR "0" (
    echo  [!] !FAIL_COUNT! build(s^) FAILED. Open this file for exact errors:
    echo      %SUMMARY%
) else (
    echo  [OK] All available components built successfully.
)
echo.
echo  Next step: run start-services.bat to launch everything that built OK.
echo.
pause
goto :eof


:: ============================================================
:: :launch_build_async  <svc-name>
:: Fires a background build; does NOT wait. Writes exit code to
:: %BUILD_DIR%\<svc>.done when finished.
:: ============================================================
:launch_build_async
set "ASVC=%~1"
set "ASVC_DIR=%SERVICES%\%ASVC%"
set "ALOG=%BUILD_DIR%\%ASVC%.log"
set "ADONE=%BUILD_DIR%\%ASVC%.done"
set "ARUNNER=%BUILD_DIR%\runner-build-%ASVC%.bat"

call :log "[BUILD] %ASVC% (parallel) ..."

> "%ARUNNER%" echo @echo off
>> "%ARUNNER%" echo cd /d "%ASVC_DIR%"
>> "%ARUNNER%" echo call "%MVN%" clean package -DskipTests ^> "%ALOG%" 2^>^&1
>> "%ARUNNER%" echo echo %%ERRORLEVEL%% ^> "%ADONE%"

start "DS-BUILD-%ASVC%" /MIN "%ARUNNER%"
goto :eof


:: ============================================================
:: :wait_wave  <"svc1 svc2 svc3...">  [timeout-seconds, default 1800]
:: Polls until all .done markers for the given services exist,
:: then collects each result (jar lookup + manifest + log).
:: ============================================================
:wait_wave
set "WAVE_LIST=%~1"
set "WAVE_TIMEOUT=%~2"
if "%WAVE_TIMEOUT%"=="" set "WAVE_TIMEOUT=1800"
set "WAVE_ELAPSED=0"

:ww_loop
set "ALL_DONE=1"
for %%S in (%WAVE_LIST%) do (
    if not exist "%BUILD_DIR%\%%S.done" set "ALL_DONE=0"
)
if "!ALL_DONE!"=="1" goto :ww_collect

ping -n 4 127.0.0.1 >nul 2>&1
set /a WAVE_ELAPSED+=3
if !WAVE_ELAPSED! LSS %WAVE_TIMEOUT% goto :ww_loop
call :log "[WARN]  Wave timeout (%WAVE_TIMEOUT%s) reached - some builds may still be running"

:ww_collect
for %%S in (%WAVE_LIST%) do call :collect_result %%S
goto :eof


:: ============================================================
:: :collect_result  <svc-name>
:: Reads a finished (or timed-out) build's outcome and writes it
:: to the manifest. Called only from :wait_wave.
:: ============================================================
:collect_result
set "CSVC=%~1"
set "CSVC_DIR=%SERVICES%\%CSVC%"
set "CDONE=%BUILD_DIR%\%CSVC%.done"
set "CLOG=%BUILD_DIR%\%CSVC%.log"

if not exist "%CDONE%" (
    call :log "[FAIL]  %CSVC% - build did not finish in time - see %CLOG%"
    echo SERVICE^|%CSVC%^|FAILED^| >> "%MANIFEST%"
    set /a FAIL_COUNT+=1
    goto :eof
)

set /p CERR=<"%CDONE%"

if not "!CERR!"=="0" (
    call :log "[FAIL]  %CSVC% - Maven build FAILED (exit !CERR!) - see %CLOG%"
    call :extract_maven_errors "%CLOG%" "%CSVC%"
    echo SERVICE^|%CSVC%^|FAILED^| >> "%MANIFEST%"
    set /a FAIL_COUNT+=1
    goto :eof
)

set "CJAR=%CSVC_DIR%\target\%CSVC%.jar"
if not exist "!CJAR!" (
    set "CJAR="
    for /f "delims=" %%f in ('dir /b "%CSVC_DIR%\target\*.jar" 2^>nul ^| findstr /v "original"') do (
        if "!CJAR!"=="" set "CJAR=%CSVC_DIR%\target\%%f"
    )
)

if "!CJAR!"=="" (
    call :log "[FAIL]  %CSVC% - built OK but no executable JAR found in target\"
    echo SERVICE^|%CSVC%^|FAILED^| >> "%MANIFEST%"
    set /a FAIL_COUNT+=1
    goto :eof
)

call :log "[OK]    %CSVC% -> !CJAR!"
echo SERVICE^|%CSVC%^|SUCCESS^|!CJAR! >> "%MANIFEST%"
set /a OK_COUNT+=1
goto :eof


:: ============================================================
:: :launch_frontend_async
:: Fires npm install (if needed) + npm run build in background.
:: ============================================================
:launch_frontend_async
set "FLOG=%BUILD_DIR%\frontend.log"
set "FDONE=%BUILD_DIR%\frontend.done"
set "FRUNNER=%BUILD_DIR%\runner-build-frontend.bat"

call :log "[BUILD] frontend (parallel, background) ..."

> "%FRUNNER%" echo @echo off
>> "%FRUNNER%" echo cd /d "%FRONTEND_DIR%"
>> "%FRUNNER%" echo if not exist node_modules call npm install ^>^> "%FLOG%" 2^>^&1
>> "%FRUNNER%" echo call npm run build ^>^> "%FLOG%" 2^>^&1
>> "%FRUNNER%" echo echo %%ERRORLEVEL%% ^> "%FDONE%"

start "DS-BUILD-frontend" /MIN "%FRUNNER%"
goto :eof


:: ============================================================
:: :collect_frontend
:: ============================================================
:collect_frontend
set "FLOG=%BUILD_DIR%\frontend.log"
set "FDONE=%BUILD_DIR%\frontend.done"

if not exist "%FDONE%" (
    call :log "[FAIL]  frontend - build did not finish in time - see %FLOG%"
    echo FRONTEND^|frontend^|FAILED^| >> "%MANIFEST%"
    set /a FAIL_COUNT+=1
    goto :eof
)

set /p FERR=<"%FDONE%"
if not "!FERR!"=="0" (
    call :log "[FAIL]  frontend build FAILED (exit !FERR!) - see %FLOG%"
    call :extract_ng_errors "%FLOG%" "frontend"
    echo FRONTEND^|frontend^|FAILED^| >> "%MANIFEST%"
    set /a FAIL_COUNT+=1
    goto :eof
)

set "DIST_DIR="
for /f "delims=" %%d in ('dir /b /s "%FRONTEND_DIR%\dist\index.html" 2^>nul') do (
    if "!DIST_DIR!"=="" for %%p in ("%%d") do set "DIST_DIR=%%~dpp"
)

if "!DIST_DIR!"=="" (
    call :log "[FAIL]  frontend - build ran but no dist\...\index.html found"
    echo FRONTEND^|frontend^|FAILED^| >> "%MANIFEST%"
    set /a FAIL_COUNT+=1
    goto :eof
)

call :log "[OK]    frontend -> !DIST_DIR!"
echo FRONTEND^|frontend^|SUCCESS^|!DIST_DIR! >> "%MANIFEST%"
set /a OK_COUNT+=1
goto :eof


:: ============================================================
:: :launch_python_async  <svc-name>   (module assumed app.main:app)
:: Fires venv-create + pip install + syntax check in background.
:: ============================================================
:launch_python_async
set "PSVC=%~1"
set "PSVC_DIR=%SERVICES%\%PSVC%"
set "PVENV=%PSVC_DIR%\.venv"
set "PLOG=%BUILD_DIR%\%PSVC%-py.log"
set "PDONE=%BUILD_DIR%\%PSVC%-py.done"
set "PRUNNER=%BUILD_DIR%\runner-build-%PSVC%-py.bat"

if not exist "%PSVC_DIR%" (
    call :log "[SKIP]  %PSVC% (Python) - directory not found: %PSVC_DIR%"
    echo PYTHON^|%PSVC%^|SKIPPED^| >> "%MANIFEST%"
    set /a SKIP_COUNT+=1
    goto :eof
)

call :log "[PY]    %PSVC% (parallel, background) ..."

> "%PRUNNER%" echo @echo off
>> "%PRUNNER%" echo if not exist "%PVENV%\Scripts\python.exe" "%PYTHON%" -m venv "%PVENV%" ^> "%PLOG%" 2^>^&1
>> "%PRUNNER%" echo if exist "%PSVC_DIR%\requirements.txt" "%PVENV%\Scripts\pip.exe" install -r "%PSVC_DIR%\requirements.txt" -q ^>^> "%PLOG%" 2^>^&1
>> "%PRUNNER%" echo "%PVENV%\Scripts\python.exe" -m compileall -q "%PSVC_DIR%\app" ^>^> "%PLOG%" 2^>^&1
>> "%PRUNNER%" echo echo %%ERRORLEVEL%% ^> "%PDONE%"

start "DS-BUILD-%PSVC%-py" /MIN "%PRUNNER%"
goto :eof


:: ============================================================
:: :wait_python  <"svc1 svc2 ...">
:: ============================================================
:wait_python
set "WP_LIST=%~1"
set "WP_TIMEOUT=900"
set "WP_ELAPSED=0"

:wp_loop
set "WP_ALL=1"
for %%S in (%WP_LIST%) do (
    if not exist "%BUILD_DIR%\%%S-py.done" set "WP_ALL=0"
)
if "!WP_ALL!"=="1" goto :wp_collect

ping -n 4 127.0.0.1 >nul 2>&1
set /a WP_ELAPSED+=3
if !WP_ELAPSED! LSS %WP_TIMEOUT% goto :wp_loop
call :log "[WARN]  Python wave timeout (%WP_TIMEOUT%s) reached"

:wp_collect
for %%S in (%WP_LIST%) do call :collect_python %%S
goto :eof


:: ============================================================
:: :collect_python  <svc-name>
:: ============================================================
:collect_python
set "CPSVC=%~1"
set "CPLOG=%BUILD_DIR%\%CPSVC%-py.log"
set "CPDONE=%BUILD_DIR%\%CPSVC%-py.done"

if not exist "%CPDONE%" (
    call :log "[FAIL]  %CPSVC% (Python) - did not finish in time - see %CPLOG%"
    echo PYTHON^|%CPSVC%^|FAILED^| >> "%MANIFEST%"
    set /a FAIL_COUNT+=1
    goto :eof
)

set /p CPERR=<"%CPDONE%"
if not "!CPERR!"=="0" (
    call :log "[FAIL]  %CPSVC% (Python) FAILED (exit !CPERR!) - see %CPLOG%"
    call :extract_py_errors "%CPLOG%" "%CPSVC%"
    echo PYTHON^|%CPSVC%^|FAILED^| >> "%MANIFEST%"
    set /a FAIL_COUNT+=1
    goto :eof
)

call :log "[OK]    %CPSVC% (Python) ready"
echo PYTHON^|%CPSVC%^|SUCCESS^| >> "%MANIFEST%"
set /a OK_COUNT+=1
goto :eof


:: ============================================================
:: :wait_single  <file>  <timeout-seconds>
:: ============================================================
:wait_single
set "WS_FILE=%~1"
set "WS_TIMEOUT=%~2"
set "WS_ELAPSED=0"

:ws_loop
if exist "%WS_FILE%" goto :eof
ping -n 4 127.0.0.1 >nul 2>&1
set /a WS_ELAPSED+=3
if !WS_ELAPSED! LSS %WS_TIMEOUT% goto :ws_loop
call :log "[WARN]  Timeout waiting for %WS_FILE%"
goto :eof


:: ============================================================
:: :extract_maven_errors  <logfile>  <label>
:: ============================================================
:extract_maven_errors
set "EM_LOG=%~1"
set "EM_LABEL=%~2"
>> "%SUMMARY%" echo ==================================================
>> "%SUMMARY%" echo %EM_LABEL%  -  %EM_LOG%
>> "%SUMMARY%" echo ==================================================
findstr /C:"[ERROR]" "%EM_LOG%" >> "%SUMMARY%" 2>nul
>> "%SUMMARY%" echo.
goto :eof


:: ============================================================
:: :extract_ng_errors  <logfile>  <label>
:: ============================================================
:extract_ng_errors
set "EN_LOG=%~1"
set "EN_LABEL=%~2"
>> "%SUMMARY%" echo ==================================================
>> "%SUMMARY%" echo %EN_LABEL%  -  %EN_LOG%
>> "%SUMMARY%" echo ==================================================
findstr /C:"ERROR in" /C:"error TS" /C:"npm ERR!" "%EN_LOG%" >> "%SUMMARY%" 2>nul
>> "%SUMMARY%" echo.
goto :eof


:: ============================================================
:: :extract_py_errors  <logfile>  <label>
:: ============================================================
:extract_py_errors
set "EP_LOG=%~1"
set "EP_LABEL=%~2"
>> "%SUMMARY%" echo ==================================================
>> "%SUMMARY%" echo %EP_LABEL%  -  %EP_LOG%
>> "%SUMMARY%" echo ==================================================
findstr /C:"SyntaxError" /C:"Error compiling" /C:"ERROR:" "%EP_LOG%" >> "%SUMMARY%" 2>nul
>> "%SUMMARY%" echo.
goto :eof


:: ============================================================
:: :log  <message>
:: ============================================================
:log
echo [%TIME%] %~1
echo [%DATE% %TIME%] %~1 >> "%BUILD_DIR%\build.log"
goto :eof