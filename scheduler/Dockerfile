# Stage 1: Build the Spring Boot app
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy the build configuration and source code together
COPY pom.xml ./pom.xml
COPY src ./src

# Let Maven download and compile at the same time
RUN mvn clean package -DskipTests -q

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/scheduler-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

RUN addgroup -S planmate && adduser -S planmate -G planmate
USER planmate

ENTRYPOINT ["java", "-jar", "app.jar"]