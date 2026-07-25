@echo off
REM ============================================================
REM Verify Windows Jenkins Prerequisites
REM Run this before creating Jenkins jobs
REM ============================================================

setlocal enabledelayedexpansion

echo.
echo ====================================================
echo Windows Jenkins Prerequisites Check
echo ====================================================
echo.

REM Check Maven
echo [1/3] Checking Maven...
mvn -version >nul 2>&1
if !errorlevel! equ 0 (
    echo   ✓ Maven found
    mvn -version | findstr "Apache Maven"
) else (
    echo   ✗ Maven NOT found
    echo   Solution: Download from https://maven.apache.org/download.cgi
    echo   Add to PATH and set JAVA_HOME
)

echo.
echo [2/3] Checking Node/npm...
node -v >nul 2>&1
if !errorlevel! equ 0 (
    echo   ✓ Node found
    node -v
    npm -v
) else (
    echo   ✗ Node NOT found
    echo   Solution: Download from https://nodejs.org/
    echo   Add to PATH
)

echo.
echo [3/3] Checking Python...
python --version >nul 2>&1
if !errorlevel! equ 0 (
    echo   ✓ Python found
    python --version
) else (
    echo   ✗ Python NOT found
    echo   Solution: Download from https://www.python.org/
    echo   Add to PATH and check "Add Python to PATH"
)

echo.
echo [4/4] Checking Git...
git --version >nul 2>&1
if !errorlevel! equ 0 (
    echo   ✓ Git found
    git --version
) else (
    echo   ✗ Git NOT found
    echo   Solution: Download from https://git-scm.com/
)

echo.
echo ====================================================
echo Verification Complete
echo ====================================================
echo.
echo Next Steps:
echo   1. All tools must be found for builds to work
echo   2. Add any missing tools to Windows PATH
echo   3. Restart Jenkins after installing tools
echo   4. Then create Jenkins jobs with Jenkinsfiles
echo.
echo Windows Jenkinsfiles to use:
echo   - Jenkinsfile-build-all-windows (builds only)
echo   - Jenkinsfile-complete-windows (build + deploy)
echo.

pause
