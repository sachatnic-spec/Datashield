@echo off
setlocal EnableDelayedExpansion

REM ==========================================================
REM DataShield India - Python Service Builder
REM Builds every Python service under SERVICES\ (venv, deps,
REM compile check) and packages each into a fresh, timestamped
REM output folder (source only, .venv/__pycache__ excluded).
REM ==========================================================

set "ROOT=%~dp0..\.."
set "SERVICES=%ROOT%\backend\python-services"
set "PYTHON=python"

where %PYTHON% >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python not found on PATH. Edit PYTHON at the top of this script.
    exit /b 1
)

for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value ^| find "="') do set "DT=%%I"
set "STAMP=%DT:~0,8%_%DT:~8,6%"

set "OUTPUT_DIR=%ROOT%\build\python_%STAMP%"
set "LOG_DIR=%ROOT%\build\logs_%STAMP%"
set "ERROR_DIR=%LOG_DIR%\errors"
set "SUMMARY=%LOG_DIR%\python-summary.txt"

if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%ERROR_DIR%" mkdir "%ERROR_DIR%"

set SUCCESS=0
set FAILED=0
set FAILED_LIST=

echo ============================================
echo Building Python Services
echo Root:   %ROOT%
echo Output -^> %OUTPUT_DIR%
echo Logs   -^> %LOG_DIR%
echo ============================================

if not exist "%SERVICES%" (
    echo [ERROR] Services folder not found: %SERVICES%
    exit /b 1
)

REM --- Auto-discover Python services: folders with requirements.txt, skip Java folders ---
for /d %%D in ("%SERVICES%\*") do (
    if exist "%%D\requirements.txt" (
        if not exist "%%D\pom.xml" (
            call :BuildPython "%%~nxD"
        )
    )
)

echo.
echo ============================================
echo PYTHON BUILD COMPLETE
echo Success : !SUCCESS!
echo Failed  : !FAILED!
if not "!FAILED_LIST!"=="" echo Failed services:!FAILED_LIST!
echo Packages in : %OUTPUT_DIR%
echo Logs in     : %LOG_DIR%
echo ============================================

endlocal
exit /b

:BuildPython
set "SERVICE=%~1"
set "DIR=%SERVICES%\%SERVICE%"
set "LOG=%LOG_DIR%\%SERVICE%-python.log"
set "ERR=%ERROR_DIR%\%SERVICE%-python-error.log"
set "VENV=%DIR%\.venv"

echo.
echo --- Building %SERVICE% (Python) ---
>> "%SUMMARY%" echo [START] %SERVICE%

if not exist "%VENV%\Scripts\python.exe" (
    %PYTHON% -m venv "%VENV%" > "%LOG%" 2>&1
    if errorlevel 1 (
        echo [FAILED] %SERVICE% - venv creation failed, see %LOG%
        >> "%SUMMARY%" echo [FAILED] %SERVICE% - venv creation
        set /a FAILED+=1
        set "FAILED_LIST=!FAILED_LIST! %SERVICE%"
        goto :eof
    )
)

call "%VENV%\Scripts\pip.exe" install --upgrade pip >> "%LOG%" 2>&1
call "%VENV%\Scripts\pip.exe" install -r "%DIR%\requirements.txt" >> "%LOG%" 2>&1
if errorlevel 1 (
    echo [FAILED] %SERVICE% - pip install failed, see %LOG%
    findstr /I "ERROR" "%LOG%" > "%ERR%" 2>nul
    >> "%SUMMARY%" echo [FAILED] %SERVICE% - pip install
    set /a FAILED+=1
    set "FAILED_LIST=!FAILED_LIST! %SERVICE%"
    goto :eof
)

set "COMPILE_TARGET=%DIR%\app"
if not exist "%COMPILE_TARGET%" set "COMPILE_TARGET=%DIR%"

call "%VENV%\Scripts\python.exe" -m compileall "%COMPILE_TARGET%" -x ".venv|__pycache__|\.git" -q >> "%LOG%" 2>&1
if errorlevel 1 (
    echo [FAILED] %SERVICE% - compile check failed, see %LOG%
    findstr /I "Error" "%LOG%" > "%ERR%" 2>nul
    >> "%SUMMARY%" echo [FAILED] %SERVICE% - compile check
    set /a FAILED+=1
    set "FAILED_LIST=!FAILED_LIST! %SERVICE%"
    goto :eof
)

REM --- Package source into the output folder, excluding venv/cache noise ---
set "PKG=%OUTPUT_DIR%\%SERVICE%"
if exist "%PKG%" rd /s /q "%PKG%"
robocopy "%DIR%" "%PKG%" /E /XD .venv __pycache__ .pytest_cache .git /NFL /NDL /NJH /NJS >nul

echo [OK] %SERVICE% -^> %PKG%
>> "%SUMMARY%" echo [SUCCESS] %SERVICE%
set /a SUCCESS+=1
goto :eof