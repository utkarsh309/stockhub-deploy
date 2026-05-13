# ─── Stage 1: Build ───────────────────────────────────────────────────────
# Use Maven + Java 21 to compile and package the jar
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom first — Docker caches this layer
# so dependencies are only re-downloaded when pom changes
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Now copy source and build
COPY src ./src
RUN mvn package -DskipTests -q

# ─── Stage 2: Run ─────────────────────────────────────────────────────────
# Slim JRE image — no Maven, no source code, just the jar
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the jar from build stage
COPY --from=build /app/target/alert-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "app.jar"]