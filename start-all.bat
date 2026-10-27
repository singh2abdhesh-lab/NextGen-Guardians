@echo off
setlocal enabledelayedexpansion

REM ==============================================================================
REM Enterprise AI Guardian (SDS) - Windows Local Startup Launcher (DigithonG)
REM Single-command execution: Validates ports, starts services in order, waits for health
REM ==============================================================================

echo ==============================================================================
echo [AI Guardian] Initiating SDS Enterprise AI Guardian Local Stack (DigithonG)...
echo [AI Guardian] Environment: Java 17 + Maven + Node.js (Zero Docker Required)
echo ==============================================================================

REM Check if PowerShell is available
where powershell >nul 2>nul
if %errorlevel% equ 0 (
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-all.ps1" %*
    exit /b %errorlevel%
)

REM Fallback if powershell is not directly reachable
echo [AI Guardian] ERROR: powershell.exe is required for process orchestration.
exit /b 1
