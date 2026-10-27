# ==============================================================================
# Enterprise AI Guardian - Health Check & Diagnostics (PowerShell)
# Probes all 7 microservices & frontend running locally without Docker
# ==============================================================================

[CmdletBinding()]
param (
    [int]$TimeoutSeconds = 3,
    [switch]$VerboseOutput
)

$services = @(
    @{ Name = "Prompt Security Scanner"; Port = 8081; Url = "http://localhost:8081/api/v1/scan/health"; Primary = $false },
    @{ Name = "Risk Scoring Engine";     Port = 8082; Url = "http://localhost:8082/api/v1/risk/health"; Primary = $false },
    @{ Name = "Audit Service (H2 DB)";   Port = 8083; Url = "http://localhost:8083/health";             Primary = $false },
    @{ Name = "Response Quality Checker";Port = 8084; Url = "http://localhost:8084/api/v1/quality/health"; Primary = $false },
    @{ Name = "AI Governance Service";   Port = 8085; Url = "http://localhost:8085/api/v1/governance/health"; Primary = $false },
    @{ Name = "AI Gateway (Ingress)";    Port = 8080; Url = "http://localhost:8080/health";             Primary = $true },
    @{ Name = "React Frontend (Vite)";   Port = 3000; Url = "http://localhost:3000/";                   Primary = $true }
)

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Yellow
Write-Host "   SDS ENTERPRISE AI GUARDIAN - SERVICE HEALTH DIAGNOSTICS            " -ForegroundColor Yellow
Write-Host "   Mode: Local Development (Java 17 + Node.js - No Docker)            " -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Yellow
Write-Host ""

$allHealthy = $true
$results = @()

foreach ($svc in $services) {
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $statusText = "DOWN"
    $statusCode = 0
    $color = "Red"

    try {
        $req = [System.Net.HttpWebRequest]::Create($svc.Url)
        $req.Timeout = $TimeoutSeconds * 1000
        $req.Method = "GET"
        
        $response = $req.GetResponse()
        $stopwatch.Stop()
        $statusCode = [int]$response.StatusCode
        $response.Close()

        if ($statusCode -ge 200 -and $statusCode -lt 400) {
            $statusText = "HEALTHY"
            $color = "Green"
        } else {
            $statusText = "HTTP $statusCode"
            $color = "Yellow"
            $allHealthy = $false
        }
    }
    catch [System.Net.WebException] {
        $stopwatch.Stop()
        if ($_.Response) {
            $statusCode = [int]$_.Response.StatusCode
            $statusText = "HTTP $statusCode"
            $color = "Yellow"
        } else {
            $statusText = "UNREACHABLE"
            $color = "Red"
        }
        $allHealthy = $false
    }
    catch {
        $stopwatch.Stop()
        $statusText = "ERROR"
        $color = "Red"
        $allHealthy = $false
    }

    $latencyMs = [math]::Round($stopwatch.Elapsed.TotalMilliseconds)

    Write-Host ("  [{0,-11}] {1,-28} (Port {2,4}) -> {3} ms" -f $statusText, $svc.Name, $svc.Port, $latencyMs) -ForegroundColor $color

    $results += [PSCustomObject]@{
        Service = $svc.Name
        Port = $svc.Port
        Status = $statusText
        LatencyMs = $latencyMs
        Url = $svc.Url
    }
}

Write-Host ""
Write-Host "----------------------------------------------------------------------" -ForegroundColor DarkGray

if ($allHealthy) {
    Write-Host "  >>> ALL SERVICES ARE OPERATIONAL AND HEALTHY! <<<" -ForegroundColor Green
    Write-Host "  Dashboard: http://localhost:3000" -ForegroundColor Cyan
    Write-Host "  Gateway:   http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
    Write-Host ""
    exit 0
} else {
    Write-Host "  [WARN] Some services are currently unavailable or still starting up." -ForegroundColor Yellow
    Write-Host "  Inspect log files in .\logs\ for diagnostic details." -ForegroundColor DarkGray
    Write-Host ""
    exit 1
}
