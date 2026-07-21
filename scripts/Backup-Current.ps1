param(
    [Parameter(Mandatory=$true)][string]$ServiceName,
    [Parameter(Mandatory=$true)][string]$CurrentArtifactPath,
    [string]$DeployRoot = "D:/Development Practice/Datasheild/deploy",
    [int]$KeepLast = 3
)

$backupDir = Join-Path $DeployRoot "$ServiceName\backup"
New-Item -ItemType Directory -Force -Path $backupDir | Out-Null

if (Test-Path $CurrentArtifactPath) {
    $stamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $ext = [System.IO.Path]::GetExtension($CurrentArtifactPath)
    $dest = Join-Path $backupDir "$ServiceName-$stamp$ext"
    Copy-Item -Path $CurrentArtifactPath -Destination $dest -Force
    Write-Host "==> Backed up current artifact to $dest"

    # Prune old backups, keep last N
    Get-ChildItem $backupDir | Sort-Object LastWriteTime -Descending | Select-Object -Skip $KeepLast | Remove-Item -Force -ErrorAction SilentlyContinue
} else {
    Write-Host "==> No existing artifact at $CurrentArtifactPath, first deployment - skipping backup"
}
