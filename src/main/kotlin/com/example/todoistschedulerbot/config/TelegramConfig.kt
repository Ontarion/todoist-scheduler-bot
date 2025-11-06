package com.example.todoistschedulerbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "telegram.bot")
data class TelegramConfig(
    var token: String = ""
)
