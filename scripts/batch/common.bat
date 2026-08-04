@echo off
REM ==========================================================
REM DataShield India - Common Utility Library
REM ==========================================================
REM Usage:
REM call "%~dp0common.bat" :Init
REM call "%~dp0common.bat" :Log "message"
REM call "%~dp0common.bat" :UpdateHtml auth-service RUNNING
REM exit /b

if "%~1"=="" goto :eof
goto %1

:Init
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%REPORT_DIR%" mkdir "%REPORT_DIR%"
if not exist "%ERROR_DIR%" mkdir "%ERROR_DIR%"
exit /b

:Log
echo [%TIME%] %~2
>> "%LOG_DIR%\build.log" echo [%DATE% %TIME%] %~2
exit /b

:StartTimer
set TIMER_START=%TIME%
exit /b

:EndTimer
set TIMER_END=%TIME%
exit /b

:WriteManifest
>> "%REPORT_DIR%\build-manifest.txt" echo %~2^|%~3^|%~4
exit /b

:WriteSummary
>> "%REPORT_DIR%\build-summary.txt" echo %~2
exit /b

:ExtractMavenErrors
set LOGFILE=%~2
set ERRFILE=%~3
if exist "%LOGFILE%" (
    findstr /C:"[ERROR]" "%LOGFILE%" > "%ERRFILE%"
)
exit /b

:FindJar
set SERVICE=%~2
set JAR_FILE=
for /f "delims=" %%f in ('dir /b "%SERVICES%\%SERVICE%\target\*.jar" 2^>nul ^| findstr /v "original"') do (
    if not defined JAR_FILE set JAR_FILE=%SERVICES%\%SERVICE%\target\%%f
)
exit /b

:UpdateHtml
REM Parameters:
REM %2 = service
REM %3 = status (PENDING/RUNNING/SUCCESS/FAILED)

set HTML=%BUILD_HOME%\build-status.html

if not exist "%HTML%" exit /b

powershell -NoProfile -Command ^
"$p='%HTML%';" ^
"$c=Get-Content $p -Raw;" ^
"$row='<tr><td>%~2</td><td>%~3</td><td>-</td><td>%~2.log</td></tr>';" ^
"$c=$c -replace '<tbody id=""services"">','<tbody id=""services"">'+$row;" ^
"Set-Content $p $c"

exit /b

:Divider
echo ------------------------------------------------------------
exit /b

:eof
exit /b
