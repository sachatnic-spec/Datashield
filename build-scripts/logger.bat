@echo off
REM ==========================================================
REM DataShield India - Logger Library
REM ==========================================================
REM Usage:
REM call "%~dp0logger.bat" :Info "Starting build"
REM call "%~dp0logger.bat" :Success "auth-service built"
REM call "%~dp0logger.bat" :Error "Build failed"
REM ==========================================================

if "%~1"=="" goto :eof
goto %1

:InitLogger
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
set BUILD_LOG=%LOG_DIR%\build.log
exit /b

:Timestamp
for /f "usebackq delims=" %%T in (`powershell -NoProfile -Command "Get-Date -Format yyyy-MM-dd HH:mm:ss"`) do (
    set TS=%%T
)
exit /b

:Info
call :Timestamp
echo [INFO ] %~2
>> "%BUILD_LOG%" echo [%TS%] [INFO ] %~2
exit /b

:Warn
call :Timestamp
echo [WARN ] %~2
>> "%BUILD_LOG%" echo [%TS%] [WARN ] %~2
exit /b

:Error
call :Timestamp
echo [ERROR] %~2
>> "%BUILD_LOG%" echo [%TS%] [ERROR] %~2
exit /b

:Success
call :Timestamp
echo [ OK  ] %~2
>> "%BUILD_LOG%" echo [%TS%] [ OK  ] %~2
exit /b

:Section
echo.
echo ==========================================================
echo %~2
echo ==========================================================
>> "%BUILD_LOG%" echo.
>> "%BUILD_LOG%" echo ==========================================================
>> "%BUILD_LOG%" echo %~2
>> "%BUILD_LOG%" echo ==========================================================
exit /b

:ServiceStart
call :Info "Building %~2..."
exit /b

:ServiceSuccess
call :Success "%~2 completed in %~3"
exit /b

:ServiceFailed
call :Error "%~2 FAILED. See %ERROR_DIR%\%~2-error.log"
exit /b

:Rotate
if exist "%BUILD_LOG%" (
    for /f "usebackq delims=" %%T in (`powershell -NoProfile -Command "Get-Date -Format yyyyMMdd_HHmmss"`) do set DTS=%%T
    copy "%BUILD_LOG%" "%LOG_DIR%\build_!DTS!.log" >nul 2>&1
)
exit /b

:eof
exit /b
