# Build Stage
FROM maven:3.9.8-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

COPY pom.xml .
COPY common/pom.xml common/
COPY prompt-security-scanner/pom.xml prompt-security-scanner/

RUN mvn dependency:go-offline -B -pl common,prompt-security-scanner -am

COPY common/src common/src
COPY prompt-security-scanner/src prompt-security-scanner/src
RUN mvn clean package -DskipTests -pl common,prompt-security-scanner -am

# Runtime Stage
FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="Enterprise SecOps Team <secops@enterprise.ai>"
LABEL description="Prompt Security Scanner - PII, Credential, Secret & Injection Detection Engine"

WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /workspace/prompt-security-scanner/target/prompt-security-scanner-*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

ENV SERVER_PORT=8081 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8081

HEALTHCHECK --interval=15s --timeout=5s --start-period=25s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/api/v1/scan/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
