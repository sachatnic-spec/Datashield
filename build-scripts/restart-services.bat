@echo off
setlocal

REM ==========================================================
REM DataShield India - Restart Services
REM ==========================================================

call "%~dp0config.bat"
call "%~dp0logger.bat" :InitLogger

call "%~dp0logger.bat" :Section "Restarting DataShield Services"

echo.
echo ==========================================
echo Stopping services...
echo ==========================================
call "%~dp0stop-services.bat"

echo.
echo Waiting 10 seconds...
timeout /t 10 /nobreak >nul

echo.
echo ==========================================
echo Starting services...
echo ==========================================
call "%~dp0start-services.bat"

echo.
echo ==========================================
echo Restart Completed
echo ==========================================
echo Restart Time : %DATE% %TIME%
echo Logs         : %LOG_DIR%\runtime
echo ==========================================

pause
exit /b
