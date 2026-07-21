@echo off
setlocal EnableDelayedExpansion

REM ==========================================================
REM DataShield India - Angular Builder
REM Builds Angular app(s) under FRONTEND and collects each
REM dist\ output into a fresh, timestamped output folder.
REM ==========================================================

set "ROOT=%~dp0.."
set "FRONTEND=%ROOT%\frontend"

where node >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Node.js not found on PATH.
    exit /b 1
)

where npm >nul 2>&1
if errorlevel 1 (
    echo [ERROR] npm not found on PATH.
    exit /b 1
)

for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value ^| find "="') do set "DT=%%I"
set "STAMP=%DT:~0,8%_%DT:~8,6%"

set "OUTPUT_DIR=%ROOT%\build\frontend_%STAMP%"
set "LOG_DIR=%ROOT%\build\logs_%STAMP%"
set "ERROR_DIR=%LOG_DIR%\errors"
set "SUMMARY=%LOG_DIR%\frontend-summary.txt"

if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%ERROR_DIR%" mkdir "%ERROR_DIR%"

set SUCCESS=0
set FAILED=0
set FAILED_LIST=

echo ============================================
echo Building Angular Applications
echo Root:   %ROOT%
echo Output -^> %OUTPUT_DIR%
echo Logs   -^> %LOG_DIR%
echo ============================================

if not exist "%FRONTEND%" (
    echo [ERROR] Frontend folder not found: %FRONTEND%
    exit /b 1
)

REM --- If FRONTEND itself is an app, build it. Otherwise build each
REM --- subfolder under it that has its own package.json. ---
if exist "%FRONTEND%\package.json" (
    call :BuildApp "%FRONTEND%"
) else (
    for /d %%D in ("%FRONTEND%\*") do (
        if exist "%%D\package.json" (
            call :BuildApp "%%D"
        )
    )
)

echo.
echo ============================================
echo ANGULAR BUILD COMPLETE
echo Success : !SUCCESS!
echo Failed  : !FAILED!
if not "!FAILED_LIST!"=="" echo Failed apps:!FAILED_LIST!
echo Dist output in : %OUTPUT_DIR%
echo Logs in        : %LOG_DIR%
echo ============================================

endlocal
exit /b

:BuildApp
set "APP=%~1"
for %%I in ("%APP%") do set "APPNAME=%%~nxI"
set "LOG=%LOG_DIR%\%APPNAME%.log"
set "ERR=%ERROR_DIR%\%APPNAME%-error.log"

echo.
echo --- Building %APPNAME% ---
>> "%SUMMARY%" echo [START] %APPNAME%

pushd "%APP%"

if not exist node_modules (
    call npm install > "%LOG%" 2>&1
    if errorlevel 1 (
        popd
        echo [FAILED] %APPNAME% - npm install failed, see %LOG%
        findstr /C:"npm ERR!" "%LOG%" > "%ERR%" 2>nul
        >> "%SUMMARY%" echo [FAILED] %APPNAME% - npm install
        set /a FAILED+=1
        set "FAILED_LIST=!FAILED_LIST! %APPNAME%"
        goto :eof
    )
)

call npm run build >> "%LOG%" 2>&1
set "RC=!ERRORLEVEL!"
popd

if not "!RC!"=="0" (
    findstr /C:"ERROR" /C:"error TS" /C:"npm ERR!" "%LOG%" > "%ERR%" 2>nul
    echo [FAILED] %APPNAME% - build failed, see %LOG%
    >> "%SUMMARY%" echo [FAILED] %APPNAME%
    set /a FAILED+=1
    set "FAILED_LIST=!FAILED_LIST! %APPNAME%"
    goto :eof
)

REM --- Locate dist output (handles classic dist\ and newer dist\<name>\browser\) ---
set "DIST="
for /f "delims=" %%F in ('dir /b /s /a-d "%APP%\dist\index.html" 2^>nul') do (
    if not defined DIST set "DIST=%%~dpF"
)

if not defined DIST (
    echo [WARNING] %APPNAME% built OK but no dist\index.html found
    >> "%SUMMARY%" echo [WARNING] %APPNAME% - no dist output found
) else (
    set "PKG=%OUTPUT_DIR%\%APPNAME%"
    if exist "!PKG!" rd /s /q "!PKG!"
    xcopy "!DIST!" "!PKG!\" /E /I /Q /Y >nul
    echo [OK] %APPNAME% -^> !PKG!
    >> "%SUMMARY%" echo [SUCCESS] %APPNAME%
)

set /a SUCCESS+=1
goto :eof