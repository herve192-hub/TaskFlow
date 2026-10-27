# Build and run auth-service using backend/ as the Docker build context.
# The JDK builder uses Maven's selected module plus required reactor dependencies;
# CI owns test execution, while image packaging skips tests. The final JRE contains
# only the executable service JAR and runs as UID/GID 10001, matching file-secret
# ownership in production. EXPOSE 8081 documents the internal port, not a host
# publication; Compose controls network access and runtime configuration.
# Credential/signing service; only its runtime receives the RSA private key.

# Dockerfile for Auth Service
# 
FROM eclipse-temurin:24.0.2_12-jdk-noble@sha256:dacac8e9a0df0d2bd24e702b4431132875c249930b70555ebd7ca285b5bee684 AS builder

WORKDIR /app

# Use backend/ as the context so Maven can resolve shared reactor modules.
# Packaging skips tests because Backend CI runs the full verification suites.
COPY . .

# Build the auth-service using Maven Wrapper (mvnw) ...
# 
RUN sed -i 's/\r$//' mvnw && \
    chmod +x mvnw && \
    ./mvnw clean package \
    -pl services/identity/auth-service \
    -am \
    -DskipTests


FROM eclipse-temurin:24.0.2_12-jre-noble@sha256:b416d02335e702b0403ff280de9475a3348e29382285969c9d4e17862ce632e7

RUN groupadd --gid 10001 taskflow && \
    useradd --uid 10001 --gid taskflow --no-create-home --home-dir /app --shell /usr/sbin/nologin taskflow && \
    mkdir -p /app && chown taskflow:taskflow /app

WORKDIR /app

# Copy the built JAR file from the builder stage to the final image ...
# 
COPY --from=builder --chown=taskflow:taskflow \
    /app/services/identity/auth-service/target/*.jar \
    app.jar

EXPOSE 8081

# Fixed runtime identity matches deployment file-secret ownership requirements.
USER 10001:10001

ENTRYPOINT ["java", "-jar", "app.jar"]
