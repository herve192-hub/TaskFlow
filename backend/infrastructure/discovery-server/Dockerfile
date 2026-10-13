#  discovery-server/Dockerfile ...
FROM eclipse-temurin:21.0.12.1_1-jdk-noble@sha256:b468c3fc688b14450571494f588bd939378e7fd542ed5a73f8efc13f17872a87 AS builder

# Set the working directory inside the container ...
WORKDIR /app

# Copy the entire backend directory into the container ...
COPY . .

# Make Maven wrapper executable and normalize line endings for Linux containers ...
RUN sed -i 's/\r$//' mvnw && \
    chmod +x mvnw && \
    ./mvnw clean package \
    -pl infrastructure/discovery-server \
    -am \
    -DskipTests

# Now, create a new image that will run the discovery server jar file. ...
# use the Eclipse Temurin JRE image as the base image for this new image. ...
FROM eclipse-temurin:21.0.12.1_1-jre-noble@sha256:000fd431958bc81a24abe1e8e5f0f0fd3ae365a594bd50aadb20696805f9408c

# Set the working directory inside the container to /app. 
# This is where the discovery server jar file will be copied to and run from. ...
WORKDIR /app

# Copy the discovery server jar file from the builder image to the new image. ...
COPY --from=builder \
    /app/infrastructure/discovery-server/target/*.jar \
    app.jar

# Expose port 8761 for the discovery server. 
# This allows other services to communicate with the discovery server through this port. ...
EXPOSE 8761

# Set the entry point for the container to run the discovery server jar file. ...
ENTRYPOINT ["java", "-jar", "app.jar"]
