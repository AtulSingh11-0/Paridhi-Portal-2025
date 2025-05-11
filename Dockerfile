# Build stage
FROM eclipse-temurin:21.0.6_7-jdk-ubi9-minimal AS build
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
FROM eclipse-temurin:21.0.6_7-jre-ubi9-minimal
WORKDIR /app

# Create a non-root user to run the application
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
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=40.0 -XX:InitialRAMPercentage=20.0 -Xss256k -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/spring -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseStringDeduplication -XX:GCTimeRatio=4 -Xmx256m -Djava.security.egd=file:/dev/./urandom -Djava.io.tmpdir=/tmp/spring"

# Expose the application port
EXPOSE 8080

# Run the application with optimization flags
# ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Duser.timezone=Asia/Kolkata -jar app.jar"]