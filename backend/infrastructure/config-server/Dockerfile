# Build and run config-server using backend/ as the Docker build context.
# The JDK builder uses Maven's selected module plus required reactor dependencies;
# CI owns test execution, while image packaging skips tests. The final JRE contains
# only the executable service JAR and runs as UID/GID 10001, matching file-secret
# ownership in production. EXPOSE 8888 documents the internal port, not a host
# publication; Compose controls network access and runtime configuration.
# Native configuration server; Compose mounts backend/config-repo read-only.

FROM eclipse-temurin:24.0.2_12-jdk-noble@sha256:dacac8e9a0df0d2bd24e702b4431132875c249930b70555ebd7ca285b5bee684 AS builder

# Set the working directory inside the container ...
WORKDIR /app

# Copy the entire backend directory into the container ...
# Use backend/ as the context so Maven can resolve shared reactor modules.
# Packaging skips tests because Backend CI runs the full verification suites.
COPY . .

# Make Maven wrapper executable and normalize line endings for Linux containers ...
RUN sed -i 's/\r$//' mvnw && \
    chmod +x mvnw && \
    ./mvnw clean package \
    -pl infrastructure/config-server \
    -am \
    -DskipTests

# Now, create a new image that will run the config-server jar file. ...
# use the Eclipse Temurin JRE image as the base image for this new image. ...
FROM eclipse-temurin:24.0.2_12-jre-noble@sha256:b416d02335e702b0403ff280de9475a3348e29382285969c9d4e17862ce632e7

RUN groupadd --gid 10001 taskflow && \
    useradd --uid 10001 --gid taskflow --no-create-home --home-dir /app --shell /usr/sbin/nologin taskflow && \
    mkdir -p /app && chown taskflow:taskflow /app

# Set the working directory inside the container to /app. 
# This is where the config-server jar file will be copied to and run from. ...
WORKDIR /app

# Copy the config-server jar file from the builder image to the new image. ...
COPY --from=builder --chown=taskflow:taskflow \
    /app/infrastructure/config-server/target/*.jar \
    app.jar

# Expose port 8888 for the config-server. ...
# This allows other services to communicate with the config-server through this port. ...
EXPOSE 8888

# Set the entry point for the container to run the config-server jar file. ...
# Fixed runtime identity matches deployment file-secret ownership requirements.
USER 10001:10001

ENTRYPOINT ["java", "-jar", "app.jar"]
