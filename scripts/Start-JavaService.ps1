param(
    [Parameter(Mandatory=$true)][string]$ServiceName,
    [Parameter(Mandatory=$true)][string]$JarPath,
    [Parameter(Mandatory=$true)][int]$Port,
    [string]$DeployRoot = "D:/Development Practice/Datasheild/deploy",
    [string]$JavaOpts = "-Xms256m -Xmx512m"
)

$logDir = Join-Path $DeployRoot "$ServiceName\logs"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$stdout = Join-Path $logDir "$ServiceName-stdout.log"
$stderr = Join-Path $logDir "$ServiceName-stderr.log"

Write-Host "==> Starting $ServiceName from $JarPath on port $Port"

$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = "java"
$psi.Arguments = "$JavaOpts -jar `"$JarPath`" --server.port=$Port"
$psi.UseShellExecute = $false
$psi.RedirectStandardOutput = $true
$psi.RedirectStandardError = $true
$psi.CreateNoWindow = $true

$process = New-Object System.Diagnostics.Process
$process.StartInfo = $psi
$process.Start() | Out-Null

# Detach and pipe output to log files asynchronously
Start-Job -ScriptBlock {
    param($proc, $out)
    $proc.StandardOutput.ReadToEnd() | Out-File -FilePath $out -Append
} -ArgumentList $process, $stdout | Out-Null

Write-Host "==> $ServiceName started with PID $($process.Id)"
Write-Host "    stdout: $stdout"
Write-Host "    stderr: $stderr"

# Save PID for future stop/rollback reference
$process.Id | Out-File -FilePath (Join-Path $DeployRoot "$ServiceName\$ServiceName.pid") -Encoding ascii
