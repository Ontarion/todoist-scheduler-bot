package com.example.todoistschedulerbot.service

import com.example.todoistschedulerbot.config.TodoistConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime

class TodoistClientTest {

    private lateinit var todoistConfig: TodoistConfig
    private lateinit var webClientBuilder: WebClient.Builder
    private lateinit var todoistClient: TodoistClient

    @BeforeEach
    fun setUp() {
        todoistConfig = mock()
        webClientBuilder = mock()
        todoistClient = TodoistClient(todoistConfig, webClientBuilder)
    }

    @Test
    fun `createHaircutEvent should return error when token is blank`() {
        // Given
        `when`(todoistConfig.token).thenReturn("")

        // When
        val result = todoistClient.createHaircutEvent(LocalDateTime.now())

        // Then
        assertFalse(result.first)
        assertEquals("API токен Todoist не настроен", result.second)
    }

    @Test
    fun `deleteTask should return error when token is blank`() {
        // Given
        `when`(todoistConfig.token).thenReturn("")

        // When
        val result = todoistClient.deleteTask("task-123")

        // Then
        assertFalse(result.first)
        assertEquals("API токен Todoist не настроен", result.second)
    }

    @Test
    fun `deleteTask should return error when taskId is blank`() {
        // Given
        `when`(todoistConfig.token).thenReturn("test-token")

        // When
        val result = todoistClient.deleteTask("")

        // Then
        assertFalse(result.first)
        assertEquals("ID задачи не указан", result.second)
    }

    @Test
    fun `testConnection should return error when token is blank`() {
        // Given
        `when`(todoistConfig.token).thenReturn("")

        // When
        val result = todoistClient.testConnection()

        // Then
        assertFalse(result.first)
        assertEquals("API токен не настроен", result.second)
    }
}
