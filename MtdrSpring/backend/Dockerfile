# Stage 1: Build the frontend
FROM node:18-slim AS frontend-builder
WORKDIR /app
# Copy package files first for better caching
COPY src/main/frontend/package*.json ./
# Install dependencies with clean npm cache
RUN npm cache clean --force && \
    npm install --legacy-peer-deps
# Copy the rest of the frontend files
COPY src/main/frontend ./
# Build the frontend
RUN npm run build

# Stage 2: Build the application using Maven
FROM maven:3.8.5-openjdk-11 AS backend-builder
WORKDIR /app
# Copy pom.xml first for better caching
COPY pom.xml .
# Copy source code
COPY src ./src
# Copy the built frontend assets
COPY --from=frontend-builder /app/build/ ./src/main/resources/static/
# Build the application
RUN mvn clean package -DskipTests

# Stage 3: Run the application
FROM openjdk:11-jre-slim
WORKDIR /app
# Copy the jar file from builder stage
COPY --from=backend-builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]