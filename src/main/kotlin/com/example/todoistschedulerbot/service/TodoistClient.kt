package com.example.todoistschedulerbot.service

import com.example.todoistschedulerbot.config.TodoistConfig
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class TodoistClient(
    private val todoistConfig: TodoistConfig,
    private val webClientBuilder: WebClient.Builder
) {

    private val logger = LoggerFactory.getLogger(TodoistClient::class.java)

    private val webClient: WebClient by lazy {
        webClientBuilder
            .baseUrl(todoistConfig.baseUrl)
            .defaultHeader("Authorization", "Bearer ${todoistConfig.token}")
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build()
    }

    data class TodoistTaskRequest(
        val content: String,
        val description: String,
        val due_datetime: String,
        val duration: Int,
        val duration_unit: String = "minute",
        val priority: Int = 4
    )

    data class TodoistTaskResponse(
        val id: String,
        val content: String,
        val description: String?,
        val due_datetime: String?
    )

    fun createHaircutEvent(
        appointmentDateTime: LocalDateTime,
        eventTitle: String = "Стрижка",
        comment: String = "",
        addComment: Boolean = true
    ): Pair<Boolean, String> {
        if (todoistConfig.token.isBlank()) {
            return false to "API токен Todoist не настроен"
        }

        return try {
            // Вычисляем время окончания (+ 1.5 часа = 90 минут)
            val endDateTime = appointmentDateTime.plusMinutes(90)

            // Формируем описание задачи в зависимости от настройки addComment
            val description = if (addComment && comment.isNotBlank()) {
                comment
            } else {
                "добавлено через бот"
            }

            val taskRequest = TodoistTaskRequest(
                content = eventTitle,
                description = description,
                due_datetime = appointmentDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                duration = 90,
                duration_unit = "minute",
                priority = 4
            )

            logger.info("Создаем задачу в Todoist: $taskRequest")

            val response = webClient.post()
                .uri("/tasks")
                .bodyValue(taskRequest)
                .retrieve()
                .bodyToMono(TodoistTaskResponse::class.java)
                .onErrorResume { e ->
                    logger.error("Ошибка при создании задачи: ${e.message}")
                    Mono.empty()
                }
                .block()

            if (response != null) {
                logger.info("Задача успешно создана с ID: ${response.id}")
                true to response.id
            } else {
                false to "Ошибка создания задачи в Todoist"
            }

        } catch (e: WebClientException) {
            logger.error("Ошибка сетевого соединения: ${e.message}")
            false to "Ошибка соединения с Todoist"
        } catch (e: Exception) {
            logger.error("Неожиданная ошибка при создании задачи: ${e.message}")
            false to "Неожиданная ошибка при создании события"
        }
    }

    fun deleteTask(taskId: String): Pair<Boolean, String> {
        if (todoistConfig.token.isBlank()) {
            return false to "API токен Todoist не настроен"
        }

        if (taskId.isBlank()) {
            return false to "ID задачи не указан"
        }

        return try {
            logger.info("Удаляем задачу с ID: $taskId")

            val response = webClient.delete()
                .uri("/tasks/$taskId")
                .retrieve()
                .toBodilessEntity()
                .onErrorResume { e ->
                    logger.error("Ошибка при удалении задачи: ${e.message}")
                    Mono.empty()
                }
                .block()

            if (response?.statusCode?.is2xxSuccessful == true) {
                logger.info("Задача $taskId успешно удалена")
                true to "Задача успешно удалена"
            } else {
                val statusCode = response?.statusCode?.value() ?: 0
                logger.error("Ошибка при удалении задачи: статус $statusCode")
                false to "Ошибка при удалении: статус $statusCode"
            }

        } catch (e: WebClientException) {
            logger.error("Ошибка сетевого соединения при удалении: ${e.message}")
            false to "Ошибка соединения с Todoist"
        } catch (e: Exception) {
            logger.error("Неожиданная ошибка при удалении задачи: ${e.message}")
            false to "Неожиданная ошибка при удалении"
        }
    }

    fun testConnection(): Pair<Boolean, String> {
        if (todoistConfig.token.isBlank()) {
            return false to "API токен не настроен"
        }

        return try {
            val response = webClient.get()
                .uri("/projects")
                .retrieve()
                .toBodilessEntity()
                .onErrorResume { e ->
                    logger.error("Ошибка при тестировании соединения: ${e.message}")
                    Mono.empty()
                }
                .block()

            if (response?.statusCode?.is2xxSuccessful == true) {
                true to "Соединение с Todoist API успешно"
            } else {
                val statusCode = response?.statusCode?.value() ?: 0
                false to "Ошибка API: статус $statusCode"
            }

        } catch (e: Exception) {
            logger.error("Ошибка при тестировании соединения: ${e.message}")
            false to "Ошибка соединения"
        }
    }
}
