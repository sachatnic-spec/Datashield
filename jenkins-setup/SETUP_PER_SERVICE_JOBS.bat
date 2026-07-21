@echo off
REM Jenkins per-service job setup guide for Windows

echo.
echo ==========================================
echo DataShield - Per-Service Jenkins Setup
echo ==========================================
echo.

echo OPTION 1: Use Jenkins Job DSL (Recommended)
echo -------------------------------------------
echo 1. Install Job DSL plugin in Jenkins:
echo    - Manage Jenkins ^-^> Plugin Manager
echo    - Search "Job DSL" and install
echo.
echo 2. Create a new "Freestyle" job called "generate-datashield-jobs"
echo.
echo 3. In Build section, add "Process Job DSLs":
echo    - Look on filesystem: jenkins-setup/generate-jobs.groovy
echo.
echo 4. Run the job - it will create all 27 per-service jobs
echo.

echo OPTION 2: Manual Jenkins UI Setup (per-service)
echo -----------------------------------------------
echo Job: DataShield-auth-service
echo   Type: Multibranch Pipeline
echo   Repository URL: https://github.com/sachatnic-spec/Datashield.git
echo   Script path: services/auth-service/Jenkinsfile
echo   Trigger: GitHub hook / Poll SCM (H/5 * * * *)
echo.
echo (Repeat for all 27 services)
echo.

echo ==========================================
echo WEBHOOK SETUP (GitHub to Jenkins)
echo ==========================================
echo 1. Go to GitHub repo Settings -^> Webhooks
echo 2. Add webhook:
echo    - Payload URL: http://^<JENKINS_URL^>/github-webhook/
echo    - Content type: application/json
echo    - Triggers: Push events
echo 3. Jenkins will auto-trigger on each push
echo.

echo ==========================================
echo TRIGGER BEHAVIOR
echo ==========================================
echo + Poll SCM: Jenkins checks for changes every 5 minutes
echo + GitHub webhook: Instant trigger on git push
echo + Only builds the service with changes
echo + Each service has its own build history
echo.
