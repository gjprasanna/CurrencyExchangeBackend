# Step 1: Build the app using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Run the app using a slim Java Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
# This copies the generated jar from the build stage and renames it to 'app.jar'
COPY --from=build /app/target/*.jar app.jar

# Render uses the PORT environment variable
EXPOSE 8080

# Start the application using the renamed file
ENTRYPOINT ["java", "-jar", "app.jar"]