package com.example.todoistschedulerbot.config

import com.example.todoistschedulerbot.bot.HaircutBot
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import jakarta.annotation.PostConstruct

@Configuration
class TelegramBotConfig {

    @Value("\${telegram.bot.token:}")
    private lateinit var telegramBotToken: String

    @PostConstruct
    fun validateConfiguration() {
        if (telegramBotToken.isBlank()) {
            throw IllegalStateException("TELEGRAM_BOT_TOKEN не задан. Установите переменную окружения TELEGRAM_BOT_TOKEN с токеном Telegram бота.")
        }
    }

    @Bean
    fun telegramBotsApi(haircutBot: HaircutBot): TelegramBotsApi {
        return try {
            val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
            botsApi.registerBot(haircutBot)
            botsApi
        } catch (e: TelegramApiException) {
            throw RuntimeException("Failed to register bot", e)
        }
    }
}
