@echo off
setlocal enabledelayedexpansion

REM ==============================================================================
REM Enterprise AI Guardian (SDS) - Windows Local Stack Shutdown Script (DigithonG)
REM Terminates all microservices (Ports 8080-8085) and Vite frontend (Port 3000)
REM ==============================================================================

echo ==============================================================================
echo [AI Guardian] Initiating Clean Local Stack Shutdown (DigithonG)...
echo ==============================================================================

REM Check if PowerShell is available
where powershell >nul 2>nul
if %errorlevel% equ 0 (
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop-all.ps1" %*
    exit /b %errorlevel%
)

REM Native batch fallback termination
echo [AI Guardian] Stopping services using native taskkill on target ports...
for %%P in (8080 8081 8082 8083 8084 8085 3000) do (
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr /r /c:":%%P .*LISTENING"') do (
        echo Killing process on port %%P (PID %%a)...
        taskkill /F /PID %%a >nul 2>&1
    )
)

echo [AI Guardian] All services terminated.
exit /b 0
