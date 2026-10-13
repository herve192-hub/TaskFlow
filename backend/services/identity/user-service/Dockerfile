# Dockerfile for User Service

FROM eclipse-temurin:24.0.2_12-jdk-noble@sha256:dacac8e9a0df0d2bd24e702b4431132875c249930b70555ebd7ca285b5bee684 AS builder

WORKDIR /app

COPY . .

# Build only the user-service module and its dependencies
RUN sed -i 's/\r$//' mvnw && \
    chmod +x mvnw && \
    ./mvnw clean package \
    -pl services/identity/user-service \
    -am \
    -DskipTests

# Runtime image
FROM eclipse-temurin:24.0.2_12-jre-noble@sha256:b416d02335e702b0403ff280de9475a3348e29382285969c9d4e17862ce632e7

RUN groupadd --gid 10001 taskflow && \
    useradd --uid 10001 --gid taskflow --no-create-home --home-dir /app --shell /usr/sbin/nologin taskflow && \
    mkdir -p /app && chown taskflow:taskflow /app

WORKDIR /app

COPY --from=builder --chown=taskflow:taskflow \
    /app/services/identity/user-service/target/*.jar \
    app.jar

EXPOSE 8082

USER 10001:10001

ENTRYPOINT ["java", "-jar", "app.jar"]
