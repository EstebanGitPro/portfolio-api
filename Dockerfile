# --- ETAPA 1: BUILD ---
FROM maven:3.9.15-eclipse-temurin-25 AS builder
WORKDIR /app

# Dependencies resolve in their own layer: editing a source file no longer
# re-downloads the whole repository on every rebuild.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# --- ETAPA 2: RUN TIME ---
FROM eclipse-temurin:25-jre
WORKDIR /app

# curl backs the container healthcheck; the JRE image ships without it.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Nothing here needs root.
RUN useradd --system --create-home --uid 1001 spring
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar
USER spring

EXPOSE 8086

# MaxRAMPercentage keeps the heap inside whatever the container limit is,
# instead of the JVM sizing itself against the host.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
