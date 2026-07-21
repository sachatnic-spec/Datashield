@echo off
REM ==========================================================
REM DataShield Build - Root Convenience Wrapper
REM Calls the master build orchestrator from build-scripts/
REM ==========================================================

set "ROOT=%~dp0"
call "%ROOT%build-scripts\build.bat" %*
exit /b %ERRORLEVEL%
