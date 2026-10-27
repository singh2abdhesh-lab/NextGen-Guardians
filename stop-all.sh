#!/usr/bin/env bash
# ==============================================================================
# Enterprise AI Guardian - Local Stack Teardown Script (Linux / macOS / Git Bash)
# Gracefully stops all microservices & Vite frontend without Docker
# ==============================================================================

SAFFRON='\033[38;5;208m'
GREEN='\033[0;32m'
AMBER='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m'

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="${ROOT_DIR}/logs"

echo -e "${SAFFRON}"
echo "======================================================================"
echo "   SDS ENTERPRISE AI GUARDIAN - SHUTTING DOWN LOCAL STACK             "
echo "======================================================================"
echo -e "${NC}"

# 1. Stop processes via saved PID files
if [ -d "${LOG_DIR}" ]; then
    for pid_file in "${LOG_DIR}"/*.pid; do
        if [ -f "$pid_file" ]; then
            svc_name=$(basename "$pid_file" .pid)
            pid=$(cat "$pid_file" 2>/dev/null || true)
            if [ -n "$pid" ]; then
                echo -e "  Stopping ${AMBER}${svc_name}${NC} (PID ${pid})..."
                if command -v taskkill.exe >/dev/null 2>&1; then
                    taskkill.exe //F //PID "$pid" >/dev/null 2>&1 || true
                elif command -v taskkill >/dev/null 2>&1; then
                    taskkill //F //PID "$pid" >/dev/null 2>&1 || true
                fi
                kill -15 "$pid" 2>/dev/null || kill -9 "$pid" 2>/dev/null || true
            fi
            rm -f "$pid_file"
        fi
    done
fi

# 2. Port-based termination fallback
PORTS=(8080 8081 8082 8083 8084 8085 3000)
for port in "${PORTS[@]}"; do
    if command -v lsof >/dev/null 2>&1; then
        pids=$(lsof -i ":$port" -sTCP:LISTEN -t 2>/dev/null || true)
        if [ -n "$pids" ]; then
            for p in $pids; do
                echo -e "  Terminating process on port ${port} (PID ${p})..."
                kill -9 "$p" 2>/dev/null || true
            done
        fi
    elif command -v netstat >/dev/null 2>&1; then
        pids=$(netstat -ano | grep ":$port " | grep "LISTENING" | awk '{print $NF}' | sort -u || true)
        if [ -n "$pids" ]; then
            for p in $pids; do
                echo -e "  Terminating process on port ${port} (PID ${p})..."
                if command -v taskkill.exe >/dev/null 2>&1; then
                    taskkill.exe //F //PID "$p" >/dev/null 2>&1 || true
                else
                    taskkill //F //PID "$p" >/dev/null 2>&1 || true
                fi
            done
        fi
    fi
done

sleep 1

# 3. Verify all ports are released
echo ""
echo "Verifying released ports:"
ALL_CLEAN=true
for port in "${PORTS[@]}"; do
    if lsof -i ":$port" -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "  [${RED}WARN ${NC}] Port ${port} is still reported active."
        ALL_CLEAN=false
    else
        echo -e "  [${GREEN}CLEAN${NC}] Port ${port} is free."
    fi
done

echo ""
if [ "$ALL_CLEAN" = true ]; then
    echo -e "${GREEN}======================================================================${NC}"
    echo -e "${GREEN}>>> ALL ENTERPRISE AI GUARDIAN SERVICES SHUT DOWN SUCCESSFULLY! <<<${NC}"
    echo -e "${GREEN}======================================================================${NC}"
else
    echo -e "${AMBER}[NOTICE] Some ports may still have lingering processes. Re-run ./stop-all.sh if needed.${NC}"
fi
echo ""
