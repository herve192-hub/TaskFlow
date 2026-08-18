# Dockerfile for Project Service

FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY . .

# Build only the project-service module and its dependencies
RUN sed -i 's/\r$//' mvnw && \
    chmod +x mvnw && \
    ./mvnw clean package \
    -pl services/work-management/project-service \
    -am \
    -DskipTests

# Runtime image 
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder \
    /app/services/work-management/project-service/target/*.jar \
    app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]