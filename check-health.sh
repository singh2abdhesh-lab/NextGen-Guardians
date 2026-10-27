#!/usr/bin/env bash
# ==============================================================================
# Enterprise AI Guardian - Health Check & Diagnostics (Bash)
# Probes all 7 microservices & frontend running locally without Docker
# ==============================================================================

SAFFRON='\033[38;5;208m'
GREEN='\033[0;32m'
AMBER='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${SAFFRON}"
echo "======================================================================"
echo "   SDS ENTERPRISE AI GUARDIAN - SERVICE HEALTH DIAGNOSTICS            "
echo "   Mode: Local Development (Java 17 + Node.js - No Docker)            "
echo "======================================================================"
echo -e "${NC}"

ALL_HEALTHY=true

check_endpoint() {
    local name="$1"
    local port="$2"
    local url="$3"

    start_time=$(date +%s%N 2>/dev/null || date +%s)
    http_code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 3 "$url" 2>/dev/null || echo "000")
    end_time=$(date +%s%N 2>/dev/null || date +%s)
    
    if [[ "$http_code" -ge 200 && "$http_code" -lt 400 ]]; then
        printf "  [${GREEN}HEALTHY    ${NC}] %-28s (Port %4s) -> HTTP %s\n" "$name" "$port" "$http_code"
    else
        printf "  [${RED}UNAVAILABLE${NC}] %-28s (Port %4s) -> HTTP %s (%s)\n" "$name" "$port" "$http_code" "$url"
        ALL_HEALTHY=false
    fi
}

check_endpoint "Prompt Security Scanner" "8081" "http://localhost:8081/api/v1/scan/health"
check_endpoint "Risk Scoring Engine"     "8082" "http://localhost:8082/api/v1/risk/health"
check_endpoint "Audit Service (H2 DB)"   "8083" "http://localhost:8083/health"
check_endpoint "Response Quality Checker" "8084" "http://localhost:8084/api/v1/quality/health"
check_endpoint "AI Governance Service"   "8085" "http://localhost:8085/api/v1/governance/health"
check_endpoint "AI Gateway (Ingress)"    "8080" "http://localhost:8080/health"
check_endpoint "React Frontend (Vite)"   "3000" "http://localhost:3000/"

echo ""
echo "----------------------------------------------------------------------"
if [ "$ALL_HEALTHY" = true ]; then
    echo -e "${GREEN}  >>> ALL SERVICES ARE OPERATIONAL AND HEALTHY! <<<${NC}"
    echo -e "  Dashboard: ${CYAN}http://localhost:3000${NC}"
    echo -e "  Gateway:   ${CYAN}http://localhost:8080/swagger-ui.html${NC}"
    echo ""
    exit 0
else
    echo -e "${AMBER}  [WARN] One or more services are not reporting healthy.${NC}"
    echo "  Check log files in ./logs/ for detailed error output."
    echo ""
    exit 1
fi
