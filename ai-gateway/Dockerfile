# Build Stage
FROM maven:3.9.8-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

# Copy parent and module pom.xml files for dependency caching
COPY pom.xml .
COPY common/pom.xml common/
COPY prompt-security-scanner/pom.xml prompt-security-scanner/
COPY risk-scoring-engine/pom.xml risk-scoring-engine/
COPY response-quality-checker/pom.xml response-quality-checker/
COPY audit-service/pom.xml audit-service/
COPY ai-gateway/pom.xml ai-gateway/

# Download dependencies in offline mode
RUN mvn dependency:go-offline -B -pl common,ai-gateway -am

# Copy source code and build common and ai-gateway
COPY common/src common/src
COPY ai-gateway/src ai-gateway/src
RUN mvn clean package -DskipTests -pl common,ai-gateway -am

# Runtime Stage
FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="Enterprise SecOps Team <secops@enterprise.ai>"
LABEL description="AI Gateway - Unified Reactive Security Ingress & Orchestration Mesh"

WORKDIR /app

# Create unprivileged user for security compliance (OWASP non-root requirement)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled executable JAR
COPY --from=builder /workspace/ai-gateway/target/ai-gateway-*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

# Environment defaults
ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
