@echo off
setlocal EnableDelayedExpansion

REM ==========================================================
REM DataShield India - Stop Services
REM Stops Java services started by start-services.bat
REM ==========================================================

call "%~dp0config.bat"
call "%~dp0logger.bat" :InitLogger

call "%~dp0logger.bat" :Section "Stopping DataShield Services"

set STOPPED=0

for %%S in (
auth-service
consent-service
rights-service
breach-service
dpbi-service
notification-service
audit-service
tenant-service
vendor-service
policy-service
workflow-service
config-service
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
    call :StopService %%S
)

echo.
echo ==========================================
echo Total Services Stopped : %STOPPED%
echo ==========================================
pause
exit /b

:StopService

set SERVICE=%~1

call "%~dp0logger.bat" :Info "Stopping %SERVICE%"

for /f "tokens=2" %%P in ('wmic process where "CommandLine like '%%!SERVICE!%%'" get ProcessId ^| findstr [0-9]') do (
    taskkill /PID %%P /F >nul 2>&1
    if not errorlevel 1 (
        set /a STOPPED+=1
        call "%~dp0logger.bat" :Success "%SERVICE% stopped (PID %%P)"
    )
)

exit /b
