# Build Stage
FROM maven:3.9.8-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

COPY pom.xml .
COPY common/pom.xml common/
COPY risk-scoring-engine/pom.xml risk-scoring-engine/

RUN mvn dependency:go-offline -B -pl common,risk-scoring-engine -am

COPY common/src common/src
COPY risk-scoring-engine/src risk-scoring-engine/src
RUN mvn clean package -DskipTests -pl common,risk-scoring-engine -am

# Runtime Stage
FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="Enterprise SecOps Team <secops@enterprise.ai>"
LABEL description="Risk Scoring Engine - Multi-Factor Weighted Scoring & Heuristic Rule Gate"

WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /workspace/risk-scoring-engine/target/risk-scoring-engine-*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

ENV SERVER_PORT=8082 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8082

HEALTHCHECK --interval=15s --timeout=5s --start-period=25s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/api/v1/risk/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
