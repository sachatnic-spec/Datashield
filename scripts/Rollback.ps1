param(
    [Parameter(Mandatory=$true)][string]$ServiceName,
    [Parameter(Mandatory=$true)][string]$TargetArtifactPath,
    [string]$DeployRoot = "D:/Development Practice/Datasheild/deploy"
)

$backupDir = Join-Path $DeployRoot "$ServiceName\backup"
$latest = Get-ChildItem $backupDir -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1

if (-not $latest) {
    Write-Error "==> No backup found for $ServiceName, cannot rollback"
    exit 1
}

Write-Host "==> Rolling back $ServiceName to $($latest.Name)"
Copy-Item -Path $latest.FullName -Destination $TargetArtifactPath -Force
Write-Host "==> Rollback artifact restored. Re-run the start step to bring the previous version back up."
