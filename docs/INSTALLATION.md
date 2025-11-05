# Руководство по установке (Kotlin + Spring Boot)

Это руководство предоставляет всесторонние инструкции по установке Todoist Scheduler Bot на Kotlin + Spring Boot для разных сред.

## Системные требования

### Необходимое ПО

- **Java Version**: 17+ (рекомендуется JDK 17 или 21)
  - Ранние версии не совместимы с Spring Boot 3.2.0
  - Более поздние версии должны работать, но не тестировались

### Совместимость операционных систем

- ✅ **macOS**: Полностью поддерживается и протестировано
- ✅ **Linux**: Поддерживается (Ubuntu, Debian, CentOS и т.д.)
- ✅ **Windows**: Поддерживается (требуется Java для Windows)

### Сетевые требования

- **Интернет-соединение**: Требуется для доступа к API
- **Исходящий HTTPS**: Должен разрешать соединения с:
  - `api.telegram.org` (Telegram Bot API)
  - `api.todoist.com` (Todoist REST API)
- **Firewall**: Не требуются входящие порты (бот использует long polling)

### Системные пакеты

Не требуются дополнительные системные пакеты помимо Java 17+ и Gradle.

## Методы установки

Выберите метод установки, который лучше всего подходит для вашей среды:

- **Метод 1**: Использование Gradle Wrapper (рекомендуется для быстрой настройки)
- **Метод 2**: Ручная установка (рекомендуется для понимания каждого шага)

---

## Метод 1: Использование Gradle Wrapper

Gradle Wrapper автоматизирует процесс сборки и запуска.

### Что делает Wrapper

Gradle Wrapper автоматически:

1. Загружает подходящую версию Gradle
2. Настраивает среду сборки
3. Собирает проект с зависимостями
4. Позволяет запускать приложение

### Шаги установки

1. **Перейдите в директорию проекта**
   ```bash
   cd kotlin-spring-boot
   ```

2. **Настройте переменные окружения сначала** (обязательно перед запуском)
   ```bash
   cp ../env_example.txt .env
   # Отредактируйте .env с вашими токенами - см. Руководство по конфигурации
   ```

3. **Соберите проект**
   ```bash
   ./gradlew build
   ```

4. **Запустите бота**
   ```bash
   ./gradlew bootRun
   ```

### Что ожидать

Сборка выведет:

```
> Task :compileKotlin
> Task :processResources
> Task :classes
> Task :bootJar

BUILD SUCCESSFUL in 30s
```

Запуск покажет:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

2024-11-01 10:00:00 - INFO - Starting TodoistSchedulerBotApplication
2024-11-01 10:00:00 - INFO - Bot registered successfully
```

### Устранение проблем с Wrapper

**Проблема**: "Permission denied" ошибка
```bash
# Решение: Сделайте скрипты исполняемыми
chmod +x gradlew
```

**Проблема**: "java: command not found"
```bash
# Решение: Установите Java 17+ сначала
# На macOS:
brew install openjdk@17

# На Ubuntu/Debian:
sudo apt-get install openjdk-17-jdk

# На Windows:
# Скачайте с oracle.com и установите
```

---

## Метод 2: Ручная установка

Для большего контроля над процессом установки следуйте этим ручным шагам.

### Шаг 1: Проверьте версию Java

```bash
java --version
```

Ожидаемый вывод: `openjdk 17.x.x` или выше

Если у вас другая версия:

**macOS** (используя Homebrew):
```bash
brew install openjdk@17
# Добавьте в PATH:
echo 'export PATH="/usr/local/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
```

**Ubuntu/Debian**:
```bash
sudo apt-get update
sudo apt-get install openjdk-17-jdk
```

**Windows**:
- Скачайте JDK 17+ с [oracle.com](https://www.oracle.com/java/technologies/downloads/)
- Установите и проверьте `java --version`

### Шаг 2: Установите Gradle

**macOS**:
```bash
brew install gradle
```

**Ubuntu/Debian**:
```bash
sudo apt-get install gradle
```

**Windows**:
- Скачайте с [gradle.org](https://gradle.org/install/)
- Добавьте в PATH

### Шаг 3: Проверьте установку

```bash
gradle --version
```

Ожидаемый вывод: `Gradle 8.5` или выше

### Шаг 4: Клонируйте и настройте проект

```bash
cd kotlin-spring-boot

# Скопируйте и отредактируйте конфигурацию
cp ../env_example.txt .env
# Отредактируйте .env с вашими токенами
```

### Шаг 5: Соберите проект

```bash
gradle build
```

### Шаг 6: Запустите бота

```bash
gradle bootRun
```

Если успешно, увидите:
```
2024-11-01 10:00:00 - INFO - Bot registered successfully
2024-11-01 10:00:00 - INFO - Started TodoistSchedulerBotApplication
```

---

## Обзор зависимостей

Бот требует четыре основных зависимости:

| Зависимость | Версия | Назначение |
|-------------|--------|------------|
| **Spring Boot Starter** | 3.2.0 | Основной фреймворк и авто-конфигурация |
| **Spring WebFlux** | 3.2.0 | Реактивный HTTP клиент для Todoist API |
| **Telegram Bots** | 6.8.0 | Telegram Bot API фреймворк |
| **Jackson Kotlin** | 2.15+ | JSON обработка с поддержкой Kotlin |

### Почему эти конкретные версии?

- **Spring Boot 3.2.0**: Требуется для Kotlin 1.9.20 совместимости
  - Версия 2.x несовместима с Kotlin 1.9+
  - Версия 3.2.0 протестирована и стабильна

- **Telegram Bots 6.8.0**: Стабильная версия с полной поддержкой
  - Предоставляет async/await поддержку для неблокирующих вызовов API

- **Jackson**: Последняя стабильная версия для надежной JSON обработки
  - Используется как основа для Kotlin data классов

### Опциональные зависимости

Все зависимости включены в `build.gradle.kts` для полной функциональности.

---

## Шаги верификации

После установки проверьте корректность работы:

### 1. Проверьте версию Java

```bash
java --version
```

Ожидаемый: `openjdk 17.x.x` или выше

### 2. Проверьте зависимости

```bash
./gradlew dependencies --configuration runtimeClasspath | grep -E 'spring-boot|telegram'
```

Ожидаемый вывод:
```
+--- org.springframework.boot:spring-boot-starter:3.2.0
+--- org.telegram:telegrambots-spring-boot-starter:6.8.0
```

### 3. Протестируйте загрузку конфигурации

```bash
./gradlew bootRun --args='--spring.profiles.active=test'
```

Если конфигурация настроена:
```
INFO - Configuration loaded successfully
```

Если конфигурация отсутствует:
```
ERROR - Missing required environment variables: TELEGRAM_BOT_TOKEN, TODOIST_API_TOKEN
```

### 4. Протестируйте импорты модулей

```bash
./gradlew compileKotlin
```

Ожидаемый: `BUILD SUCCESSFUL`

### 5. Протестируйте парсер дат

```bash
./gradlew bootRun --args='--app.test.date-parser=true'
```

Ожидаемый: Успешный парсинг тестовых дат

---

## Пост-установочные шаги

### 1. Настройте переменные окружения

Вы должны настроить переменные окружения перед запуском бота. См. [Руководство по конфигурации](./CONFIGURATION.md) для:

- Получения Telegram Bot Token
- Получения Todoist API Token(s)
- Поиска вашего Telegram User ID
- Настройки мульти-пользовательской конфигурации

### 2. Протестируйте подключение бота

Перед полным развертыванием протестируйте бота:

```bash
# Запустите бота
./gradlew bootRun

# В другом терминале или на телефоне:
# Откройте Telegram и найдите вашего бота
# Отправьте: /start
```

Вы должны получить приветственное сообщение.

### 3. Создайте тестовое событие

Отправьте тестовое сообщение:
```
Стрижка завтра в 15:00
```

Проверьте ваш Todoist на созданное событие.

### 4. Просмотрите логи

Мониторьте вывод консоли на ошибки или предупреждения:

```bash
# Бот должен показать:
INFO - Received message from user ...
INFO - Parsing text: ...
INFO - Found time: ...
INFO - Task created successfully with ID: ...
```

---

## Распространенные проблемы установки

### Проблема: "java: command not found"

**Причина**: Java 17+ не установлена или не в PATH

**Решения**:

1. **Установите Java 17+**:
   ```bash
   # macOS с Homebrew
   brew install openjdk@17

   # Ubuntu/Debian
   sudo apt-get install openjdk-17-jdk
   ```

2. **Проверьте PATH**:
   ```bash
   which java
   ls -la $(which java)
   ```

3. **Используйте полный путь** если нужно:
   ```bash
   export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
   ```

### Проблема: "Could not find or load main class"

**Причина**: Неправильная конфигурация Gradle или отсутствующие зависимости

**Решение**:
```bash
# Очистите и пересоберите
./gradlew clean build

# Проверьте зависимости
./gradlew dependencies
```

### Проблема: "Failed to register bot"

**Причина**: Неправильный TELEGRAM_BOT_TOKEN или проблемы с сетью

**Решение**:
```bash
# Проверьте токен
echo $TELEGRAM_BOT_TOKEN

# Протестируйте подключение
curl https://api.telegram.org/bot$TELEGRAM_BOT_TOKEN/getMe
```

### Проблема: "Permission denied: './gradlew'"

**Причина**: Скрипт не имеет прав на исполнение

**Решение**:
```bash
chmod +x gradlew
./gradlew
```

### Проблема: Зависимости загружаются медленно

**Причина**: Разрешитель зависимостей Gradle или скорость сети

**Решения**:

1. **Используйте локальный кэш**:
   ```bash
   ./gradlew build --offline
   ```

2. **Увеличьте timeout**:
   ```bash
   ./gradlew build --timeout=300
   ```

---

## Следующие шаги

После успешной установки:

1. **Настройте бота** → [Руководство по конфигурации](./CONFIGURATION.md)
2. **Узнайте, как его использовать** → [Руководство пользователя](./USER_GUIDE.md)
3. **Разверните в продакшн** → [Руководство по развертыванию](./DEPLOYMENT.md)

---

## Удаление

Для полного удаления бота:

```bash
# Остановите приложение если запущено
# Ctrl+C в терминале с ботом

# Удалите собранные файлы
./gradlew clean

# Удалите конфигурацию (будьте осторожны!)
rm .env

# Удалите всю директорию проекта
cd ..
rm -rf kotlin-spring-boot/
```

---

## Дополнительные ресурсы

- **Официальная документация Kotlin**: https://kotlinlang.org/docs/
- **Spring Boot Documentation**: https://docs.spring.io/spring-boot/docs/current/reference/html/
- **Telegram Bots Documentation**: https://core.telegram.org/bots/api
- **Todoist API Documentation**: https://developer.todoist.com/rest/v2/

---

**Установка завершена!** Перейдите к [Руководству по конфигурации](./CONFIGURATION.md) для настройки ваших токенов и доступа пользователей.
