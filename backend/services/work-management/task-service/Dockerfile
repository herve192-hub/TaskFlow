
# ============================================================
# BUILD STAGE
# ============================================================

FROM eclipse-temurin:21-jdk AS builder

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

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder \
    /app/services/work-management/task-service/target/task-service-*.jar \
    app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]