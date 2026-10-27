# Build Stage
FROM maven:3.9.8-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

COPY pom.xml .
COPY common/pom.xml common/
COPY governance-service/pom.xml governance-service/

RUN mvn dependency:go-offline -B -pl common,governance-service -am

COPY common/src common/src
COPY governance-service/src governance-service/src
RUN mvn clean package -DskipTests -pl common,governance-service -am

# Runtime Stage
FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="Enterprise SecOps & AI Governance Team <secops@enterprise.ai>"
LABEL description="AI Governance Service - Policy Management, Model Registry, Department Quotas & Approval Workflows"

WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /workspace/governance-service/target/governance-service-*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

ENV SERVER_PORT=8085 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8085

HEALTHCHECK --interval=15s --timeout=5s --start-period=25s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8085/api/v1/governance/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
