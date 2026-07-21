@echo off
REM ==========================================================
REM DataShield India - Java Service Builder
REM ==========================================================

call "%~dp0config.bat"
call "%~dp0common.bat" :Init
call "%~dp0logger.bat" :InitLogger

call "%~dp0logger.bat" :Section "Building Java Services"

set SUCCESS=0
set FAILED=0

for %%S in (%JAVA_SERVICES%) do (
    call :BuildService %%S
)

echo.
echo ============================================
echo BUILD COMPLETE
echo Success : %SUCCESS%
echo Failed  : %FAILED%
echo ============================================

exit /b

:BuildService

set SERVICE=%~1
set DIR=%SERVICES%\%SERVICE%
set LOG=%LOG_DIR%\%SERVICE%.log
set ERR=%ERROR_DIR%\%SERVICE%-error.log

call "%~dp0logger.bat" :ServiceStart "%SERVICE%"
call "%~dp0common.bat" :UpdateHtml "%SERVICE%" RUNNING

if not exist "%DIR%" (
    call "%~dp0logger.bat" :Error "%SERVICE% directory not found."
    set /a FAILED+=1
    goto :eof
)

pushd "%DIR%"

call "%MVN%" clean install %SKIP_TESTS% > "%LOG%" 2>&1
set RC=%ERRORLEVEL%

popd

if not "%RC%"=="0" (
    call "%~dp0common.bat" :ExtractMavenErrors "%LOG%" "%ERR%"
    call "%~dp0common.bat" :WriteManifest SERVICE "%SERVICE%" FAILED
    call "%~dp0common.bat" :WriteSummary "%SERVICE% FAILED"
    call "%~dp0common.bat" :UpdateHtml "%SERVICE%" FAILED
    call "%~dp0logger.bat" :ServiceFailed "%SERVICE%"
    set /a FAILED+=1
    goto :eof
)

call "%~dp0common.bat" :FindJar "%SERVICE%"
call "%~dp0common.bat" :WriteManifest SERVICE "%SERVICE%" SUCCESS
call "%~dp0common.bat" :WriteSummary "%SERVICE% SUCCESS"
call "%~dp0common.bat" :UpdateHtml "%SERVICE%" SUCCESS
call "%~dp0logger.bat" :ServiceSuccess "%SERVICE%" "SUCCESS"

set /a SUCCESS+=1

goto :eof
