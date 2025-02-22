# AvitoMerchShop
## Стэк технологий
- Язык разработки: Java 21
- СУБД: PostgreSQL
- Фреймворк: Spring
- Линтер: Spotless
- Тестирование: Junit и Testcontainers
- Создание образа: Docker

Исходный код сервиса доступен в каталоге `merchshop/src`.

# Запуск приложения

Для сборки и запуска сервиса необходимо использовать `docker compose` в корневой папке проекта:

```bash
$ docker compose up --build
```

В результате будут запущены два контейнера:
- `postgres-db` - сервис с БД PostgreSQL. Порты - `5432:5432`, если на машине присутствует другая база данных или сервис слушающий порт `5432` то необходимо изменить первый порт;
- `spring-boot-app` - сервис логики магазина мерча. Порты - `8080:8080`.

После поднятия сервис доступен по адресу `http://localhost:8080`. [API](https://github.com/avito-tech/tech-internship/blob/main/Tech%20Internships/Backend/Backend-trainee-assignment-winter-2025/schema.yaml) реализовано в соответствии с заданием.

# Линтер
В качестве Линтера использовался плагин Spotless предоставляющий автоматическое форматирование кода.  
Настройка Линтера в `pom.xml`  
```
<build>
  <plugins>
    <plugin>
      <groupId>com.diffplug.spotless</groupId>
      <artifactId>spotless-maven-plugin</artifactId>
      <version>2.42.0</version>
      <executions>
        <execution>
          <goals>
            <goal>apply</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <java>
          <googleJavaFormat/>
        </java>
      </configuration>
    </plugin>
  </plugins>
</build>
```
Для форматирования используются стандартные правила Google для языка Java.  
Чтобы применить форматирование необходимо выполнить команду `mvn spotless:apply`

# Тестирование

Проект содержит модульные и интеграционные-тесты.  
Исходный код тестов находится в директории `merchshop/src/test/java/com/avito/merchshop/service`.  
Тесты можно запустить с помощью команды `mvn test`.  
  
Также для обеспечения надежности и изоляции тестирования использовались testсontainers создающие временный Docker-образ в котором развертывается приложение и проводится его тестирование, что позволяет избежать действий с реальными данными на основной БД.

## Интеграционное тестирование

Описанные тест-кейсы:
- покупка мерча: приобретается 1 предмет, после чего проверяется корректное изменение баланса и запись предмета в инвентарь пользователя;
- передача монеток: от `user1` передаются монеты `user2`, после чего проверяется правильное изменение баланса обоих пользователей;

## Покрытие проекта модульными тестами

При написание модульного тестирования использовались классы-заглушки, помеченные аннотацией `@MockBean`.  
Для отображения процента покрытия теста IntellijIDEA предоставляет функционал запуска с покрытием, для полного описания результата тестирования нужно указать папку `src/test/java/com/avito/merchshop/service` как папку запуска.  
Единственный тестируемый класс - `UserServiceImpl`, т.к. в этом классе находится вся логика сервиса.  

Покрытие класса модульным тестами следующее:

| Element         | Class, %   | Method, %  | Line, %      | Branch, %   |
|:---------------:|:----------:|:----------:|:------------:|:-----------:|
| UserServiceImpl | 100% (1/1) | 100% (5/5) | 74% (85/114) | 66%, (24/36)|

# Прочие вопросы и решение

Возник вопрос что делать с отправлением монет самому себе.  
Было приянто решение запретить данную операции, т.к. данное действие является ненужным в рамках сервиса.  
По итогу при попытке сотрудника отправить монетки самому себе, выдаётся ошибка `400 Bad request` с соответствующим сообщением:
```json
{
  "errors": "cannot send coins to yourself"
}
```
