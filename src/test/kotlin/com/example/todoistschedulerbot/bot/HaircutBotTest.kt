package com.example.todoistschedulerbot.bot

import com.example.todoistschedulerbot.config.TelegramConfig
import com.example.todoistschedulerbot.service.DateParser
import com.example.todoistschedulerbot.service.TodoistClient
import com.example.todoistschedulerbot.service.UserManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class HaircutBotTest {

    private lateinit var telegramConfig: TelegramConfig
    private lateinit var dateParser: DateParser
    private lateinit var userManager: UserManager
    private lateinit var todoistClient: TodoistClient
    private lateinit var haircutBot: HaircutBot

    @BeforeEach
    fun setUp() {
        telegramConfig = mock()
        dateParser = mock()
        userManager = mock()
        todoistClient = mock()

        `when`(telegramConfig.token).thenReturn("test-token")

        haircutBot = HaircutBot(telegramConfig, dateParser, userManager, todoistClient)
    }

    @Test
    fun `getBotUsername should return correct username`() {
        // When
        val result = haircutBot.botUsername

        // Then
        assertEquals("HaircutSchedulerBot", result)
    }

    @Test
    fun `getBotToken should return token from config`() {
        // When
        val result = haircutBot.botToken

        // Then
        assertEquals("test-token", result)
    }
}
