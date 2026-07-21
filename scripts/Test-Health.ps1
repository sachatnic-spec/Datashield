param(
    [Parameter(Mandatory=$true)][string]$ServiceName,
    [Parameter(Mandatory=$true)][int]$Port,
    [Parameter(Mandatory=$true)][string]$HealthPath,
    [int]$MaxRetries = 15,
    [int]$DelaySeconds = 4
)

$url = "http://localhost:$Port$HealthPath"
Write-Host "==> Health-checking $ServiceName at $url"

for ($i = 1; $i -le $MaxRetries; $i++) {
    try {
        $resp = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
        if ($resp.StatusCode -eq 200) {
            Write-Host "==> $ServiceName is healthy (attempt $i/$MaxRetries)"
            exit 0
        }
    } catch {
        Write-Host "    Attempt $i/$MaxRetries : not ready yet ($($_.Exception.Message))"
    }
    Start-Sleep -Seconds $DelaySeconds
}

Write-Error "==> $ServiceName failed health check after $MaxRetries attempts"
exit 1
