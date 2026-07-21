param(
    [Parameter(Mandatory=$true)][string]$ServiceName,
    [Parameter(Mandatory=$true)][int]$Port
)

Write-Host "==> Stopping $ServiceName on port $Port"

$conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
if ($conn) {
    $pids = $conn | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($p in $pids) {
        try {
            Write-Host "    Killing PID $p"
            Stop-Process -Id $p -Force -ErrorAction Stop
        } catch {
            Write-Warning "    Could not kill PID $p : $_"
        }
    }
    Start-Sleep -Seconds 2
    Write-Host "==> $ServiceName stopped"
} else {
    Write-Host "==> No process found listening on port $Port (already stopped)"
}
