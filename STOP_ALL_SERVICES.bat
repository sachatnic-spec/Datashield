@echo off
setlocal EnableDelayedExpansion

:: =============================================================================
:: DataShield India — Stop All Services
:: Gracefully kills all DS-* titled windows, then force-kills by port if needed
:: =============================================================================

set "LOGS=%~dp0logs"
if not exist "%LOGS%" mkdir "%LOGS%"

echo [%DATE% %TIME%] Stopping all DataShield services... | tee "%LOGS%\shutdown.log"

:: Kill all windows started by START_ALL_SERVICES.bat (titled DS-*)
set "KILLED=0"
for /f "tokens=2" %%p in ('tasklist /fi "WINDOWTITLE eq DS-*" /fo csv /nh 2^>nul') do (
    set "PID=%%~p"
    if not "!PID!"=="" (
        taskkill /PID !PID! /F >nul 2>&1
        set /a KILLED+=1
    )
)

:: Also kill any java processes running our JARs
for /f "tokens=2" %%p in ('wmic process where "CommandLine like '%%datasheild%%'" get ProcessId /format:value 2^>nul ^| findstr "="') do (
    set "PID=%%~p"
    taskkill /PID !PID! /F >nul 2>&1
    set /a KILLED+=1
)

:: Kill uvicorn processes on our Python ports
for %%P in (8101 8102 8103 8104) do (
    for /f "tokens=5" %%p in ('netstat -ano 2^>nul ^| findstr ":%%P "') do (
        taskkill /PID %%p /F >nul 2>&1
    )
)

echo [%DATE% %TIME%] Done. Killed !KILLED! process(es). >> "%LOGS%\shutdown.log"
echo Done. Killed !KILLED! process(es).
pause >nul
