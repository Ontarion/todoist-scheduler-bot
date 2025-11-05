package com.example.todoistschedulerbot.bot

import com.example.todoistschedulerbot.config.TelegramConfig
import com.example.todoistschedulerbot.service.DateParser
import com.example.todoistschedulerbot.service.TodoistClient
import com.example.todoistschedulerbot.service.UserManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class HaircutBot(
    private val telegramConfig: TelegramConfig,
    private val dateParser: DateParser,
    private val userManager: UserManager,
    private val todoistClient: TodoistClient
) : TelegramLongPollingBot() {

    private val logger = LoggerFactory.getLogger(HaircutBot::class.java)

    override fun getBotUsername(): String = "HaircutSchedulerBot"

    override fun getBotToken(): String = telegramConfig.token

    override fun onUpdateReceived(update: Update) {
        try {
            when {
                update.hasMessage() && update.message.hasText() -> handleTextMessage(update)
                update.hasCallbackQuery() -> handleCallbackQuery(update)
                else -> logger.debug("Получено неподдерживаемое обновление: ${update.updateId}")
            }
        } catch (e: Exception) {
            logger.error("Ошибка при обработке обновления: ${e.message}", e)
        }
    }

    private fun handleTextMessage(update: Update) {
        val message = update.message
        val userId = message.from.id.toString()
        val text = message.text

        logger.info("Получено сообщение от пользователя $userId: $text")

        when {
            text.startsWith("/start") -> handleStartCommand(update)
            text.startsWith("/help") -> handleHelpCommand(update)
            else -> handleAppointmentMessage(update)
        }
    }

    private fun handleStartCommand(update: Update) {
        val userId = update.message.from.id.toString()

        if (!userManager.isUserAllowed(userId)) {
            sendMessage(update.message.chatId, "❌ Извините, у вас нет доступа к этому боту.\n\nОбратитесь к администратору для получения доступа.")
            return
        }

        val welcomeMessage = """
            Привет! 👋

            Я бот для создания событий стрижки в Todoist.

            Просто отправь мне сообщение с датой и временем стрижки, например:
            • 'Стрижка 15 сентября в 14:00'
            • 'Парикмахерская завтра в 10:30'
            • 'Стригусь в пятницу в 16:00'

            Можно добавить комментарий с новой строки:
            • 'Стрижка 15 сентября в 14:00
              без бороды'

            Я автоматически создам событие на 1.5 часа в твоем Todoist!
        """.trimIndent()

        sendMessage(update.message.chatId, welcomeMessage)
    }

    private fun handleHelpCommand(update: Update) {
        val helpMessage = """
            📋 Как пользоваться ботом:

            1. Отправь сообщение с датой и временем стрижки
            2. Я найду дату в твоем сообщении
            3. Создам событие 'Стрижка' на 1.5 часа в Todoist

            Примеры сообщений:
            • 'Стрижка 20 августа в 15:00'
            • 'Завтра в 11:30 стригусь'
            • 'В понедельник в 14:00 к парикмахеру'

            Добавить комментарий можно с новой строки:
            • 'Стрижка 20 августа в 15:00
              без бороды'
            • 'Завтра в 11:30 стригусь
              как в прошлый раз'

            Команды:
            /start - начать работу с ботом
            /help - показать эту справку
        """.trimIndent()

        sendMessage(update.message.chatId, helpMessage)
    }

    private fun handleAppointmentMessage(update: Update) {
        val userId = update.message.from.id.toString()
        val text = update.message.text

        // Проверяем, разрешен ли пользователь
        if (!userManager.isUserAllowed(userId)) {
            sendMessage(update.message.chatId, "❌ Извините, у вас нет доступа к этому боту.\n\nОбратитесь к администратору для получения доступа.")
            return
        }

        // Получаем конфигурацию пользователя
        val userConfig = userManager.getUserConfig(userId)
        if (userConfig == null) {
            sendMessage(update.message.chatId, "❌ Конфигурация пользователя не найдена.\n\nОбратитесь к администратору для настройки.")
            return
        }

        // Отправляем сообщение о том, что обрабатываем запрос
        sendMessage(update.message.chatId, "⏳ Обрабатываем сообщение...")

        // Парсим дату из сообщения
        val parsedDateTime = dateParser.parseDate(text)

        if (parsedDateTime == null) {
            sendMessage(update.message.chatId, """
                ❌ Не удалось найти дату в вашем сообщении.

                Попробуйте написать более четко, например:
                • 'Стрижка 15 сентября в 14:00'
                • 'Парикмахерская завтра в 10:30'
            """.trimIndent())
            return
        }

        // Извлекаем комментарий
        val comment = extractComment(text, parsedDateTime)

        // Получаем настройки пользователя
        val todoistToken = userConfig.todoistToken
        val eventTitle = userConfig.eventTitle
        val addComment = userConfig.addComment

        if (todoistToken.isNullOrBlank()) {
            sendMessage(update.message.chatId, "❌ Todoist токен не настроен.\n\nОбратитесь к администратору.")
            return
        }

        // Создаем событие в Todoist
        val (success, result) = todoistClient.createHaircutEvent(
            parsedDateTime,
            eventTitle,
            comment,
            addComment
        )

        if (success) {
            val taskId = result
            // Отправляем уведомления всем пользователям
            notifyAllUsers(parsedDateTime, userId, eventTitle, taskId, comment)
        } else {
            sendMessage(update.message.chatId, "❌ Ошибка при создании события:\n$result\n\nПопробуйте еще раз через несколько минут.")
        }
    }

    private fun extractComment(messageText: String, parsedDate: LocalDateTime): String {
        // Используем перенос строки как разделитель комментария
        val parts = messageText.split("\n", limit = 2)
        return if (parts.size == 2) {
            parts[1].trim()
        } else {
            ""
        }
    }

    private fun notifyAllUsers(
        appointmentDateTime: LocalDateTime,
        creatorUserId: String,
        eventTitle: String,
        taskId: String,
        comment: String = ""
    ) {
        val configuredUsers = userManager.listConfiguredUsers()
        // Исключаем 'default' из списка пользователей
        val userIdsToNotify = configuredUsers.filter { it != "default" }

        val formattedDate = appointmentDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm"))

        for (userId in userIdsToNotify) {
            try {
                // Проверяем, что пользователь разрешен
                if (!userManager.isUserAllowed(userId)) {
                    continue
                }

                if (userId == creatorUserId) {
                    // Сообщение с кнопкой удаления для создателя
                    val message = buildString {
                        append("✅ Событие успешно создано!\n\n")
                        append("📅 $eventTitle: $formattedDate\n")
                        append("⏰ Длительность: 1.5 часа\n")
                        append("📋 Добавлено в Todoist")
                        if (comment.isNotBlank()) {
                            append("\n📝 Комментарий: $comment")
                        }
                    }

                    // Создаем кнопку удаления
                    val deleteButton = InlineKeyboardButton.builder()
                        .text("🗑 Удалить запись")
                        .callbackData("delete_$taskId")
                        .build()

                    val keyboard = InlineKeyboardMarkup.builder()
                        .keyboardRow(listOf(deleteButton))
                        .build()

                    sendMessage(userId.toLong(), message, keyboard)
                } else {
                    // Простое уведомление для остальных пользователей
                    val message = """
                        🔔 Новая запись!

                        📅 $eventTitle: $formattedDate
                        ⏰ Длительность: 1.5 часа
                        📋 Добавлено в Todoist
                    """.trimIndent()

                    sendMessage(userId.toLong(), message)
                }

                logger.info("Уведомление отправлено пользователю $userId")

            } catch (e: Exception) {
                logger.error("Ошибка при отправке уведомления пользователю $userId: ${e.message}")
            }
        }
    }

    private fun handleCallbackQuery(update: Update) {
        val callbackQuery = update.callbackQuery
        val userId = callbackQuery.from.id.toString()
        val callbackData = callbackQuery.data

        logger.info("Получен callback от пользователя $userId: $callbackData")

        // Проверяем права пользователя
        if (!userManager.isUserAllowed(userId)) {
            answerCallbackQuery(callbackQuery.id, "❌ Доступ запрещен")
            return
        }

        // Обработка кнопки удаления
        if (callbackData.startsWith("delete_")) {
            val taskId = callbackData.substring(7) // Убираем префикс 'delete_'

            // Получаем токен пользователя
            val userConfig = userManager.getUserConfig(userId)
            if (userConfig?.todoistToken.isNullOrBlank()) {
                answerCallbackQuery(callbackQuery.id, "❌ Конфигурация пользователя не найдена")
                return
            }

            // Подтверждаем получение callback
            answerCallbackQuery(callbackQuery.id, "⏳ Удаляем запись...")

            // Удаляем задачу
            val (success, message) = todoistClient.deleteTask(taskId)

            if (success) {
                // Обновляем сообщение
                val newText = """
                    🗑 Запись успешно удалена!

                    ❌ Событие удалено из Todoist
                """.trimIndent()

                editMessageText(callbackQuery.message.chatId, callbackQuery.message.messageId, newText)
            } else {
                // Показываем ошибку
                val errorText = """
                    ❌ Ошибка при удалении:
                    $message

                    Попробуйте еще раз или обратитесь к администратору.
                """.trimIndent()

                editMessageText(callbackQuery.message.chatId, callbackQuery.message.messageId, errorText)
            }
        } else {
            answerCallbackQuery(callbackQuery.id, "❓ Неизвестная команда")
        }
    }

    private fun sendMessage(chatId: Long, text: String, replyMarkup: InlineKeyboardMarkup? = null) {
        val message = SendMessage.builder()
            .chatId(chatId.toString())
            .text(text)
            .replyMarkup(replyMarkup)
            .build()

        try {
            execute(message)
        } catch (e: TelegramApiException) {
            logger.error("Ошибка при отправке сообщения: ${e.message}")
        }
    }

    private fun editMessageText(chatId: Long, messageId: Int, text: String) {
        val editMessage = EditMessageText.builder()
            .chatId(chatId.toString())
            .messageId(messageId)
            .text(text)
            .build()

        try {
            execute(editMessage)
        } catch (e: TelegramApiException) {
            logger.error("Ошибка при редактировании сообщения: ${e.message}")
        }
    }

    private fun answerCallbackQuery(callbackQueryId: String, text: String) {
        try {
            val answer = org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .text(text)
                .build()
            execute(answer)
        } catch (e: TelegramApiException) {
            logger.error("Ошибка при ответе на callback query: ${e.message}")
        }
    }
}
