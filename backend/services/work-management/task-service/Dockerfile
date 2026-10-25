# Build and run task-service using backend/ as the Docker build context.
# The JDK builder uses Maven's selected module plus required reactor dependencies;
# CI owns test execution, while image packaging skips tests. The final JRE contains
# only the executable service JAR and runs as UID/GID 10001, matching file-secret
# ownership in production. EXPOSE 8084 documents the internal port, not a host
# publication; Compose controls network access and runtime configuration.
# Task lifecycle/persistence and events; runtime also receives Kafka credentials.


# ============================================================
# BUILD STAGE
# ============================================================

FROM eclipse-temurin:21.0.12.1_1-jdk-noble@sha256:b468c3fc688b14450571494f588bd939378e7fd542ed5a73f8efc13f17872a87 AS builder

WORKDIR /app

# Use backend/ as the context so Maven can resolve shared reactor modules.
# Packaging skips tests because Backend CI runs the full verification suites.
COPY . .

# Normalize Windows line endings ...
RUN sed -i 's/\r$//' mvnw

# Make Maven wrapper executable ...
RUN chmod +x mvnw

# Build Task Service and required modules ...
RUN ./mvnw clean package \
    -pl services/work-management/task-service \
    -am \
    -DskipTests


# ============================================================
# RUNTIME STAGE
# ============================================================

FROM eclipse-temurin:21.0.12.1_1-jre-noble@sha256:000fd431958bc81a24abe1e8e5f0f0fd3ae365a594bd50aadb20696805f9408c

RUN groupadd --gid 10001 taskflow && \
    useradd --uid 10001 --gid taskflow --no-create-home --home-dir /app --shell /usr/sbin/nologin taskflow && \
    mkdir -p /app && chown taskflow:taskflow /app

WORKDIR /app

COPY --from=builder --chown=taskflow:taskflow \
    /app/services/work-management/task-service/target/task-service-*.jar \
    app.jar

EXPOSE 8084

# Fixed runtime identity matches deployment file-secret ownership requirements.
USER 10001:10001

ENTRYPOINT ["java", "-jar", "app.jar"]
