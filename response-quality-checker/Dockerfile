# Build Stage
FROM maven:3.9.8-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

COPY pom.xml .
COPY common/pom.xml common/
COPY response-quality-checker/pom.xml response-quality-checker/

RUN mvn dependency:go-offline -B -pl common,response-quality-checker -am

COPY common/src common/src
COPY response-quality-checker/src response-quality-checker/src
RUN mvn clean package -DskipTests -pl common,response-quality-checker -am

# Runtime Stage
FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="Enterprise SecOps Team <secops@enterprise.ai>"
LABEL description="Response Quality Checker - Hallucination, PII Leakage, & Output Safety Heuristics"

WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /workspace/response-quality-checker/target/response-quality-checker-*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

ENV SERVER_PORT=8084 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8084

HEALTHCHECK --interval=15s --timeout=5s --start-period=25s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/api/v1/quality/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
