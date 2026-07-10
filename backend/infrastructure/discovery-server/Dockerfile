FROM eclipse-temurin:21-jdk AS builder

# Set the working directory inside the container ...
WORKDIR /app

# Copy the entire backend directory into the container ...
COPY . .

# Make Maven wrapper executable ...
RUN chmod +x mvnw && \
    ./mvnw clean package \
    -pl infrastructure/discovery-server \
    -am \
    -DskipTests

# Now, create a new image that will run the discovery server jar file. ...
# use the Eclipse Temurin JRE image as the base image for this new image. ...
FROM eclipse-temurin:21-jre

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

# Set the entry point for the container to run the discovery server jar file using the java command. ...
ENTRYPOINT ["java", "-jar", "app.jar"]