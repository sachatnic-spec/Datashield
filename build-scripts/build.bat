@echo off
setlocal

REM ==========================================================
REM DataShield India - Master Build
REM ==========================================================

call "%~dp0config.bat"
call "%~dp0common.bat" :Init
call "%~dp0logger.bat" :InitLogger

call "%~dp0logger.bat" :Section "DataShield India Build Started"

echo.
echo ============================================
echo DataShield India Build
echo ============================================
echo.

set STARTTIME=%TIME%

REM ----------------------------------------------------------
REM Build Shared Libraries
REM ----------------------------------------------------------
call "%~dp0logger.bat" :Section "STEP 1 : Shared Libraries"

call "%~dp0build-root.bat"
if errorlevel 1 (
    call "%~dp0logger.bat" :Error "Shared library build failed."
    goto END
)

REM ----------------------------------------------------------
REM Build Java
REM ----------------------------------------------------------
call "%~dp0logger.bat" :Section "STEP 2 : Java Services"

call "%~dp0build-java.bat"

REM ----------------------------------------------------------
REM Build Python
REM ----------------------------------------------------------
call "%~dp0logger.bat" :Section "STEP 3 : Python Services"

call "%~dp0build-python.bat"

REM ----------------------------------------------------------
REM Build Angular
REM ----------------------------------------------------------
call "%~dp0logger.bat" :Section "STEP 4 : Angular"

call "%~dp0build-angular.bat"

:END

echo.
echo ============================================
echo BUILD FINISHED
echo Started : %STARTTIME%
echo Finished: %TIME%
echo ============================================

echo.
echo Logs      : %LOG_DIR%
echo Reports   : %REPORT_DIR%
echo Dashboard : %BUILD_HOME%\build-status.html

echo.
pause
exit /b
