@echo off
setlocal EnableDelayedExpansion

REM ==========================================================
REM DataShield India - Start Services
REM Starts JARs produced by the build in dependency order.
REM ==========================================================

call "%~dp0config.bat"
call "%~dp0logger.bat" :InitLogger

set RUN_LOG=%LOG_DIR%\runtime
if not exist "%RUN_LOG%" mkdir "%RUN_LOG%"

call "%~dp0logger.bat" :Section "Starting Services"

REM ---- Infrastructure order ----
call :Start config-service
call :Wait 5
call :Start auth-service
call :Wait 5
call :Start tenant-service
call :Wait 5

REM ---- Remaining services ----
for %%S in (
consent-service
rights-service
breach-service
dpbi-service
notification-service
audit-service
vendor-service
policy-service
workflow-service
connector-service
search-service
webhook-service
siem-service
analytics-service
report-service
retention-service
grievance-service
data-classification-service
data-discovery-service
data-lineage-service
pii-detection-service
ai-analysis-service
anomaly-detection-service
risk-scoring-service
) do (
    call :Start %%S
)

echo.
echo ============================================
echo All start commands have been issued.
echo Runtime logs: %RUN_LOG%
echo ============================================
exit /b

:Start
set SVC=%~1
set JAR=

for /f "delims=" %%F in ('dir /b /s "%SERVICES%\%SVC%\target\*.jar" 2^>nul ^| findstr /v "original"') do (
    if not defined JAR set JAR=%%F
)

if not defined JAR (
    call "%~dp0logger.bat" :Warn "%SVC% jar not found."
    exit /b
)

call "%~dp0logger.bat" :Info "Starting %SVC%"
start "%SVC%" cmd /c java -jar "!JAR!" ^> "%RUN_LOG%\%SVC%.log" 2^>^&1
exit /b

:Wait
timeout /t %~1 /nobreak >nul
exit /b
