@echo off
REM ==========================================================
REM DataShield India - Python Service Builder
REM ==========================================================

call "%~dp0config.bat"
call "%~dp0common.bat" :Init
call "%~dp0logger.bat" :InitLogger

call "%~dp0logger.bat" :Section "Building Python Services"

set SUCCESS=0
set FAILED=0

for %%S in (%PYTHON_SERVICES%) do (
    call :BuildPython %%S
)

echo.
echo ============================================
echo PYTHON BUILD COMPLETE
echo Success : %SUCCESS%
echo Failed  : %FAILED%
echo ============================================
exit /b

:BuildPython

set SERVICE=%~1
set DIR=%SERVICES%\%SERVICE%
set LOG=%LOG_DIR%\%SERVICE%-python.log
set ERR=%ERROR_DIR%\%SERVICE%-python-error.log
set VENV=%DIR%\.venv

call "%~dp0logger.bat" :ServiceStart "%SERVICE% (Python)"
call "%~dp0common.bat" :UpdateHtml "%SERVICE%" RUNNING

if not exist "%DIR%" (
    call "%~dp0logger.bat" :Error "%SERVICE% directory not found."
    set /a FAILED+=1
    goto :eof
)

if not exist "%PYTHON%" (
    call "%~dp0logger.bat" :Error "Python executable not found."
    set /a FAILED+=1
    goto :eof
)

if not exist "%VENV%\Scripts\python.exe" (
    "%PYTHON%" -m venv "%VENV%" > "%LOG%" 2>&1
)

if exist "%DIR%\requirements.txt" (
    call "%VENV%\Scripts\pip.exe" install -r "%DIR%\requirements.txt" >> "%LOG%" 2>&1
)

call "%VENV%\Scripts\python.exe" -m compileall "%DIR%\app" >> "%LOG%" 2>&1

set RC=%ERRORLEVEL%

if not "%RC%"=="0" (
    call "%~dp0common.bat" :ExtractMavenErrors "%LOG%" "%ERR%"
    call "%~dp0common.bat" :UpdateHtml "%SERVICE%" FAILED
    call "%~dp0common.bat" :WriteManifest PYTHON "%SERVICE%" FAILED
    call "%~dp0logger.bat" :ServiceFailed "%SERVICE%"
    set /a FAILED+=1
    goto :eof
)

call "%~dp0common.bat" :UpdateHtml "%SERVICE%" SUCCESS
call "%~dp0common.bat" :WriteManifest PYTHON "%SERVICE%" SUCCESS
call "%~dp0logger.bat" :ServiceSuccess "%SERVICE%" "SUCCESS"

set /a SUCCESS+=1

goto :eof
