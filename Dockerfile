# ---------------------------------------------
# Build stage: compile Spring Boot app with Maven
# ---------------------------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy Maven descriptor first to leverage layer caching
COPY pom.xml .

# Download dependencies, skipping OS-specific setup scripts
RUN mvn -B -q -DskipTests -DskipUnixSetup=true -DskipWindowsSetup=true dependency:go-offline

# Copy source
COPY src ./src

# Build the application JAR (skip tests and env setup scripts)
RUN mvn -B -DskipTests -DskipUnixSetup=true -DskipWindowsSetup=true package

# ---------------------------------------------
# Runtime stage: run the Spring Boot app
# ---------------------------------------------
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/imovel-api-*.jar /app/app.jar

# Use port 8082 externally
EXPOSE 8082

# Entrypoint - activates both 'default' and 'docker' profiles by default
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-default,docker} -jar /app/app.jar"]