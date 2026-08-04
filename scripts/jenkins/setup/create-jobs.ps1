# Jenkins per-service job creation via REST API
# Usage: .\create-jobs.ps1 -JenkinsUrl "http://localhost:8080" -Username "admin" -ApiToken "token123"

param(
    [Parameter(Mandatory=$true)]
    [string]$JenkinsUrl,
    
    [Parameter(Mandatory=$true)]
    [string]$Username,
    
    [Parameter(Mandatory=$true)]
    [string]$ApiToken
)

$repo = "https://github.com/sachatnic-spec/Datashield.git"
$services = @(
    "auth-service",
    "ai-analysis-service",
    "analytics-service",
    "anomaly-detection-service",
    "audit-service",
    "breach-service",
    "config-service",
    "connector-service",
    "consent-service",
    "data-classification-service",
    "data-discovery-service",
    "data-lineage-service",
    "dpbi-service",
    "grievance-service",
    "notification-service",
    "pii-detection-service",
    "policy-service",
    "report-service",
    "retention-service",
    "rights-service",
    "risk-scoring-service",
    "search-service",
    "siem-service",
    "tenant-service",
    "vendor-service",
    "webhook-service",
    "workflow-service"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Creating per-service Jenkins jobs" -ForegroundColor Cyan
Write-Host "Jenkins: $JenkinsUrl" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("$Username`:$ApiToken"))
$headers = @{
    "Authorization" = "Basic $auth"
    "Content-Type" = "application/xml"
}

foreach ($service in $services) {
    Write-Host "Creating DataShield-$service..." -ForegroundColor Yellow
    
    $jobName = "DataShield-$service"
    $url = "$JenkinsUrl/createItem?name=$jobName"
    
    $jobXml = @"
<?xml version="1.1" encoding="UTF-8"?>
<flow-definition plugin="workflow-job@1174.1148.v7b_e4953211c9">
  <description>DataShield - Build and Deploy $service</description>
  <keepDependencies>false</keepDependencies>
  <properties/>
  <triggers>
    <hudson.triggers.SCMTrigger>
      <spec>H/5 * * * *</spec>
      <ignorePostCommitHooks>false</ignorePostCommitHooks>
    </hudson.triggers.SCMTrigger>
    <com.github.pushdotccgithubwebhooktrigger.PushTrigger plugin="github-webhook@1.0.2">
      <push>true</push>
    </com.github.pushdotccgithubwebhooktrigger.PushTrigger>
  </triggers>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@2933.v6f7e81ca_e34d">
    <scm class="hudson.plugins.git.GitSCM" plugin="git@5.2.2">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>$repo</url>
          <credentialsId>sachatnic-spec-jenkins</credentialsId>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/main</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
    </scm>
    <scriptPath>services/$service/Jenkinsfile</scriptPath>
  </definition>
</flow-definition>
"@

    try {
        $response = Invoke-WebRequest -Uri $url -Method POST -Headers $headers -Body $jobXml -ErrorAction Stop
        Write-Host "[OK] DataShield-$service created" -ForegroundColor Green
    }
    catch {
        if ($_.Exception.Response.StatusCode -eq 409) {
            Write-Host "[EXISTS] DataShield-$service already exists" -ForegroundColor Yellow
        }
        else {
            Write-Host "[ERROR] Failed: $_" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Job creation complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
