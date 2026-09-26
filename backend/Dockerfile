# ---------- Etapa 1: compilar el jar usando el propio Gradle Wrapper del proyecto ----------
# Usamos una imagen con solo el JDK (no Gradle preinstalado) y dejamos que ./gradlew
# descargue automáticamente la versión exacta de Gradle que pide gradle-wrapper.properties
# (9.3.0 en este proyecto). Así nunca se desincroniza con la versión que necesita el
# plugin de Spring Boot, aunque más adelante actualices Spring Boot o Gradle.
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon

# ---------- Etapa 2: imagen final, solo el jar + JRE ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Render/Koyeb inyectan la variable PORT; application.properties ya la lee.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
