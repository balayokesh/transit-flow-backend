# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

# Install Maven
RUN apk add --no-cache maven

WORKDIR /app

# Copy POM first to cache dependency downloads separately from source changes
COPY pom.xml .

# Download all dependencies (re-runs only when pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy source code and build the fat JAR (skip tests — CI/CD should run them)
COPY src ./src
RUN mvn package -DskipTests -B

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Create a non-root user for security best practices
RUN addgroup -S spring && adduser -S spring -G spring

# Copy only the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

USER spring

# Railway automatically injects the PORT environment variable.
# We pass it through to Spring Boot at startup.
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar -Dserver.port=${PORT:-8080} app.jar"]
