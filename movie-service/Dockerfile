# ─── Étape 1 : Build Maven ─────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Télécharger les dépendances séparément pour profiter du cache Docker
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Étape 2 : Image finale légère ────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar movie-service.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "movie-service.jar"]
