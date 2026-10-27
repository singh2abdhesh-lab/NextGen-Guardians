# ==============================================================================
# Enterprise AI Guardian - Local Stack Teardown Script (PowerShell)
# Gracefully stops all microservices & Vite frontend without Docker
# ==============================================================================

[CmdletBinding()]
param ()

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Yellow
Write-Host "   SDS ENTERPRISE AI GUARDIAN - SHUTTING DOWN LOCAL STACK             " -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Yellow
Write-Host ""

$ports = @(8080, 8081, 8082, 8083, 8084, 8085, 3000)
$stoppedCount = 0

# 1. Stop processes via saved PID files in logs/
$pidDir = Join-Path $PSScriptRoot "logs"
if (Test-Path $pidDir) {
    Get-ChildItem -Path $pidDir -Filter "*.pid" | ForEach-Object {
        $svcName = $_.BaseName
        $pidVal = Get-Content $_.FullName -ErrorAction SilentlyContinue
        if ($pidVal -and ($pidVal -match '^\d+$')) {
            $pIdInt = [int]$pidVal
            try {
                $proc = Get-Process -Id $pIdInt -ErrorAction Stop
                Write-Host "  Stopping $svcName (PID $pIdInt)..." -ForegroundColor Yellow
                Stop-Process -Id $pIdInt -Force -ErrorAction SilentlyContinue
                $stoppedCount++
            } catch {
                # Process might have already terminated
            }
        }
        Remove-Item $_.FullName -Force -ErrorAction SilentlyContinue
    }
}

# 2. Kill any remaining processes actively bound to target ports
foreach ($port in $ports) {
    try {
        $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($conns) {
            foreach ($conn in $conns) {
                $owningPid = $conn.OwningProcess
                if ($owningPid -gt 0) {
                    try {
                        $p = Get-Process -Id $owningPid -ErrorAction Stop
                        Write-Host "  Terminating process on port $port ($($p.ProcessName), PID $owningPid)..." -ForegroundColor Yellow
                        Stop-Process -Id $owningPid -Force -ErrorAction SilentlyContinue
                        $stoppedCount++
                    } catch {
                        # Ignore
                    }
                }
            }
        }
    } catch {
        # Fallback to netstat if Get-NetTCPConnection is unavailable
        $netstatOut = netstat -ano | Select-String ":$port\s+.*LISTENING\s+(\d+)"
        foreach ($match in $netstatOut) {
            if ($match.Line -match ":$port\s+.*LISTENING\s+(\d+)") {
                $targetPid = $matches[1]
                try {
                    taskkill /F /PID $targetPid >$null 2>&1
                    $stoppedCount++
                } catch {}
            }
        }
    }
}

Start-Sleep -Seconds 2

# 3. Verify all ports are released
Write-Host ""
Write-Host "Verifying released ports:" -ForegroundColor DarkGray
$allClean = $true
foreach ($port in $ports) {
    $stillActive = $false
    try {
        $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($conns) { $stillActive = $true }
    } catch {
        $out = netstat -ano | Select-String ":$port\s+.*LISTENING"
        if ($out) { $stillActive = $true }
    }

    if ($stillActive) {
        Write-Host "  [WARN] Port $port is still reported in use!" -ForegroundColor Red
        $allClean = $false
    } else {
        Write-Host "  [CLEAN] Port $port is free." -ForegroundColor Green
    }
}

Write-Host ""
if ($allClean) {
    Write-Host "======================================================================" -ForegroundColor Green
    Write-Host ">>> ALL ENTERPRISE AI GUARDIAN SERVICES SHUT DOWN SUCCESSFULLY! <<<" -ForegroundColor Green
    Write-Host "======================================================================" -ForegroundColor Green
} else {
    Write-Host "[NOTICE] Some ports may still have lingering connections. Re-run stop-all.ps1 if needed." -ForegroundColor Yellow
}
Write-Host ""
