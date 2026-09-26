# ---------- Etapa 1: compilar el jar con Gradle ----------
FROM gradle:8.9-jdk17 AS build
WORKDIR /app
COPY . .
# --no-daemon evita que Gradle deje un proceso residente (no aplica en un build de un solo uso)
RUN gradle bootJar --no-daemon

# ---------- Etapa 2: imagen final, solo el jar + JRE ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Render/Koyeb inyectan la variable PORT; application.properties ya la lee.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
