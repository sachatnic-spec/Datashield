@echo off
setlocal EnableDelayedExpansion

REM ==========================================================
REM DataShield India - Master Build Orchestrator
REM Builds Angular apps, Java services, and Python services
REM in a single coordinated build run with unified timestamp.
REM ==========================================================

set "ROOT=%~dp0.."
set "BUILD_SCRIPTS=%ROOT%\build-scripts"

REM --- Create unified timestamp for all build phases ---
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value ^| find "="') do set "DT=%%I"
set "BUILD_STAMP=%DT:~0,8%_%DT:~8,6%"

set "BUILD_DIR=%ROOT%\build"
set "LOG_DIR=%BUILD_DIR%\logs_%BUILD_STAMP%"

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

echo ============================================
echo DataShield Build Orchestrator
echo Build Timestamp: %BUILD_STAMP%
echo Root: %ROOT%
echo Logs: %LOG_DIR%
echo ============================================
echo.

set "OVERALL_SUCCESS=0"
set "OVERALL_FAILED=0"

REM --- Phase 1: Angular Build ---
echo [PHASE 1/3] Building Angular Applications...
set "PHASE_LOG=%LOG_DIR%\01-angular.log"
call "%BUILD_SCRIPTS%\build-angular.bat" > "%PHASE_LOG%" 2>&1
set "RC=!ERRORLEVEL!"
if "!RC!"=="0" (
    echo [OK] Angular build succeeded
    set /a OVERALL_SUCCESS+=1
) else (
    echo [FAILED] Angular build failed - see %PHASE_LOG%
    set /a OVERALL_FAILED+=1
)
echo.

REM --- Phase 2: Java Build ---
echo [PHASE 2/3] Building Java Services...
set "PHASE_LOG=%LOG_DIR%\02-java.log"
call "%BUILD_SCRIPTS%\build-java.bat" > "%PHASE_LOG%" 2>&1
set "RC=!ERRORLEVEL!"
if "!RC!"=="0" (
    echo [OK] Java build succeeded
    set /a OVERALL_SUCCESS+=1
) else (
    echo [FAILED] Java build failed - see %PHASE_LOG%
    set /a OVERALL_FAILED+=1
)
echo.

REM --- Phase 3: Python Build ---
echo [PHASE 3/3] Building Python Services...
set "PHASE_LOG=%LOG_DIR%\03-python.log"
call "%BUILD_SCRIPTS%\build-python.bat" > "%PHASE_LOG%" 2>&1
set "RC=!ERRORLEVEL!"
if "!RC!"=="0" (
    echo [OK] Python build succeeded
    set /a OVERALL_SUCCESS+=1
) else (
    echo [FAILED] Python build failed - see %PHASE_LOG%
    set /a OVERALL_FAILED+=1
)
echo.

echo ============================================
echo BUILD ORCHESTRATION COMPLETE
echo ============================================
echo Phases Passed: !OVERALL_SUCCESS!/3
echo Phases Failed: !OVERALL_FAILED!/3
echo Build outputs and logs in: %BUILD_DIR%
echo Full logs: %LOG_DIR%
echo ============================================

if not "!OVERALL_FAILED!"=="0" (
    exit /b 1
)

endlocal
exit /b