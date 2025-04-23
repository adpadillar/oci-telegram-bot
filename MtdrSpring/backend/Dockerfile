# Stage 1: Build the frontend
FROM node:16 AS frontend-builder
WORKDIR /app
COPY src/main/frontend ./
RUN npm install --legacy-peer-deps
RUN npm run build

# Stage 2: Build the application using Maven
FROM maven:3.8.5-openjdk-11 AS backend-builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Copy the built frontend assets
COPY --from=frontend-builder /app/build/ ./src/main/resources/static/
RUN mvn clean package -DskipTests

# Stage 3: Run the application using a lightweight JRE image
FROM openjdk:11-jre
WORKDIR /app
# Copy the generated jar file from the builder stage
COPY --from=backend-builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]