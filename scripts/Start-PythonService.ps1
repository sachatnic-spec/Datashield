param(
    [Parameter(Mandatory=$true)][string]$ServiceName,
    [Parameter(Mandatory=$true)][string]$ServiceDir,
    [Parameter(Mandatory=$true)][int]$Port,
    [string]$DeployRoot = "D:/Development Practice/Datasheild/deploy",
    [string]$VenvName = ".venv"
)

$logDir = Join-Path $DeployRoot "$ServiceName\logs"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$stdout = Join-Path $logDir "$ServiceName-stdout.log"

$venvPython = Join-Path $ServiceDir "$VenvName\Scripts\python.exe"

Write-Host "==> Starting $ServiceName (uvicorn) from $ServiceDir on port $Port"

$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = $venvPython
$psi.Arguments = "-m uvicorn main:app --host 0.0.0.0 --port $Port"
$psi.WorkingDirectory = $ServiceDir
$psi.UseShellExecute = $false
$psi.RedirectStandardOutput = $true
$psi.RedirectStandardError = $true
$psi.CreateNoWindow = $true

$process = New-Object System.Diagnostics.Process
$process.StartInfo = $psi
$process.Start() | Out-Null

Write-Host "==> $ServiceName started with PID $($process.Id)"
$process.Id | Out-File -FilePath (Join-Path $DeployRoot "$ServiceName\$ServiceName.pid") -Encoding ascii
