FROM eclipse-temurin:21-jdk AS builder

# Set the working directory inside the container ...
WORKDIR /app

# Copy the entire backend directory into the container ...
COPY . .

# Make Maven wrapper executable and build the config-server module, skipping tests ...
RUN chmod +x mvnw && \
    ./mvnw clean package \
    -pl infrastructure/config-server \
    -am \
    -DskipTests

# Now, create a new image that will run the config-server jar file. ...
# use the Eclipse Temurin JRE image as the base image for this new image. ...
FROM eclipse-temurin:21-jre

# Set the working directory inside the container to /app. 
# This is where the config-server jar file will be copied to and run from. ...
WORKDIR /app

# Copy the config-server jar file from the builder image to the new image. ...
COPY --from=builder \
    /app/infrastructure/config-server/target/*.jar \
    app.jar

# Expose port 8888 for the config-server. ...
# This allows other services to communicate with the config-server through this port. ...
EXPOSE 8888

# Set the entry point for the container to run the config-server jar file using the java command. ...
ENTRYPOINT ["java", "-jar", "app.jar"]