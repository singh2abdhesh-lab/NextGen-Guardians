#!/usr/bin/env bash
# ==============================================================================
# Enterprise AI Guardian - Unified Local Startup Script (Linux / macOS / Git Bash)
# Launches all 7 microservices & Vite frontend locally without Docker
# Prerequisites: Java 17+, Maven 3.8+, Node.js 18+
# ==============================================================================
set -e

SAFFRON='\033[38;5;208m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
AMBER='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m'

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="${ROOT_DIR}/logs"

echo -e "${SAFFRON}"
echo "======================================================================"
echo "   ____ ___  ____     Enterprise AI Guardian (SDS)"
echo "  / __// _ \/ __/     AI Governance & Security Control Tower"
echo " _\ \ / // /\ \       "
echo "/___//____//___/      SDS Enterprise AI Guardian"
echo "                      Production Local Control Tower (Zero Docker)"
echo "======================================================================"
echo -e "${NC}"

# ------------------------------------------------------------------------------
# 1. Environment & Prerequisites Validation
# ------------------------------------------------------------------------------
echo -e "${AMBER}[1/6] Verifying Local Prerequisites (Java 17, Maven, Node.js)...${NC}"

if ! command -v java &> /dev/null; then
    echo -e "${RED}[ERROR] java is not installed or not in system PATH.${NC}"
    exit 1
fi

JAVA_MAJOR=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$JAVA_MAJOR" -lt 17 ]; then
    echo -e "${RED}[ERROR] Java 17 or higher is required. Found version: $JAVA_MAJOR${NC}"
    exit 1
fi
echo -e "${GREEN}  [OK] Java Runtime detected (Java $JAVA_MAJOR).${NC}"

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}[ERROR] Apache Maven (mvn) is not installed or not in PATH.${NC}"
    exit 1
fi
echo -e "${GREEN}  [OK] Apache Maven detected.${NC}"

if ! command -v node &> /dev/null; then
    echo -e "${RED}[ERROR] Node.js is not installed or not in PATH.${NC}"
    exit 1
fi
echo -e "${GREEN}  [OK] Node.js runtime detected ($(node -v)).${NC}"

# ------------------------------------------------------------------------------
# 2. Port Validation & Conflict Resolution
# ------------------------------------------------------------------------------
echo -e "${AMBER}[2/6] Validating Target Ports (8080-8085, 3000)...${NC}"
PORTS=(8080 8081 8082 8083 8084 8085 3000)
OCCUPIED=()

for port in "${PORTS[@]}"; do
    if lsof -i ":$port" -sTCP:LISTEN -t >/dev/null 2>&1 || netstat -an 2>/dev/null | grep -q ":$port .*LISTEN"; then
        OCCUPIED+=("$port")
    fi
done

if [ ${#OCCUPIED[@]} -gt 0 ]; then
    echo -e "${AMBER}  [CONFLICT] Ports in use: ${OCCUPIED[*]}. Running stop-all.sh to clear previous instances...${NC}"
    bash "${ROOT_DIR}/stop-all.sh" >/dev/null 2>&1 || true
    sleep 2
else
    echo -e "${GREEN}  [OK] All target ports are clear.${NC}"
fi

# ------------------------------------------------------------------------------
# 3. Log Directory Setup
# ------------------------------------------------------------------------------
mkdir -p "${LOG_DIR}"

BUILD_FLAG=false
if [[ "$1" == "--build" ]] || [[ "$1" == "-b" ]]; then
    BUILD_FLAG=true
fi

# ------------------------------------------------------------------------------
# 4. Compile & Package Modules
# ------------------------------------------------------------------------------
MODULES=(
    "prompt-security-scanner"
    "risk-scoring-engine"
    "audit-service"
    "response-quality-checker"
    "governance-service"
    "ai-gateway"
)

NEEDS_BUILD=$BUILD_FLAG
for mod in "${MODULES[@]}"; do
    if ! ls "${ROOT_DIR}/${mod}/target/"*.jar 1> /dev/null 2>&1; then
        NEEDS_BUILD=true
        break
    fi
done

if [ "$NEEDS_BUILD" = true ]; then
    echo -e "${AMBER}[3/6] Compiling and packaging Maven modules (H2 DB & Zero Docker mode)...${NC}"
    cd "${ROOT_DIR}"
    mvn clean package -DskipTests
    echo -e "${GREEN}  [OK] Maven build completed successfully.${NC}"
else
    echo -e "${GREEN}[3/6] Existing build artifacts verified (skipping mvn compile; use --build to recompile).${NC}"
fi

# Frontend npm dependencies
if [ ! -d "${ROOT_DIR}/frontend/node_modules" ]; then
    echo -e "${AMBER}  Installing frontend npm dependencies...${NC}"
    cd "${ROOT_DIR}/frontend"
    npm install --silent
    cd "${ROOT_DIR}"
fi

# ------------------------------------------------------------------------------
# 5. Launch Microservices in Dependency Order
# ------------------------------------------------------------------------------
echo -e "${AMBER}[4/6] Launching Backend Microservices in Dependency Order...${NC}"

start_java_service() {
    local mod="$1"
    local port="$2"

    local jar_path=$(ls "${ROOT_DIR}/${mod}/target/"*.jar 2>/dev/null | grep -v "original" | head -n 1)
    if [ -z "$jar_path" ]; then
        echo -e "${RED}[ERROR] JAR not found for $mod${NC}"
        return 1
    fi

    local out_log="${LOG_DIR}/${mod}.log"
    local pid_file="${LOG_DIR}/${mod}.pid"

    nohup java \
        -Dspring.profiles.active=dev \
        -Dspring.main.allow-bean-definition-overriding=true \
        -Dserver.port="${port}" \
        -DKAFKA_ENABLED=false \
        -DKAFKA_AUTO_STARTUP=false \
        -DAI_UPSTREAM_MOCK=true \
        -Dspring.datasource.url="jdbc:h2:mem:${mod}db;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE" \
        -Dspring.datasource.driver-class-name=org.h2.Driver \
        -Dspring.datasource.username=sa \
        -Dspring.datasource.password= \
        -jar "${jar_path}" > "${out_log}" 2>&1 &

    local pid=$!
    echo "$pid" > "${pid_file}"
    echo -e "${CYAN}  Started ${mod} on Port ${port} (PID ${pid}) -> logs/${mod}.log${NC}"
}

wait_for_health() {
    local name="$1"
    local url="$2"
    local max_wait=30
    local elapsed=0

    while [ $elapsed -lt $max_wait ]; do
        if curl -s -f "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}    [READY] ${name} is operational (${elapsed}s)${NC}"
            return 0
        fi
        sleep 1
        elapsed=$((elapsed + 1))
    done
    echo -e "${AMBER}    [WARNING] ${name} did not respond within ${max_wait}s (${url})${NC}"
    return 0
}

# --- Step 1: Utility / Core Assessment Services ---
echo -e "${SAFFRON}  Phase A: Starting core assessment microservices & H2 datastores...${NC}"
start_java_service "prompt-security-scanner" 8081
start_java_service "risk-scoring-engine" 8082
start_java_service "response-quality-checker" 8084
start_java_service "audit-service" 8083
start_java_service "governance-service" 8085

echo -e "  Awaiting health probes for core services..."
wait_for_health "Security Scanner (8081)" "http://localhost:8081/api/v1/scan/health"
wait_for_health "Risk Engine (8082)"     "http://localhost:8082/api/v1/risk/health"
wait_for_health "Quality Checker (8084)" "http://localhost:8084/api/v1/quality/health"
wait_for_health "Audit Service (8083)"   "http://localhost:8083/health"
wait_for_health "Governance Service (8085)" "http://localhost:8085/api/v1/governance/health"

# --- Step 2: Gateway Ingress ---
echo -e "${SAFFRON}  Phase B: Starting Reactive AI Gateway orchestrator (Port 8080)...${NC}"
start_java_service "ai-gateway" 8080
wait_for_health "AI Gateway (8080)" "http://localhost:8080/health"

# ------------------------------------------------------------------------------
# 6. Launch React Frontend (Vite)
# ------------------------------------------------------------------------------
echo -e "${AMBER}[5/6] Launching React Governance Frontend on Port 3000...${NC}"
cd "${ROOT_DIR}/frontend"
nohup node ./node_modules/vite/bin/vite.js --host 0.0.0.0 --port 3000 > "${LOG_DIR}/frontend.log" 2>&1 &
FRONT_PID=$!
echo "$FRONT_PID" > "${LOG_DIR}/frontend.pid"
cd "${ROOT_DIR}"
echo -e "${SAFFRON}  Started Vite Frontend on Port 3000 (PID ${FRONT_PID}) -> logs/frontend.log${NC}"

wait_for_health "React Frontend (3000)" "http://localhost:3000/" 15 || true

# ------------------------------------------------------------------------------
# Diagnostics Verification
# ------------------------------------------------------------------------------
echo -e "${AMBER}[6/6] Running System Verification Diagnostics...${NC}"
bash "${ROOT_DIR}/check-health.sh" || true

echo ""
echo -e "${GREEN}======================================================================${NC}"
echo -e "${GREEN}>>> SDS ENTERPRISE AI GUARDIAN PLATFORM RUNNING SUCCESSFULLY! <<<<${NC}"
echo -e "${GREEN}======================================================================${NC}"
echo -e "  ● React Dashboard:       ${SAFFRON}http://localhost:3000${NC}"
echo -e "  ● AI Gateway Swagger:    ${SAFFRON}http://localhost:8080/swagger-ui.html${NC}"
echo -e "  ● Security Scanner:      ${SAFFRON}http://localhost:8081/swagger-ui.html${NC}"
echo -e "  ● Risk Engine Swagger:   ${SAFFRON}http://localhost:8082/swagger-ui.html${NC}"
echo -e "  ● Audit Service Swagger: ${SAFFRON}http://localhost:8083/swagger-ui.html${NC}"
echo -e "  ● Quality Checker:       ${SAFFRON}http://localhost:8084/swagger-ui.html${NC}"
echo -e "  ● Governance Suite:      ${SAFFRON}http://localhost:8085/swagger-ui.html${NC}"
echo -e "  ● In-Memory H2 DB:       ${SAFFRON}http://localhost:8083/h2-console${NC}"
echo -e "                           (JDBC URL: jdbc:h2:mem:auditdb, User: sa, Pass: [blank])"
echo ""
echo -e "  Runtime Architecture:"
echo -e "  - Storage: In-Memory H2 Database (No external Postgres needed)"
echo -e "  - Event Bus: Kafka REST Fallback Mode Active (No broker needed)"
echo -e "  - LLM Provider: Mock Pipeline Active (Zero cost / zero external API calls)"
echo -e "  - Logs: Individual service stdout/stderr piped to ./logs/*.log"
echo ""
echo -e "To check health anytime:   ${AMBER}./check-health.sh${NC}"
echo -e "To shut down cleanly:      ${AMBER}./stop-all.sh${NC}"
echo -e "${GREEN}======================================================================${NC}"
echo ""
