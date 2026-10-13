# Dockerfile for Auth Service
# 
FROM eclipse-temurin:21.0.12.1_1-jdk-noble@sha256:b468c3fc688b14450571494f588bd939378e7fd542ed5a73f8efc13f17872a87 AS builder

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


FROM eclipse-temurin:21.0.12.1_1-jre-noble@sha256:000fd431958bc81a24abe1e8e5f0f0fd3ae365a594bd50aadb20696805f9408c

WORKDIR /app

# Copy the built JAR file from the builder stage to the final image ...
# 
COPY --from=builder \
    /app/services/identity/auth-service/target/*.jar \
    app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
