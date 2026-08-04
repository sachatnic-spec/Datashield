# Multi-stage build for DataShield services
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom files
COPY pom.xml .
COPY backend/libraries/ backend/libraries/
COPY backend/java-services/ backend/java-services/

# Build all modules
RUN mvn clean package -DskipTests -q

# Stage 2: Runtime for Auth Service
FROM eclipse-temurin:21-jre-alpine AS auth-service

WORKDIR /app

COPY --from=builder /app/backend/java-services/auth-service/target/auth-service.jar .

EXPOSE 8001

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8001/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "auth-service.jar"]

# Stage 3: Runtime for Consent Service
FROM eclipse-temurin:21-jre-alpine AS consent-service

WORKDIR /app

COPY --from=builder /app/backend/java-services/consent-service/target/consent-service.jar .

EXPOSE 8002

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8002/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "consent-service.jar"]

# Stage 4: Runtime for Rights Service
FROM eclipse-temurin:21-jre-alpine AS rights-service

WORKDIR /app

COPY --from=builder /app/backend/java-services/rights-service/target/rights-service.jar .

EXPOSE 8003

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8003/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "rights-service.jar"]

# Stage 5: Runtime for Breach Service
FROM eclipse-temurin:21-jre-alpine AS breach-service

WORKDIR /app

COPY --from=builder /app/backend/java-services/breach-service/target/breach-service.jar .

EXPOSE 8004

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8004/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "breach-service.jar"]

# Stage 6: Runtime for Notification Service
FROM eclipse-temurin:21-jre-alpine AS notification-service

WORKDIR /app

COPY --from=builder /app/backend/java-services/notification-service/target/notification-service.jar .

EXPOSE 8005

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8005/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "notification-service.jar"]

# Stage 7: Runtime for Audit Service
FROM eclipse-temurin:21-jre-alpine AS audit-service

WORKDIR /app

COPY --from=builder /app/backend/java-services/audit-service/target/audit-service.jar .

EXPOSE 8006

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8006/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "audit-service.jar"]

# Stage 8: Runtime for Tenant Service
FROM eclipse-temurin:21-jre-alpine AS tenant-service

WORKDIR /app

COPY --from=builder /app/backend/java-services/tenant-service/target/tenant-service.jar .

EXPOSE 8007

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8007/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "tenant-service.jar"]

# Stage 9: Runtime for Connector Service
FROM eclipse-temurin:21-jre-alpine AS connector-service

WORKDIR /app

COPY --from=builder /app/backend/java-services/connector-service/target/connector-service.jar .

EXPOSE 8022

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8022/health || exit 1

ENTRYPOINT ["java", "-jar", "connector-service.jar"]

# Stage 10: Runtime for Webhook Service
FROM eclipse-temurin:21-jre-alpine AS webhook-service

WORKDIR /app

COPY --from=builder /app/backend/java-services/webhook-service/target/webhook-service.jar .

EXPOSE 8023

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8023/health || exit 1

ENTRYPOINT ["java", "-jar", "webhook-service.jar"]

# Stage 11: Runtime for SIEM Service
FROM eclipse-temurin:21-jre-alpine AS siem-service

WORKDIR /app

COPY --from=builder /app/backend/java-services/siem-service/target/siem-service.jar .

EXPOSE 8024

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8024/health || exit 1

ENTRYPOINT ["java", "-jar", "siem-service.jar"]

# Stage 12: Runtime for DPBI Service
FROM eclipse-temurin:21-jre-alpine AS dpbi-service

WORKDIR /app

COPY --from=builder /app/backend/java-services/dpbi-service/target/dpbi-service.jar .

EXPOSE 8025

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8025/health || exit 1

ENTRYPOINT ["java", "-jar", "dpbi-service.jar"]

