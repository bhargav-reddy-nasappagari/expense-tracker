# ===============================================
# Multi-stage build - small & efficient final image
# ===============================================

# ── Build stage ───────────────────────────────────────────────────────
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Cache dependencies first (very important for faster rebuilds)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the WAR
COPY src ./src
RUN mvn clean package -DskipTests

# ── Runtime stage - clean Tomcat 10.1 + Java 21 ───────────────────────
FROM tomcat:10.1.34-jdk21-temurin-jammy

# Remove default Tomcat applications (security + smaller image)
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy built WAR → deploys as ROOT application (/)
COPY --from=builder /build/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Optional: create place for external config if you want to mount later
RUN mkdir -p /usr/local/tomcat/config

# Tomcat runs on 8080
EXPOSE 8080

# Simple healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -f http://localhost:8080/ || exit 1

# Start Tomcat (foreground - required for Docker)
CMD ["catalina.sh", "run"]