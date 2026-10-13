
# ============================================================
# BUILD STAGE
# ============================================================

FROM eclipse-temurin:21.0.12.1_1-jdk-noble@sha256:b468c3fc688b14450571494f588bd939378e7fd542ed5a73f8efc13f17872a87 AS builder

WORKDIR /app

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

WORKDIR /app

COPY --from=builder \
    /app/services/work-management/task-service/target/task-service-*.jar \
    app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
