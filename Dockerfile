# Build stage
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy only the necessary files for Maven build
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Make Maven wrapper executable and build without tests
RUN chmod +x mvnw && \
    ./mvnw package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create a non-root user to run the application
# Using Ubuntu (Jammy) user commands instead of Alpine (-S flag)
RUN groupadd -r spring && useradd -r -g spring spring

# Create necessary writable directories for Java temp files
RUN mkdir -p /tmp/spring && \
    mkdir -p /var/log/spring && \
    chown -R spring:spring /tmp/spring && \
    chown -R spring:spring /var/log/spring

USER spring:spring

# Copy only the built JAR from the build stage
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Environment variables for JVM optimization and temp directory configuration
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -Djava.io.tmpdir=/tmp/spring"

# Expose the application port
EXPOSE 8080

# Health check (uncommented for better container orchestration)
# HEALTHCHECK --interval=30s --timeout=3s CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Run the application with optimization flags
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]