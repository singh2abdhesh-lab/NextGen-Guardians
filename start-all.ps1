# ==============================================================================
# Enterprise AI Guardian - Unified Local Startup Script (PowerShell)
# Starts all 7 microservices & Vite frontend locally without Docker
# Prerequisites: Java 17+, Maven 3.8+, Node.js 18+
# ==============================================================================

[CmdletBinding()]
param (
    [switch]$Build,
    [switch]$CleanLogs,
    [int]$StartupTimeoutSeconds = 45
)

$ErrorActionPreference = "Stop"
$rootDir = $PSScriptRoot

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Yellow
Write-Host "   ____ ___  ____     Enterprise AI Guardian (SDS)" -ForegroundColor Yellow
Write-Host "  / __// _ \/ __/     AI Governance & Security Control Tower" -ForegroundColor Yellow
Write-Host " _\ \ / // /\ \       " -ForegroundColor Yellow
Write-Host "/___//____//___/      SDS Enterprise AI Guardian" -ForegroundColor Yellow
Write-Host "                      Production Local Control Tower (Zero Docker)" -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Yellow
Write-Host ""

# ------------------------------------------------------------------------------
# 1. Environment & Prerequisites Validation
# ------------------------------------------------------------------------------
Write-Host "[1/6] Verifying Local Prerequisites (Java 17, Maven, Node.js)..." -ForegroundColor Yellow

# Java check
try {
    $javaVer = java -version 2>&1 | Out-String
    if ($javaVer -match 'version "(\d+)') {
        $major = [int]$matches[1]
        if ($major -lt 17) {
            Write-Host "  [ERROR] Java version must be 17 or higher. Detected Java $major." -ForegroundColor Red
            exit 1
        }
    }
    Write-Host "  [OK] Java Runtime detected." -ForegroundColor Green
} catch {
    Write-Host "  [ERROR] Java is not installed or not in PATH." -ForegroundColor Red
    exit 1
}

# Maven check
try {
    $mvnVer = mvn -v 2>&1 | Select-Object -First 1
    Write-Host "  [OK] Apache Maven detected ($mvnVer)." -ForegroundColor Green
} catch {
    Write-Host "  [ERROR] Maven (mvn) is not installed or not in PATH." -ForegroundColor Red
    exit 1
}

# Node.js check
try {
    $nodeVer = node -v 2>&1
    Write-Host "  [OK] Node.js runtime detected ($nodeVer)." -ForegroundColor Green
} catch {
    Write-Host "  [ERROR] Node.js is not installed or not in PATH." -ForegroundColor Red
    exit 1
}

# ------------------------------------------------------------------------------
# 2. Port Validation & Conflict Resolution
# ------------------------------------------------------------------------------
Write-Host "[2/6] Validating Target Ports (8080-8085, 3000)..." -ForegroundColor Yellow
$ports = @(8080, 8081, 8082, 8083, 8084, 8085, 3000)
$occupied = @()

foreach ($port in $ports) {
    $inUse = $false
    try {
        $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($conns) { $inUse = $true }
    } catch {
        $netstat = netstat -ano | Select-String ":$port\s+.*LISTENING"
        if ($netstat) { $inUse = $true }
    }
    if ($inUse) { $occupied += $port }
}

if ($occupied.Count -gt 0) {
    Write-Host "  [CONFLICT] The following ports are already in use: $($occupied -join ', ')" -ForegroundColor Yellow
    Write-Host "  Automatically clearing previous instances via stop-all.ps1..." -ForegroundColor Yellow
    & "$rootDir\stop-all.ps1" | Out-Null
    Start-Sleep -Seconds 2
} else {
    Write-Host "  [OK] All target ports are clear." -ForegroundColor Green
}

# ------------------------------------------------------------------------------
# 3. Log Directory Setup
# ------------------------------------------------------------------------------
$logDir = Join-Path $rootDir "logs"
if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir | Out-Null
}
if ($CleanLogs) {
    Get-ChildItem -Path $logDir -Filter "*.log" | Remove-Item -Force -ErrorAction SilentlyContinue
    Write-Host "  [OK] Log directory cleaned." -ForegroundColor DarkGray
}

# ------------------------------------------------------------------------------
# 4. Compile & Package Modules (if needed or requested)
# ------------------------------------------------------------------------------
$backendModules = @(
    "prompt-security-scanner",
    "risk-scoring-engine",
    "audit-service",
    "response-quality-checker",
    "governance-service",
    "ai-gateway"
)

$needsBuild = $Build
foreach ($mod in $backendModules) {
    $targetDir = Join-Path $rootDir "$mod\target"
    $jars = Get-ChildItem -Path $targetDir -Filter "*.jar" -ErrorAction SilentlyContinue | Where-Object { $_.Name -notmatch "original" }
    if (-not $jars) {
        $needsBuild = $true
        break
    }
}

if ($needsBuild) {
    Write-Host "[3/6] Compiling and packaging Maven modules (H2 DB & Zero Docker profiles)..." -ForegroundColor Yellow
    Push-Location $rootDir
    try {
        mvn clean package -DskipTests
        if ($LASTEXITCODE -ne 0) {
            Write-Host "  [ERROR] Maven build failed." -ForegroundColor Red
            Pop-Location
            exit 1
        }
        Write-Host "  [OK] Maven build completed successfully." -ForegroundColor Green
    } finally {
        Pop-Location
    }
} else {
    Write-Host "[3/6] Existing build artifacts verified (skipping mvn compile; use -Build to force rebuild)." -ForegroundColor Green
}

# Frontend npm dependencies check
$frontendDir = Join-Path $rootDir "frontend"
if (-not (Test-Path (Join-Path $frontendDir "node_modules"))) {
    Write-Host "  Installing frontend npm dependencies..." -ForegroundColor Yellow
    Push-Location $frontendDir
    try {
        npm install --silent
    } finally {
        Pop-Location
    }
}

# ------------------------------------------------------------------------------
# 5. Launch Microservices in Dependency Order
# ------------------------------------------------------------------------------
Write-Host "[4/6] Launching Backend Microservices in Dependency Order..." -ForegroundColor Yellow

function Start-JavaService {
    param(
        [string]$ModuleName,
        [int]$Port,
        [string]$HealthUrl
    )

    $modPath = Join-Path $rootDir $ModuleName
    $jar = Get-ChildItem -Path (Join-Path $modPath "target") -Filter "*.jar" | Where-Object { $_.Name -notmatch "original" } | Select-Object -First 1
    if (-not $jar) {
        Write-Host "  [ERROR] Could not find JAR file for $ModuleName" -ForegroundColor Red
        return $null
    }

    $outLog = Join-Path $logDir "$ModuleName.log"
    $errLog = Join-Path $logDir "$ModuleName.err.log"
    $pidFile = Join-Path $logDir "$ModuleName.pid"

    # Set local development JVM parameters
    $javaArgs = @(
        "-Dspring.profiles.active=dev",
        "-Dspring.main.allow-bean-definition-overriding=true",
        "-Dserver.port=$Port",
        "-DKAFKA_ENABLED=false",
        "-DKAFKA_AUTO_STARTUP=false",
        "-DAI_UPSTREAM_MOCK=true",
        "-Dspring.datasource.url=jdbc:h2:mem:${ModuleName}db;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "-Dspring.datasource.driver-class-name=org.h2.Driver",
        "-Dspring.datasource.username=sa",
        "-Dspring.datasource.password=",
        "-jar",
        $jar.FullName
    )

    $pInfo = New-Object System.Diagnostics.ProcessStartInfo
    $pInfo.FileName = "java"
    $pInfo.Arguments = ($javaArgs -join " ")
    $pInfo.WorkingDirectory = $rootDir
    $pInfo.RedirectStandardOutput = $true
    $pInfo.RedirectStandardError = $true
    $pInfo.UseShellExecute = $false
    $pInfo.CreateNoWindow = $true

    $proc = New-Object System.Diagnostics.Process
    $proc.StartInfo = $pInfo

    # Redirect to log files
    $outStream = [System.IO.StreamWriter]::new($outLog, $false)
    $errStream = [System.IO.StreamWriter]::new($errLog, $false)

    Register-ObjectEvent -InputObject $proc -EventName OutputDataReceived -Action {
        if ($Event.SourceEventArgs.Data) {
            $Event.MessageData.Out.WriteLine($Event.SourceEventArgs.Data)
            $Event.MessageData.Out.Flush()
        }
    } -MessageData @{ Out = $outStream } | Out-Null

    Register-ObjectEvent -InputObject $proc -EventName ErrorDataReceived -Action {
        if ($Event.SourceEventArgs.Data) {
            $Event.MessageData.Err.WriteLine($Event.SourceEventArgs.Data)
            $Event.MessageData.Err.Flush()
        }
    } -MessageData @{ Err = $errStream } | Out-Null

    $proc.Start() | Out-Null
    $proc.BeginOutputReadLine()
    $proc.BeginErrorReadLine()

    $proc.Id | Out-File -FilePath $pidFile -Force

    Write-Host "  Started $ModuleName on Port $Port (PID $($proc.Id)) -> logs\$ModuleName.log" -ForegroundColor Cyan
    return $proc
}

function Wait-ForHealth {
    param(
        [string]$Name,
        [string]$Url,
        [int]$TimeoutSec = 30
    )

    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    while ($sw.Elapsed.TotalSeconds -lt $TimeoutSec) {
        try {
            $req = [System.Net.HttpWebRequest]::Create($Url)
            $req.Timeout = 1500
            $res = $req.GetResponse()
            $code = [int]$res.StatusCode
            $res.Close()
            if ($code -ge 200 -and $code -lt 400) {
                Write-Host "    [READY] $Name is healthy ($([math]::Round($sw.Elapsed.TotalSeconds, 1))s)" -ForegroundColor Green
                return $true
            }
        } catch {
            Start-Sleep -Milliseconds 700
        }
    }
    Write-Host "    [WARNING] $Name did not report ready within $TimeoutSec seconds ($Url)" -ForegroundColor Yellow
    return $false
}

# --- Step 1: Utility / Core Assessment Services ---
Write-Host "  Phase A: Starting core assessment microservices & H2 datastores..." -ForegroundColor DarkCyan
Start-JavaService -ModuleName "prompt-security-scanner" -Port 8081
Start-JavaService -ModuleName "risk-scoring-engine" -Port 8082
Start-JavaService -ModuleName "response-quality-checker" -Port 8084
Start-JavaService -ModuleName "audit-service" -Port 8083
Start-JavaService -ModuleName "governance-service" -Port 8085

Write-Host "  Awaiting health probes for core services..." -ForegroundColor DarkGray
Wait-ForHealth -Name "Security Scanner (8081)" -Url "http://localhost:8081/api/v1/scan/health"
Wait-ForHealth -Name "Risk Engine (8082)"     -Url "http://localhost:8082/api/v1/risk/health"
Wait-ForHealth -Name "Quality Checker (8084)" -Url "http://localhost:8084/api/v1/quality/health"
Wait-ForHealth -Name "Audit Service (8083)"   -Url "http://localhost:8083/health"
Wait-ForHealth -Name "Governance Service (8085)" -Url "http://localhost:8085/api/v1/governance/health"

# --- Step 2: Gateway Ingress ---
Write-Host "  Phase B: Starting Reactive AI Gateway orchestrator (Port 8080)..." -ForegroundColor DarkCyan
Start-JavaService -ModuleName "ai-gateway" -Port 8080
Wait-ForHealth -Name "AI Gateway (8080)" -Url "http://localhost:8080/health"

# ------------------------------------------------------------------------------
# 6. Launch React Frontend (Vite)
# ------------------------------------------------------------------------------
Write-Host "[5/6] Launching React Governance Frontend on Port 3000..." -ForegroundColor Yellow

$frontOut = Join-Path $logDir "frontend.log"
$frontErr = Join-Path $logDir "frontend.err.log"
$frontPidFile = Join-Path $logDir "frontend.pid"

# Launch Vite dev server via Node
$fInfo = New-Object System.Diagnostics.ProcessStartInfo
$fInfo.FileName = "node"
$fInfo.Arguments = ".\node_modules\vite\bin\vite.js --host 0.0.0.0 --port 3000"
$fInfo.WorkingDirectory = $frontendDir
$fInfo.RedirectStandardOutput = $true
$fInfo.RedirectStandardError = $true
$fInfo.UseShellExecute = $false
$fInfo.CreateNoWindow = $true

$frontProc = New-Object System.Diagnostics.Process
$frontProc.StartInfo = $fInfo

$fOutStream = [System.IO.StreamWriter]::new($frontOut, $false)
$fErrStream = [System.IO.StreamWriter]::new($frontErr, $false)

Register-ObjectEvent -InputObject $frontProc -EventName OutputDataReceived -Action {
    if ($Event.SourceEventArgs.Data) {
        $Event.MessageData.Out.WriteLine($Event.SourceEventArgs.Data)
        $Event.MessageData.Out.Flush()
    }
} -MessageData @{ Out = $fOutStream } | Out-Null

Register-ObjectEvent -InputObject $frontProc -EventName ErrorDataReceived -Action {
    if ($Event.SourceEventArgs.Data) {
        $Event.MessageData.Err.WriteLine($Event.SourceEventArgs.Data)
        $Event.MessageData.Err.Flush()
    }
} -MessageData @{ Err = $fErrStream } | Out-Null

$frontProc.Start() | Out-Null
$frontProc.BeginOutputReadLine()
$frontProc.BeginErrorReadLine()

$frontProc.Id | Out-File -FilePath $frontPidFile -Force
Write-Host "  Started Vite Frontend on Port 3000 (PID $($frontProc.Id)) -> logs\frontend.log" -ForegroundColor Cyan

Wait-ForHealth -Name "React Frontend (3000)" -Url "http://localhost:3000/" -TimeoutSec 15 | Out-Null

# ------------------------------------------------------------------------------
# Diagnostics Verification
# ------------------------------------------------------------------------------
Write-Host "[6/6] Running System Verification Diagnostics..." -ForegroundColor Yellow
& "$rootDir\check-health.ps1"

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Green
Write-Host ">>> SDS ENTERPRISE AI GUARDIAN PLATFORM RUNNING SUCCESSFULLY! <<<<" -ForegroundColor Green
Write-Host "======================================================================" -ForegroundColor Green
Write-Host "  ● React Dashboard:       http://localhost:3000" -ForegroundColor Yellow
Write-Host "  ● AI Gateway Swagger:    http://localhost:8080/swagger-ui.html" -ForegroundColor Yellow
Write-Host "  ● Security Scanner:      http://localhost:8081/swagger-ui.html" -ForegroundColor Yellow
Write-Host "  ● Risk Engine Swagger:   http://localhost:8082/swagger-ui.html" -ForegroundColor Yellow
Write-Host "  ● Audit Service Swagger: http://localhost:8083/swagger-ui.html" -ForegroundColor Yellow
Write-Host "  ● Quality Checker:       http://localhost:8084/swagger-ui.html" -ForegroundColor Yellow
Write-Host "  ● Governance Suite:      http://localhost:8085/swagger-ui.html" -ForegroundColor Yellow
Write-Host "  ● In-Memory H2 DB:       http://localhost:8083/h2-console" -ForegroundColor Yellow
Write-Host "                           (JDBC URL: jdbc:h2:mem:auditdb, User: sa, Pass: [blank])" -ForegroundColor DarkGray
Write-Host ""
Write-Host "  Runtime Architecture:" -ForegroundColor DarkGray
Write-Host "  - Storage: In-Memory H2 Database (No external Postgres needed)" -ForegroundColor DarkGray
Write-Host "  - Event Bus: Kafka REST Fallback Mode Active (No broker needed)" -ForegroundColor DarkGray
Write-Host "  - LLM Provider: Mock Pipeline Active (Zero cost / zero external API calls)" -ForegroundColor DarkGray
Write-Host "  - Logs: Individual service stdout/stderr piped to .\logs\*.log" -ForegroundColor DarkGray
Write-Host ""
Write-Host "To check health anytime:   .\check-health.ps1" -ForegroundColor Yellow
Write-Host "To shut down cleanly:      .\stop-all.ps1 or stop-all.bat" -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Green
Write-Host ""
