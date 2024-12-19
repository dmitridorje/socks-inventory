#  REST API для учета носков на складе магазина

## Использованные технологии
- Spring Boot 2.7
- Java 17
- PostgreSQL
- Docker
- Liquibase
- MapStruct
- JaCoCo
- Gradle
- Swagger

## Инструкция по локальному запуску

### 1. Клонирование репозитория
Клонируйте репозиторий с помощью команды:
```bash
git clone https://github.com/dmitridorje/socks-inventory.git
```

### 2. Запуск Docker-контейнера с PostgreSQL

#### Способ 1: Запуск через скрипт
В корне проекта выполните следующий скрипт:
```bash
./run.sh
```

#### Способ 2: Ручной запуск
Если вы хотите настроить контейнер вручную:
```bash
docker-compose build
docker-compose up -d
```
PostgreSQL будет доступен на `localhost:5434`.

### 3. Сборка приложения
Выполните команду:
```bash
./gradlew build
```

### 4. Запуск приложения
Запустите собранный JAR-файл:
```bash
java -jar build/libs/SocksManagement.jar
```

### 5. Работа в приложени
При первом запуске в БД будет автоматически создана нужная таблица и в неё будут внесены несколько тестовых записей:

```bash
INSERT INTO sock (color, cotton_part, quantity)
VALUES ('BLACK', 10, 1),
       ('WHITE', 13, 3),
       ('RED', 16, 5),
       ('PINK', 19, 7),
       ('GREEN', 22, 9),
       ('PURPLE', 25, 11);
```

Приложение доступно:

- Через Swagger-UI: http://localhost:8080/swagger-ui/index.html
- Через Postman или любой другой аналогичный инструмент.

В целях тестирования написан ряд как модульных, так и интеграционных тестов (с использованием тест-контейнеров). Для проверки степени покрытия кода тестами используется плагин JaCoCo (выставлен порог 70%, после сборки формируется отчёт, см. файл build/jacocoHtml/index.html.
