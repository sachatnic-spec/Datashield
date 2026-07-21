@echo off
REM ==========================================================
REM DataShield India - Build Shared Libraries
REM ==========================================================

call "%~dp0config.bat"
call "%~dp0common.bat" :Init
call "%~dp0logger.bat" :InitLogger

call "%~dp0logger.bat" :Section "Building Shared Libraries"

for %%L in (%SHARED_LIBS%) do (
    call :BuildLibrary %%L
)

echo.
call "%~dp0logger.bat" :Success "All shared libraries built successfully."
exit /b 0

:BuildLibrary
set LIB=%~1
set LIB_DIR=%SERVICES%\%LIB%
set LOG_FILE=%LOG_DIR%\%LIB%.log
set ERR_FILE=%ERROR_DIR%\%LIB%-error.log

call "%~dp0logger.bat" :ServiceStart "%LIB%"

if not exist "%LIB_DIR%" (
    call "%~dp0logger.bat" :Error "%LIB% directory not found."
    exit /b 1
)

pushd "%LIB_DIR%"

call "%MVN%" clean install %SKIP_TESTS% > "%LOG_FILE%" 2>&1
set RC=%ERRORLEVEL%

popd

if not "%RC%"=="0" (
    call "%~dp0common.bat" :ExtractMavenErrors "%LOG_FILE%" "%ERR_FILE%"
    call "%~dp0common.bat" :UpdateHtml "%LIB%" FAILED
    call "%~dp0logger.bat" :ServiceFailed "%LIB%"
    exit /b 1
)

call "%~dp0common.bat" :UpdateHtml "%LIB%" SUCCESS
call "%~dp0common.bat" :WriteManifest LIB "%LIB%" SUCCESS
call "%~dp0logger.bat" :ServiceSuccess "%LIB%" "OK"

exit /b 0
