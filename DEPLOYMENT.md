# Enterprise AI Guardian - Production Deployment Guide

This guide provides comprehensive instructions for deploying the **Enterprise AI Guardian** platform across local containerized environments, bare-metal virtual machines, and enterprise Kubernetes clusters (EKS, GKE, AKS, or on-premises OpenShift).

---

## 1. System Architecture & Topology

The platform comprises an asynchronous, event-driven microservices architecture partitioned into three operational tiers:

```
[ Clients / API Consumers ]
             │
             ▼  (Port 443 / 80)
   ┌───────────────────┐
   │ Reverse Proxy     │ (Nginx / Cloud Ingress / API Gateway)
   │ (SSL/TLS, WAF)    │
   └─────────┬─────────┘
             ├────────────────────────────────────────┐
             ▼                                        ▼
   ┌──────────────────────────┐             ┌──────────────────┐
   │ React Governance UI      │             │ AI Gateway       │
   │ (Port 3000 / Static SPA) │             │ (Port 8080)      │
   └──────────────────────────┘             └────────┬─────────┘
                                                     │
         ┌───────────────────┬───────────────────────┼──────────────────────┐
         ▼                   ▼                       ▼                      ▼
┌──────────────────┐ ┌──────────────────┐ ┌──────────────────────┐ ┌──────────────────┐
│ Security Scanner │ │ Risk Engine      │ │ Upstream LLM / Mock  │ │ Quality Checker  │
│ (Port 8081)      │ │ (Port 8082)      │ │ (OpenAI / Anthropic) │ │ (Port 8084)      │
└──────────────────┘ └──────────────────┘ └──────────────────────┘ └──────────────────┘
                                                     │
                                                     ▼ (Async Events)
                                          ┌──────────────────────┐
                                          │ Apache Kafka Broker  │ (Port 9092 / 29092)
                                          └──────────┬───────────┘
                                                     │
                                                     ▼ (Consumer Group)
                                          ┌──────────────────────┐
                                          │ Audit Service        │ (Port 8083)
                                          └──────────┬───────────┘
                                                     │
                                                     ▼ (JPA / Flyway)
                                          ┌──────────────────────┐
                                          │ PostgreSQL Database  │ (Port 5432)
                                          └──────────────────────┘
```

---

## 2. Minimum Hardware & Sizing Requirements

### 2.1 Per-Component Sizing

| Component | Min CPU | Min RAM | Storage | Scalability Model |
| :--- | :--- | :--- | :--- | :--- |
| **ai-gateway** | 1.0 vCPU | 1.5 GB | Ephemeral | Horizontal (Stateless, I/O bound) |
| **prompt-security-scanner** | 1.0 vCPU | 1.0 GB | Ephemeral | Horizontal (Stateless, CPU regex bound) |
| **risk-scoring-engine** | 0.5 vCPU | 1.0 GB | Ephemeral | Horizontal (Stateless, memory cache) |
| **response-quality-checker**| 1.0 vCPU | 1.0 GB | Ephemeral | Horizontal (Stateless) |
| **audit-service** | 1.0 vCPU | 1.5 GB | 50 GB+ | Horizontal (Kafka consumer partition scale) |
| **frontend (UI)** | 0.2 vCPU | 256 MB | Ephemeral | Static CDN or Nginx container |
| **PostgreSQL 16** | 2.0 vCPU | 4.0 GB | 100 GB+ SSD | Vertical + Read Replicas |
| **Apache Kafka + ZK** | 2.0 vCPU | 4.0 GB | 50 GB+ SSD | Horizontal (Cluster node scaling) |

### 2.2 Target Cluster Nodes
- **Minimum Production Cluster**: 3 Nodes (4 vCPU, 16 GB RAM each).
- **High-Throughput Production Cluster**: 5+ Nodes with dedicated Kafka and DB instances (e.g. AWS RDS Aurora Postgres & AWS MSK Kafka).

---

## 3. Environment Configuration & Secrets Management

All microservices read configuration through environment variables adhering to the Twelve-Factor App methodology.

### 3.1 Core Environment Variables

| Variable | Description | Recommended Production Value |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `POSTGRES_DB` | Database schema name | `ai_guardian_audit` |
| `POSTGRES_USER` | Database user account | `guardian_admin` (managed via IAM) |
| `POSTGRES_PASSWORD` | Database password | *High-entropy secret from Vault* |
| `DB_HOST` / `DB_PORT` | PostgreSQL host and port | Managed RDS / Cloud SQL endpoint |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers | `broker1:9092,broker2:9092,broker3:9092` |
| `JWT_SECRET` | HS256 / RS256 Secret key | 64+ char cryptographically random hex |
| `LLM_MOCK_MODE` | Enable simulated LLM | `false` |
| `OPENAI_API_KEY` | Upstream OpenAI provider key | Stored in Kubernetes Secret / KMS |
| `JAVA_OPTS` | JVM Memory & GC flags | `-Xms512m -Xmx1536m -XX:+UseG1GC` |

### 3.2 Secrets Injection Strategies
- **Kubernetes**: Inject secrets via `SealedSecrets`, `ExternalSecrets` Operator, or HashiCorp Vault Agent sidecar.
- **Docker Compose**: Mount secrets via `.env` file with restrictive permissions (`chmod 600 .env`) or Docker Secrets engine.
- **Cloud Native**: Utilize AWS Secrets Manager, Azure Key Vault, or GCP Secret Manager.

---

## 4. Docker Compose Deployment (Single Host / Staging)

For development, testing, and single-node staging environments, Docker Compose provides automated provisioning of all platform services.

### 4.1 Step-by-Step Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/enterprise/ai-guardian.git
   cd ai-guardian
   ```

2. **Configure Environment Variables**:
   ```bash
   cp .env.example .env
   # Edit .env with your environment credentials
   nano .env
   ```

3. **Launch the platform via orchestration script**:
   - On Linux / macOS:
     ```bash
     chmod +x start-all.sh stop-all.sh
     ./start-all.sh
     ```
   - On Windows:
     ```cmd
     start-all.bat
     ```

4. **Verify container health**:
   ```bash
   docker compose ps
   ```

5. **Access Application Endpoints**:
   - Web Console: `http://localhost:3000`
   - AI Gateway Ingress: `http://localhost:8080`
   - Swagger OpenAPI Docs: `http://localhost:8080/swagger-ui.html`

6. **Teardown**:
   ```bash
   ./stop-all.sh
   # Or to purge persistent volumes:
   docker compose down -v
   ```

---

## 5. Kubernetes (K8s) Production Deployment

For enterprise production deployments, use standard Kubernetes manifests or Helm charts.

### 5.1 Namespace & Secret Provisioning

```yaml
# 00-namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: ai-guardian
---
# 01-secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: ai-guardian-secrets
  namespace: ai-guardian
type: Opaque
stringData:
  POSTGRES_USER: "guardian_app"
  POSTGRES_PASSWORD: "SuperSecureProductionPassword123!"
  JWT_SECRET: "9f8a3c2b1e4d5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a"
  OPENAI_API_KEY: "sk-proj-prod-actual-key-here"
```

### 5.2 AI Gateway Deployment & Horizontal Pod Autoscaler

```yaml
# 02-ai-gateway.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ai-gateway
  namespace: ai-guardian
  labels:
    app: ai-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: ai-gateway
  template:
    metadata:
      labels:
        app: ai-gateway
    spec:
      containers:
      - name: ai-gateway
        image: enterprise/ai-guardian-gateway:latest
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: ai-guardian-config
        - secretRef:
            name: ai-guardian-secrets
        resources:
          requests:
            cpu: "500m"
            memory: "1Gi"
          limits:
            cpu: "2000m"
            memory: "2Gi"
        livenessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 15
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: ai-gateway
  namespace: ai-guardian
spec:
  type: ClusterIP
  ports:
  - port: 8080
    targetPort: 8080
  selector:
    app: ai-gateway
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: ai-gateway-hpa
  namespace: ai-guardian
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: ai-gateway
  minReplicas: 3
  maxReplicas: 15
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

### 5.3 Audit Service Deployment

```yaml
# 03-audit-service.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: audit-service
  namespace: ai-guardian
spec:
  replicas: 2
  selector:
    matchLabels:
      app: audit-service
  template:
    metadata:
      labels:
        app: audit-service
    spec:
      containers:
      - name: audit-service
        image: enterprise/ai-guardian-audit-service:latest
        ports:
        - containerPort: 8083
        envFrom:
        - configMapRef:
            name: ai-guardian-config
        - secretRef:
            name: ai-guardian-secrets
        resources:
          requests:
            cpu: "500m"
            memory: "1Gi"
          limits:
            cpu: "1500m"
            memory: "2Gi"
        livenessProbe:
          httpGet:
            path: /health
            port: 8083
          initialDelaySeconds: 30
          periodSeconds: 10
```

---

## 6. Database Migrations & High Availability

### 6.1 Schema Initialisation
The database schema (`schema.sql`) runs automatically upon container initialization in Docker Compose or can be managed via Flyway/Liquibase pipelines.

### 6.2 Backup & Disaster Recovery Strategy
1. **Automated Snapshots**: Daily automated physical snapshots with point-in-time recovery (PITR) up to 30 days.
2. **Logical Dump Schedule**:
   ```bash
   pg_dump -h $DB_HOST -U $POSTGRES_USER -Fc -d ai_guardian_audit > audit_backup_$(date +%Y%m%d_%H%M%S).dump
   ```
3. **Partition Pruning**: The `audit_events` table contains time-indexed partitioning on `timestamp`. Archive partitions older than 365 days to cold cloud object storage (S3 Glacier / Google Cloud Archive).

---

## 7. Security Hardening & Compliance Checklist

- [x] **Non-Root Execution**: All Docker containers drop privileges and execute as `appuser:appgroup` (UID/GID 10001).
- [x] **OWASP Secure Headers**: Nginx and Spring Security enforce `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Strict-Transport-Security`, and restrictive `Content-Security-Policy`.
- [x] **Mutual TLS (mTLS)**: Recommended inside Kubernetes via Istio or Linkerd service mesh for inter-service communication.
- [x] **Kafka ACLs & SASL_SSL**: Enable SASL/SCRAM or mTLS authentication on Kafka brokers in production.
- [x] **Rate Limiting**: AI Gateway enforces IP- and Token-bucket rate limiting (100 req/sec standard, burstable to 200).
- [x] **Vulnerability Scanning**: Automated image vulnerability scanning using Trivy or Snyk in CI/CD before registry push.

---

## 8. Observability & Monitoring

### 8.1 Health Probes
Every microservice implements a lightweight `/health` probe:
- Gateway: `http://localhost:8080/health`
- Scanner: `http://localhost:8081/api/v1/scan/health`
- Risk Engine: `http://localhost:8082/api/v1/risk/health`
- Audit Service: `http://localhost:8083/health`
- Quality Checker: `http://localhost:8084/api/v1/quality/health`

### 8.2 Metrics & Tracing
- **Micrometer & Prometheus**: Exposes operational metrics at `/actuator/prometheus` (HTTP request rates, latency p95/p99, JVM heap, garbage collection).
- **Correlation ID Tracking**: Every inbound request is stamped with an `X-Correlation-Id` or `traceId` which propagates across Kafka messages and down into the PostgreSQL forensic event records.
