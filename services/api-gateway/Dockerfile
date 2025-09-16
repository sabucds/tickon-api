# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy parent pom and all modules (Maven needs all modules to exist)
COPY pom.xml ./pom.xml
COPY common ./common
COPY services ./services
COPY tests ./tests

# Build only this service and its dependencies
RUN mvn clean package -pl services/api-gateway -am -DskipTests

# Runtime stage
FROM openjdk:21-slim

VOLUME /tmp

# Copy the built jar from builder stage
COPY --from=builder /app/services/api-gateway/target/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
