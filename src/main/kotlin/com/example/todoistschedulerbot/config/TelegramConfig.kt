package com.example.todoistschedulerbot.config

import com.example.todoistschedulerbot.service.UserManager
import org.springframework.stereotype.Component

@Component
data class TelegramConfig(
    private val userManager: UserManager
) {
    val token: String
        get() = userManager.getTelegramBotToken("default") ?: ""
}
