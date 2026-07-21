@echo off
setlocal EnableDelayedExpansion

REM ==========================================================
REM DataShield India - Java Service Builder
REM Builds every Java service under SERVICES\ and collects
REM all resulting jars into a fresh, timestamped output folder.
REM ==========================================================

REM --- EDIT THIS if your project root is different ---
set "ROOT=%~dp0.."
set "SERVICES=%ROOT%\services"
set "MVN=mvn"
set "SKIP_TESTS=-DskipTests"

REM --- Timestamp for this build run (safe even with regional date formats) ---
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value ^| find "="') do set "DT=%%I"
set "STAMP=%DT:~0,8%_%DT:~8,6%"

set "OUTPUT_JARS=%ROOT%\build\jars_%STAMP%"
set "LOG_DIR=%ROOT%\build\logs_%STAMP%"
set "ERROR_DIR=%LOG_DIR%\errors"
set "SUMMARY=%LOG_DIR%\summary.txt"

if not exist "%OUTPUT_JARS%" mkdir "%OUTPUT_JARS%"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%ERROR_DIR%" mkdir "%ERROR_DIR%"

set SUCCESS=0
set FAILED=0
set FAILED_LIST=

echo ============================================
echo Building Java Services
echo Root:    %ROOT%
echo Jars ->  %OUTPUT_JARS%
echo Logs ->  %LOG_DIR%
echo ============================================

if not exist "%SERVICES%" (
    echo [ERROR] Services folder not found: %SERVICES%
    echo Edit ROOT/SERVICES at the top of this script and re-run.
    exit /b 1
)

REM --- Auto-discover Java services: any folder under services\ with a pom.xml ---
for /d %%D in ("%SERVICES%\*") do (
    if exist "%%D\pom.xml" (
        call :BuildService "%%~nxD"
    )
)

echo.
echo ============================================
echo BUILD COMPLETE
echo Success : !SUCCESS!
echo Failed  : !FAILED!
if not "!FAILED_LIST!"=="" echo Failed services:!FAILED_LIST!
echo Jars collected in : %OUTPUT_JARS%
echo Full logs in      : %LOG_DIR%
echo ============================================

endlocal
exit /b

:BuildService
set "SERVICE=%~1"
set "DIR=%SERVICES%\%SERVICE%"
set "LOG=%LOG_DIR%\%SERVICE%.log"
set "ERR=%ERROR_DIR%\%SERVICE%-error.log"

echo.
echo --- Building %SERVICE% ---
>> "%SUMMARY%" echo [START] %SERVICE%

pushd "%DIR%"
call %MVN% clean install %SKIP_TESTS% > "%LOG%" 2>&1
set "RC=!ERRORLEVEL!"
popd

if not "!RC!"=="0" (
    echo [FAILED] %SERVICE% - see %LOG%
    findstr /I "ERROR BUILD" "%LOG%" > "%ERR%" 2>nul
    >> "%SUMMARY%" echo [FAILED] %SERVICE%
    set /a FAILED+=1
    set "FAILED_LIST=!FAILED_LIST! %SERVICE%"
    goto :eof
)

REM --- Find the built jar, skipping sources/javadoc/original variants ---
set "FOUND_JAR="
if exist "%DIR%\target\*.jar" (
    for %%J in ("%DIR%\target\*.jar") do (
        echo %%~nJ | findstr /V /I "sources javadoc original" >nul
        if not errorlevel 1 (
            copy /Y "%%J" "%OUTPUT_JARS%\" >nul
            set "FOUND_JAR=%%~nxJ"
        )
    )
)

if "!FOUND_JAR!"=="" (
    echo [WARNING] %SERVICE% built OK but no jar found in target\
    >> "%SUMMARY%" echo [WARNING] %SERVICE% - no jar found
) else (
    echo [OK] %SERVICE% -^> !FOUND_JAR!
    >> "%SUMMARY%" echo [SUCCESS] %SERVICE% - !FOUND_JAR!
)

set /a SUCCESS+=1
goto :eof