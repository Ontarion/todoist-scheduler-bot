package com.example.todoistschedulerbot.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DateParserTest {

    private lateinit var dateParser: DateParser

    @BeforeEach
    fun setUp() {
        dateParser = DateParser()
    }

    @Test
    fun `parseDate should parse relative dates - today`() {
        val result = dateParser.parseDate("стрижка сегодня в 14:00")

        assertNotNull(result)
        assertEquals(LocalDate.now(), result?.toLocalDate())
        assertEquals(LocalTime.of(14, 0), result?.toLocalTime())
    }

    @Test
    fun `parseDate should parse relative dates - tomorrow`() {
        val result = dateParser.parseDate("стрижка завтра в 10:30")

        assertNotNull(result)
        assertEquals(LocalDate.now().plusDays(1), result?.toLocalDate())
        assertEquals(LocalTime.of(10, 30), result?.toLocalTime())
    }

    @Test
    fun `parseDate should parse relative dates - day after tomorrow`() {
        val result = dateParser.parseDate("стрижка послезавтра в 16:00")

        assertNotNull(result)
        assertEquals(LocalDate.now().plusDays(2), result?.toLocalDate())
        assertEquals(LocalTime.of(16, 0), result?.toLocalTime())
    }

    @Test
    fun `parseDate should parse weekdays - monday`() {
        val result = dateParser.parseDate("стрижка в понедельник в 15:00")

        assertNotNull(result)
        assertEquals(LocalTime.of(15, 0), result?.toLocalTime())
        // Дата должна быть следующим понедельником
        var expectedDate = LocalDate.now().with(java.time.DayOfWeek.MONDAY)
        if (expectedDate.isBefore(LocalDate.now()) || expectedDate == LocalDate.now()) {
            expectedDate = expectedDate.plusWeeks(1)
        }
        assertEquals(expectedDate, result?.toLocalDate())
    }

    @Test
    fun `parseDate should parse weekdays - friday with next modifier`() {
        val result = dateParser.parseDate("стрижка в следующую пятницу в 12:00")

        assertNotNull(result)
        assertEquals(LocalTime.of(12, 0), result?.toLocalTime())
        // Дата должна быть следующей пятницей
        val expectedDate = LocalDate.now().with(java.time.DayOfWeek.FRIDAY).plusWeeks(1)
        assertEquals(expectedDate, result?.toLocalDate())
    }

    @Test
    fun `parseDate should parse specific date with month name - september`() {
        val result = dateParser.parseDate("стрижка 15 сентября в 14:00")

        assertNotNull(result)
        var expectedDate = LocalDate.of(LocalDate.now().year, 9, 15)
        if (expectedDate.isBefore(LocalDate.now())) {
            expectedDate = expectedDate.withYear(LocalDate.now().year + 1)
        }
        assertEquals(expectedDate, result?.toLocalDate())
        assertEquals(LocalTime.of(14, 0), result?.toLocalTime())
    }

    @Test
    fun `parseDate should parse specific date with month name - january next year`() {
        val result = dateParser.parseDate("стрижка 10 января в 11:00")

        assertNotNull(result)
        val currentYear = LocalDate.now().year
        var expectedDate = LocalDate.of(currentYear, 1, 10)
        if (expectedDate.isBefore(LocalDate.now())) {
            expectedDate = expectedDate.withYear(currentYear + 1)
        }
        assertEquals(expectedDate, result?.toLocalDate())
        assertEquals(LocalTime.of(11, 0), result?.toLocalTime())
    }

    @Test
    fun `parseDate should parse date in dd mm format - current year`() {
        val result = dateParser.parseDate("стрижка 25.12 в 18:00")

        assertNotNull(result)
        val currentYear = LocalDate.now().year
        var expectedDate = LocalDate.of(currentYear, 12, 25)
        if (expectedDate.isBefore(LocalDate.now())) {
            expectedDate = expectedDate.withYear(currentYear + 1)
        }
        assertEquals(expectedDate, result?.toLocalDate())
        assertEquals(LocalTime.of(18, 0), result?.toLocalTime())
    }

    @Test
    fun `parseDate should parse date in dd mm format - specific year`() {
        val nextYear = LocalDate.now().year + 1
        val result = dateParser.parseDate("стрижка 15.06.$nextYear в 13:00")

        assertNotNull(result)
        assertEquals(LocalDate.of(nextYear, 6, 15), result?.toLocalDate())
        assertEquals(LocalTime.of(13, 0), result?.toLocalTime())
    }

    @Test
    fun `parseDate should parse time with dot separator - 14 30`() {
        val result = dateParser.parseDate("стрижка завтра в 14.30")

        assertNotNull(result)
        assertEquals(LocalDate.now().plusDays(1), result?.toLocalDate())
        assertEquals(LocalTime.of(14, 30), result?.toLocalTime())
    }

    @Test
    fun `parseDate should use default time when no time specified - 14 00`() {
        val result = dateParser.parseDate("стрижка завтра")

        assertNotNull(result)
        assertEquals(LocalDate.now().plusDays(1), result?.toLocalDate())
        assertEquals(LocalTime.of(14, 0), result?.toLocalTime())
    }

    @Test
    fun `parseDate should return null for invalid date`() {
        val result = dateParser.parseDate("стрижка в несуществующий день")

        assertNull(result)
    }

    @Test
    fun `parseDate should handle case insensitive parsing`() {
        val result = dateParser.parseDate("СТРИЖКА ЗАВТРА В 15:00")

        assertNotNull(result)
        assertEquals(LocalDate.now().plusDays(1), result?.toLocalDate())
        assertEquals(LocalTime.of(15, 0), result?.toLocalTime())
    }

    @Test
    fun `parseDate should parse abbreviated month names - sep`() {
        val result = dateParser.parseDate("стрижка 15 сен в 16:00")

        assertNotNull(result)
        var expectedDate = LocalDate.of(LocalDate.now().year, 9, 15)
        if (expectedDate.isBefore(LocalDate.now())) {
            expectedDate = expectedDate.withYear(LocalDate.now().year + 1)
        }
        assertEquals(expectedDate, result?.toLocalDate())
        assertEquals(LocalTime.of(16, 0), result?.toLocalTime())
    }
}
