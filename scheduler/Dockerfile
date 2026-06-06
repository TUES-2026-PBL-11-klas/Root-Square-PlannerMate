# Stage 1: Build the Spring Boot app
# We use Maven + Java 21 to compile and package the app into a JAR
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml first - Docker caches this layer separately
# So if only source code changes, Maven dependencies aren't re-downloaded
COPY pom.xml ./pom.xml
RUN mvn dependency:go-offline -q

# Now copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -q

# Stage 2: Runtime image - much smaller than the build image
# We don't need Maven in production, just the JRE
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the built JAR from Stage 1
COPY --from=builder /app/target/scheduler-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8081 (what Spring Boot listens on)
EXPOSE 8081

# Add a non-root user for security - never run apps as root in containers
RUN addgroup -S planmate && adduser -S planmate -G planmate
USER planmate

# Start the app
ENTRYPOINT ["java", "-jar", "app.jar"]
