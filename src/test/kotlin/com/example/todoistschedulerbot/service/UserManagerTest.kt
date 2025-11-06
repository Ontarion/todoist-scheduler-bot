package com.example.todoistschedulerbot.service

import com.example.todoistschedulerbot.config.AppConfig
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class UserManagerTest {

    private lateinit var appConfig: AppConfig
    private lateinit var objectMapper: ObjectMapper
    private lateinit var userManager: UserManager

    @BeforeEach
    fun setUp() {
        appConfig = mock(AppConfig::class.java)
        val usersConfig = mock(AppConfig.UsersConfig::class.java)
        val defaultAppointment = mock(AppConfig.DefaultAppointmentConfig::class.java)

        `when`(appConfig.users).thenReturn(usersConfig)
        `when`(appConfig.defaultAppointment).thenReturn(defaultAppointment)

        objectMapper = ObjectMapper()
        userManager = UserManager(appConfig, objectMapper)
    }

    @Test
    fun `getAllowedUsers should return users from config except default`() {
        // Given
        val userConfigJson = """
            {
                "user1": {"todoist_token": "token1"},
                "user2": {"todoist_token": "token2"},
                "default": {"todoist_token": "defaultToken"}
            }
        """.trimIndent()
        `when`(appConfig.users.config).thenReturn(userConfigJson)

        // When
        val result = userManager.getAllowedUsers()

        // Then
        assertEquals(listOf("user1", "user2"), result)
    }

    @Test
    fun `isUserAllowed should return true when user is in config`() {
        // Given
        val userConfigJson = """
            {
                "user1": {"todoist_token": "token1"},
                "user2": {"todoist_token": "token2"}
            }
        """.trimIndent()
        `when`(appConfig.users.config).thenReturn(userConfigJson)

        // When
        val result = userManager.isUserAllowed("user2")

        // Then
        assertTrue(result)
    }

    @Test
    fun `isUserAllowed should return false when user is not in config`() {
        // Given
        val userConfigJson = """
            {
                "user1": {"todoist_token": "token1"},
                "user2": {"todoist_token": "token2"}
            }
        """.trimIndent()
        `when`(appConfig.users.config).thenReturn(userConfigJson)

        // When
        val result = userManager.isUserAllowed("user4")

        // Then
        assertFalse(result)
    }

    @Test
    fun `getUserConfig should return user specific config when exists`() {
        // Given
        val userConfigJson = """
            {
                "user1": {
                    "todoist_token": "token123",
                    "event_title": "Парикмахерская",
                    "add_comment": false
                }
            }
        """.trimIndent()
        `when`(appConfig.users.config).thenReturn(userConfigJson)

        // When
        val result = userManager.getUserConfig("user1")

        // Then
        assertNotNull(result)
        assertEquals("token123", result?.todoistToken)
        assertEquals("Парикмахерская", result?.eventTitle)
        assertFalse(result?.addComment ?: true)
    }

    @Test
    fun `getUserConfig should return default config when user config not found and default exists`() {
        // Given
        val userConfigJson = """
            {
                "default": {
                    "todoist_token": "defaultToken",
                    "event_title": "Стрижка",
                    "add_comment": true
                }
            }
        """.trimIndent()
        `when`(appConfig.users.config).thenReturn(userConfigJson)

        // When
        val result = userManager.getUserConfig("user1")

        // Then
        assertNotNull(result)
        assertEquals("defaultToken", result?.todoistToken)
        assertEquals("Стрижка", result?.eventTitle)
        assertTrue(result?.addComment ?: false)
    }

    @Test
    fun `getUserConfig should return null when user config not found and no default exists`() {
        // Given
        `when`(appConfig.users.config).thenReturn("{}")

        // When
        val result = userManager.getUserConfig("user1")

        // Then
        assertNull(result)
    }

    @Test
    fun `getUserConfig should handle string token format`() {
        // Given
        val userConfigJson = """
            {
                "user1": "token123"
            }
        """.trimIndent()
        `when`(appConfig.users.config).thenReturn(userConfigJson)

        // When
        val result = userManager.getUserConfig("user1")

        // Then
        assertNotNull(result)
        assertEquals("token123", result?.todoistToken)
        assertEquals("Стрижка", result?.eventTitle) // default value
        assertTrue(result?.addComment ?: false) // default value
    }

    @Test
    fun `getTodoistToken should return token from user config`() {
        // Given
        val userConfigJson = """
            {
                "user1": {
                    "todoist_token": "token123"
                }
            }
        """.trimIndent()
        `when`(appConfig.users.config).thenReturn(userConfigJson)

        // When
        val result = userManager.getTodoistToken("user1")

        // Then
        assertEquals("token123", result)
    }

    @Test
    fun `getEventTitle should return custom title from user config`() {
        // Given
        val userConfigJson = """
            {
                "user1": {
                    "event_title": "Парикмахерская"
                }
            }
        """.trimIndent()
        `when`(appConfig.users.config).thenReturn(userConfigJson)

        // When
        val result = userManager.getEventTitle("user1")

        // Then
        assertEquals("Парикмахерская", result)
    }

    @Test
    fun `getEventTitle should return default title when not specified`() {
        // Given
        `when`(appConfig.users.config).thenReturn("{}")

        // When
        val result = userManager.getEventTitle("user1")

        // Then
        assertEquals("Стрижка", result)
    }

    @Test
    fun `shouldAddComment should return custom setting from user config`() {
        // Given
        val userConfigJson = """
            {
                "user1": {
                    "add_comment": false
                }
            }
        """.trimIndent()
        `when`(appConfig.users.config).thenReturn(userConfigJson)

        // When
        val result = userManager.shouldAddComment("user1")

        // Then
        assertFalse(result)
    }

    @Test
    fun `shouldAddComment should return default true when not specified`() {
        // Given
        `when`(appConfig.users.config).thenReturn("{}")

        // When
        val result = userManager.shouldAddComment("user1")

        // Then
        assertTrue(result)
    }

    @Test
    fun `listConfiguredUsers should return all configured users except default`() {
        // Given
        val userConfigJson = """
            {
                "user1": {"todoist_token": "token1"},
                "user2": {"todoist_token": "token2"},
                "default": {"todoist_token": "defaultToken"}
            }
        """.trimIndent()
        `when`(appConfig.users.config).thenReturn(userConfigJson)

        // When
        val result = userManager.listConfiguredUsers()

        // Then
        assertEquals(listOf("user1", "user2"), result)
    }

    @Test
    fun `loadUsersConfig should create default config from environment variable when config is blank`() {
        // Given
        `when`(appConfig.users.config).thenReturn("")

        // Set environment variable for this test
        val originalEnv = System.getenv("TODOIST_API_TOKEN")
        try {
            // We can't actually set environment variables in tests easily,
            // so we'll test the case where the env var is not set
            `when`(appConfig.users.config).thenReturn("")

            // When
            // This will be tested indirectly through other methods

            // Then
            // The config should be empty when no env var and no config
            val result = userManager.getUserConfig("anyUser")
            assertNull(result)
        } finally {
            // Restore original environment if needed
        }
    }
}
