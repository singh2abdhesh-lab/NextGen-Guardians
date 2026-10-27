# Build Stage
FROM maven:3.9.8-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

COPY pom.xml .
COPY common/pom.xml common/
COPY audit-service/pom.xml audit-service/

RUN mvn dependency:go-offline -B -pl common,audit-service -am

COPY common/src common/src
COPY audit-service/src audit-service/src
RUN mvn clean package -DskipTests -pl common,audit-service -am

# Runtime Stage
FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="Enterprise SecOps Team <secops@enterprise.ai>"
LABEL description="Audit Service - Immutable Forensic Ledger, Kafka Ingestion, and Executive Analytics"

WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /workspace/audit-service/target/audit-service-*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

ENV SERVER_PORT=8083 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8083

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8083/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
