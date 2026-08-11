# Dockerfile for User Service

FROM eclipse-temurin:21-jdk AS builder

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
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder \
    /app/services/identity/user-service/target/*.jar \
    app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]