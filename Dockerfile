# syntax=docker/dockerfile:1


# Compilation du frontend angular

FROM node:22-alpine AS frontend-build

WORKDIR /app

# Les dépendances sont copiées en premier pour profiter du cache Docker.
COPY frontend/package.json frontend/package-lock.json ./

RUN npm ci

# Copie du code Angular.
COPY frontend/ ./

# Génération du frontend de production.
RUN npm run build


# Compilation du  backend Spring Boot
FROM maven:3.9-eclipse-temurin-17 AS backend-build

WORKDIR /app

# Le pom est copié séparément afin de mettre les dépendances Maven en cache.
COPY backend/pom.xml ./

RUN mvn -B -Pboot-plugin dependency:go-offline

# Copie du code Spring Boot.
COPY backend/src ./src

# Suppression de l'ancienne interface statique de secours.
RUN rm -rf ./src/main/resources/static/* \
    && mkdir -p ./src/main/resources/static

# Copie du frontend Angular compilé dans les ressources Spring Boot.
COPY --from=frontend-build \
    /app/dist/meal-planner-frontend/browser/ \
    ./src/main/resources/static/

# Compilation et exécution des tests du backend.
RUN mvn -B -Pboot-plugin clean package


FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

COPY --from=backend-build \
    /app/target/meal-planner-api-0.1.0-SNAPSHOT.jar \
    ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]