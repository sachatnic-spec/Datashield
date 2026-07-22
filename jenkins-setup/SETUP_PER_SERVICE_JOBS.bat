@echo off
REM Jenkins per-service job setup guide for Windows

echo.
echo ==========================================
echo DataShield - Per-Service Jenkins Setup
echo ==========================================
echo.

echo OPTION 1: Auto-Create Jobs via REST API (RECOMMENDED)
echo =====================================================
echo PowerShell:
echo   cd jenkins-setup
echo   .\create-jobs.ps1 -JenkinsUrl "http://localhost:8080" -Username "admin" -ApiToken "YOUR_API_TOKEN"
echo.
echo Batch:
echo   cd jenkins-setup
echo   create-jobs.bat http://localhost:8080 admin YOUR_API_TOKEN
echo.
echo How to get API Token:
echo   1. Log in to Jenkins
echo   2. Click your username (top-right)
echo   3. Click "Configure"
echo   4. Under "API Token", click "Add new Token"
echo   5. Generate and copy token
echo.

echo OPTION 2: Use Jenkins Job DSL UI
echo ================================
echo 1. Install Job DSL plugin:
echo    - Manage Jenkins ^-^> Plugins ^-^> Available
echo    - Search "Job DSL" and install
echo.
echo 2. Create a "Freestyle" job called "generate-datashield-jobs"
echo.
echo 3. Build section, add "Process Job DSLs":
echo    - Look on filesystem: jenkins-setup/generate-jobs.groovy
echo.
echo 4. Run the job - creates all 27 per-service jobs
echo.

echo OPTION 3: Manual Jenkins UI (per-service)
echo ==========================================
echo For each service:
echo 1. Create "Multibranch Pipeline" job
echo 2. Name: DataShield-^<service-name^>
echo 3. Repository URL: https://github.com/sachatnic-spec/Datashield.git
echo 4. Script path: services/^<service-name^>/Jenkinsfile
echo 5. Trigger: GitHub hook / Poll SCM (H/5 * * * *)
echo.
echo (Repeat for all 27 services)
echo.

echo ==========================================
echo WEBHOOK SETUP (GitHub to Jenkins)
echo ==========================================
echo 1. GitHub repo ^-^> Settings ^-^> Webhooks
echo 2. Add webhook:
echo    - Payload URL: http://^<JENKINS_URL^>/github-webhook/
echo    - Content type: application/json
echo    - Triggers: Push events
echo 3. Jenkins will auto-trigger on each push
echo.

echo ==========================================
echo AFTER SETUP - Triggers Work Like:
echo ==========================================
echo + Webhook: Push to GitHub ^-^> instant build
echo + SCM Poll: Jenkins checks every 5 min
echo + Per-service: Only affected service builds
echo + Build history: Separate job per service
echo.
