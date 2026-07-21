@echo off
REM ==========================================================
REM DataShield India - Angular Builder
REM ==========================================================

call "%~dp0config.bat"
call "%~dp0common.bat" :Init
call "%~dp0logger.bat" :InitLogger

call "%~dp0logger.bat" :Section "Building Angular Applications"

where node >nul 2>&1
if errorlevel 1 (
    call "%~dp0logger.bat" :Error "Node.js not found."
    exit /b 1
)

where npm >nul 2>&1
if errorlevel 1 (
    call "%~dp0logger.bat" :Error "npm not found."
    exit /b 1
)

call :BuildApp "%FRONTEND%"
exit /b

:BuildApp
set APP=%~1
for %%I in ("%APP%") do set APPNAME=%%~nxI

set LOG=%LOG_DIR%\%APPNAME%.log
set ERR=%ERROR_DIR%\%APPNAME%-error.log

call "%~dp0logger.bat" :ServiceStart "%APPNAME%"
call "%~dp0common.bat" :UpdateHtml "%APPNAME%" RUNNING

if not exist "%APP%" (
    call "%~dp0logger.bat" :Error "%APP% not found."
    exit /b 1
)

pushd "%APP%"

if not exist node_modules (
    call npm install > "%LOG%" 2>&1
)

call npm run build >> "%LOG%" 2>&1
set RC=%ERRORLEVEL%

popd

if not "%RC%"=="0" (
    findstr /C:"ERROR" /C:"error TS" /C:"npm ERR!" "%LOG%" > "%ERR%" 2>nul
    call "%~dp0common.bat" :UpdateHtml "%APPNAME%" FAILED
    call "%~dp0common.bat" :WriteManifest FRONTEND "%APPNAME%" FAILED
    call "%~dp0logger.bat" :ServiceFailed "%APPNAME%"
    exit /b 1
)

set DIST=
for /f "delims=" %%F in ('dir /b /s "%APP%\dist\index.html" 2^>nul') do (
    if not defined DIST set DIST=%%~dpF
)

call "%~dp0common.bat" :UpdateHtml "%APPNAME%" SUCCESS
call "%~dp0common.bat" :WriteManifest FRONTEND "%APPNAME%" SUCCESS "%DIST%"
call "%~dp0logger.bat" :ServiceSuccess "%APPNAME%" "SUCCESS"

exit /b 0
