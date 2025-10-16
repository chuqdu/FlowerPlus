# --- STAGE 1: BUILD ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# --- STAGE 2: RUN ---
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /workspace/target/*.war app.war
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.war"]
