# Dockerfile for Auth Service
# 
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY . .

# Build the auth-service using Maven Wrapper (mvnw) ...
# 
RUN sed -i 's/\r$//' mvnw && \
    chmod +x mvnw && \
    ./mvnw clean package \
    -pl services/identity/auth-service \
    -am \
    -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built JAR file from the builder stage to the final image ...
# 
COPY --from=builder \
    /app/services/identity/auth-service/target/*.jar \
    app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]