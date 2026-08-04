@echo off
REM Jenkins per-service job creation via REST API
REM Usage: create-jobs.bat http://jenkins-url:8080 username api-token

setlocal EnableDelayedExpansion

if "%1"=="" (
  echo Usage: create-jobs.bat ^<jenkins-url^> ^<username^> ^<api-token^>
  echo Example: create-jobs.bat http://localhost:8080 admin mytoken123
  exit /b 1
)

set "JENKINS_URL=%1"
set "USERNAME=%2"
set "API_TOKEN=%3"
set "REPO=https://github.com/sachatnic-spec/Datashield.git"

echo ========================================
echo Creating per-service Jenkins jobs
echo Jenkins: %JENKINS_URL%
echo ========================================
echo.

for %%S in (
  auth-service
  ai-analysis-service
  analytics-service
  anomaly-detection-service
  audit-service
  breach-service
  config-service
  connector-service
  consent-service
  data-classification-service
  data-discovery-service
  data-lineage-service
  dpbi-service
  grievance-service
  notification-service
  pii-detection-service
  policy-service
  report-service
  retention-service
  rights-service
  risk-scoring-service
  search-service
  siem-service
  tenant-service
  vendor-service
  webhook-service
  workflow-service
) do (
  call :CreateJob "%%S"
)

echo.
echo ========================================
echo Job creation complete!
echo ========================================
goto :eof

:CreateJob
setlocal
set "SERVICE=%~1"
echo Creating DataShield-%SERVICE%...

set "XML=^
^<?xml version="1.1" encoding="UTF-8"?^>^
^<flow-definition plugin="workflow-job@1174.1148.v7b_e4953211c9"^>^
  ^<description^>DataShield - Build and Deploy %SERVICE%^</description^>^
  ^<keepDependencies^>false^</keepDependencies^>^
  ^<properties^>^
    ^<com.coralogix.jenkins.plugin.github.poll.GitHubPollPropertyProperty plugin="github-poll@1.0.2"^>^
      ^<enabled^>false^</enabled^>^
      ^<branches^>main^</branches^>^
    ^</com.coralogix.jenkins.plugin.github.poll.GitHubPollPropertyProperty^>^
  ^</properties^>^
  ^<triggers^>^
    ^<com.cloudbees.jenkins.plugins.bitbucket.BitbucketPushTrigger plugin="bitbucket@1.1.21"^>^
      ^<triggerOnPush^>true^</triggerOnPush^>^
    ^</com.cloudbees.jenkins.plugins.bitbucket.BitbucketPushTrigger^>^
    ^<hudson.triggers.SCMTrigger^>^
      ^<spec^>H/5 * * * *^</spec^>^
      ^<ignorePostCommitHooks^>false^</ignorePostCommitHooks^>^
    ^</hudson.triggers.SCMTrigger^>^
  ^</triggers^>^
  ^<definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@2933.v6f7e81ca_e34d"^>^
    ^<scm class="hudson.plugins.git.GitSCM" plugin="git@5.2.2"^>^
      ^<configVersion^>2^</configVersion^>^
      ^<userRemoteConfigs^>^
        ^<hudson.plugins.git.UserRemoteConfig^>^
          ^<url^>%REPO%^</url^>^
          ^<credentialsId^>sachatnic-spec-jenkins^</credentialsId^>^
        ^</hudson.plugins.git.UserRemoteConfig^>^
      ^</userRemoteConfigs^>^
      ^<branches^>^
        ^<hudson.plugins.git.BranchSpec^>^
          ^<name^>*/main^</name^>^
        ^</hudson.plugins.git.BranchSpec^>^
      ^</branches^>^
    ^</scm^>^
    ^<scriptPath^>services/%SERVICE%/Jenkinsfile^</scriptPath^>^
  ^</definition^>^
^</flow-definition^>
"

echo !XML! > temp_job.xml

curl -X POST "%JENKINS_URL%/createItem?name=DataShield-%SERVICE%" ^
  -u %USERNAME%:%API_TOKEN% ^
  -H "Content-Type: application/xml" ^
  -d @temp_job.xml

if !ERRORLEVEL! equ 0 (
  echo [OK] DataShield-%SERVICE% created
) else (
  echo [ERROR] Failed to create DataShield-%SERVICE%
)

del temp_job.xml
endlocal
goto :eof
