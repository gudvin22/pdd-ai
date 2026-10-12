# ========== Этап 1: Сборка ==========
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Копируем pom.xml и скачиваем зависимости (кэшируется)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходники и собираем
COPY src ./src
RUN cp src/main/resources/application.properties.example src/main/resources/application.properties && \
    mvn clean package -DskipTests

# ========== Этап 2: Запуск ==========
FROM eclipse-temurin:21-jre

WORKDIR /app

# Копируем готовый jar из этапа сборки
COPY --from=build /app/target/*.jar app.jar

# Открываем порт
EXPOSE 8080

# Запускаем
ENTRYPOINT ["java", "-jar", "app.jar"]