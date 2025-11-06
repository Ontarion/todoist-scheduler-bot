package com.example.todoistschedulerbot.service

import com.example.todoistschedulerbot.config.AppConfig
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserManager(
    private val appConfig: AppConfig,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(UserManager::class.java)

    private val usersConfig: Map<String, UserConfig> by lazy {
        loadUsersConfig()
    }

    data class UserConfig(
        val todoistToken: String? = null,
        val timezone: String? = null,
        val eventTitle: String = "Стрижка",
        val addComment: Boolean = true
    )

    private fun loadUsersConfig(): Map<String, UserConfig> {
        return try {
            val usersJson = appConfig.users.config
            if (usersJson.isBlank()) {
                // Попытка создать базовую конфигурацию из основного токена Todoist
                val mainToken = System.getenv("TODOIST_API_TOKEN")
                if (!mainToken.isNullOrBlank()) {
                    logger.info("Создана базовая конфигурация пользователей из TODOIST_API_TOKEN")
                    mapOf("default" to UserConfig(todoistToken = mainToken))
                } else {
                    emptyMap()
                }
            } else {
                val rawConfig: Map<String, Any> = objectMapper.readValue(usersJson)
                rawConfig.mapValues { (_, value) ->
                    when (value) {
                        is Map<*, *> -> {
                            @Suppress("UNCHECKED_CAST")
                            val configMap = value as Map<String, Any>
                            UserConfig(
                                todoistToken = configMap["todoist_token"] as? String,
                                timezone = configMap["timezone"] as? String,
                                eventTitle = configMap["event_title"] as? String ?: "Стрижка",
                                addComment = configMap["add_comment"] as? Boolean ?: true
                            )
                        }
                        is String -> {
                            // Если значение - строка, предполагаем что это токен
                            UserConfig(todoistToken = value)
                        }
                        else -> UserConfig()
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Ошибка загрузки конфигурации пользователей: ${e.message}")
            emptyMap()
        }.also { config ->
            logger.info("Загружена конфигурация для ${config.size} пользователей")
        }
    }

    fun getAllowedUsers(): List<String> {
        return usersConfig.keys.filter { it != "default" }.toList()
    }

    fun isUserAllowed(userId: String): Boolean {
        val allowedUsers = getAllowedUsers()

        // Если список пуст, разрешаем всех (обратная совместимость)
        if (allowedUsers.isEmpty()) {
            return true
        }

        return userId in allowedUsers
    }

    fun getUserConfig(userId: String): UserConfig? {
        // Ищем конфигурацию по user_id
        val userConfig = usersConfig[userId]

        if (userConfig != null) {
            return userConfig
        }

        // Если конфигурации для пользователя нет, используем default
        val defaultConfig = usersConfig["default"]
        if (defaultConfig != null) {
            logger.info("Используется default конфигурация для пользователя $userId")
            return defaultConfig
        }

        // Если и default нет, возвращаем null
        logger.warn("Конфигурация не найдена для пользователя $userId")
        return null
    }

    fun getTodoistToken(userId: String): String? {
        return getUserConfig(userId)?.todoistToken
    }

    fun getTimezone(userId: String): String? {
        return getUserConfig(userId)?.timezone
    }

    fun getEventTitle(userId: String): String {
        return getUserConfig(userId)?.eventTitle ?: "Стрижка"
    }

    fun shouldAddComment(userId: String): Boolean {
        return getUserConfig(userId)?.addComment ?: true
    }

    fun listConfiguredUsers(): List<String> {
        return usersConfig.keys.filter { it != "default" }.toList()
    }
}
