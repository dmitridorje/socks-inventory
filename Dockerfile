# Используем Java-образ как базу
FROM openjdk:17-jdk-slim

# Указываем арг, который можно будет передать при билде (необязательно)
ARG JAR_FILE=build/libs/SocksManagement.jar

# Копируем jar-файл в контейнер
COPY ${JAR_FILE} socks.jar

# Команда запуска приложения
ENTRYPOINT ["java", "-jar", "/socks.jar"]
