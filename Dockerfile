# Dockerfile for Todoist Scheduler Bot (Kotlin + Spring Boot)
# Multi-architecture support for amd64 and arm64

# Use Eclipse Temurin JDK 17 as base image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Install necessary packages for building
RUN apk add --no-cache \
    bash \
    curl \
    && rm -rf /var/cache/apk/*

# Copy Gradle wrapper and build files first for better layer caching
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts ./

# Download Gradle dependencies (without source code for faster builds)
RUN ./gradlew dependencies --no-daemon --parallel

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew build --no-daemon --parallel -x test

# Create a smaller runtime image
FROM eclipse-temurin:17-jre-alpine

# Install necessary runtime packages
RUN apk add --no-cache \
    bash \
    curl \
    && rm -rf /var/cache/apk/*

# Set working directory
WORKDIR /app

# Copy the built application from the build stage
COPY --from=0 /app/build/libs/*.jar app.jar

# Set Java to run in container-optimized mode
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Set default environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV LOGGING_LEVEL_COM_EXAMPLE=INFO
ENV LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB=INFO
ENV APP_TIMEZONE=Europe/Moscow

# Expose port (though bot doesn't listen on HTTP port, Spring Boot requires it)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the bot
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
