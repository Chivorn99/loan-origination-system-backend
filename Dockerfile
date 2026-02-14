# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the Spring Boot JAR file into the container
COPY target/loan_origination_system-0.0.1-SNAPSHOT.jar /app/loan-origination-system-backend.jar

# Expose the port the app will run on
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "/app/loan-origination-system-backend.jar"]