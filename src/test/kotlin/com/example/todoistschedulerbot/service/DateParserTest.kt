package com.example.todoistschedulerbot.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.DayOfWeek

class DateParserTest {

    private lateinit var dateParser: DateParser

    @BeforeEach
    fun setUp() {
        dateParser = DateParser()
    }

    @Nested
    @DisplayName("Относительные даты")
    inner class RelativeDatesTest {

        @Test
        fun `сегодня с временем`() {
            val result = dateParser.parseDate("сегодня в 14:00")

            assertNotNull(result)
            assertEquals(LocalDate.now(), result?.toLocalDate())
            assertEquals(LocalTime.of(14, 0), result?.toLocalTime())
        }

        @Test
        fun `сегодня с временем 16-00`() {
            val result = dateParser.parseDate("сегодня в 16:00")

            assertNotNull(result)
            assertEquals(LocalDate.now(), result?.toLocalDate())
            assertEquals(LocalTime.of(16, 0), result?.toLocalTime())
        }

        @Test
        fun `завтра с временем`() {
            val result = dateParser.parseDate("завтра в 10:30")

            assertNotNull(result)
            assertEquals(LocalDate.now().plusDays(1), result?.toLocalDate())
            assertEquals(LocalTime.of(10, 30), result?.toLocalTime())
        }

        @Test
        fun `завтра с временем 16-00`() {
            val result = dateParser.parseDate("завтра в 16:00")

            assertNotNull(result)
            assertEquals(LocalDate.now().plusDays(1), result?.toLocalDate())
            assertEquals(LocalTime.of(16, 0), result?.toLocalTime())
        }

        @Test
        fun `послезавтра с временем`() {
            val result = dateParser.parseDate("послезавтра в 16:00")

            assertNotNull(result)
            assertEquals(LocalDate.now().plusDays(2), result?.toLocalDate())
            assertEquals(LocalTime.of(16, 0), result?.toLocalTime())
        }

        @Test
        fun `послезавтра с временем 15-30`() {
            val result = dateParser.parseDate("послезавтра в 15:30")

            assertNotNull(result)
            assertEquals(LocalDate.now().plusDays(2), result?.toLocalDate())
            assertEquals(LocalTime.of(15, 30), result?.toLocalTime())
        }

        @Test
        fun `стрижка сегодня в 14-00`() {
            val result = dateParser.parseDate("стрижка сегодня в 14:00")

            assertNotNull(result)
            assertEquals(LocalDate.now(), result?.toLocalDate())
            assertEquals(LocalTime.of(14, 0), result?.toLocalTime())
        }

        @Test
        fun `стрижка завтра в 10-30`() {
            val result = dateParser.parseDate("стрижка завтра в 10:30")

            assertNotNull(result)
            assertEquals(LocalDate.now().plusDays(1), result?.toLocalDate())
            assertEquals(LocalTime.of(10, 30), result?.toLocalTime())
        }

        @Test
        fun `стрижка послезавтра в 16-00`() {
            val result = dateParser.parseDate("стрижка послезавтра в 16:00")

            assertNotNull(result)
            assertEquals(LocalDate.now().plusDays(2), result?.toLocalDate())
            assertEquals(LocalTime.of(16, 0), result?.toLocalTime())
        }
    }

    @Nested
    @DisplayName("Дни недели")
    inner class WeekdaysTest {

        @Test
        fun `в понедельник`() {
            val result = dateParser.parseDate("в понедельник в 15:00")

            assertNotNull(result)
            assertEquals(LocalTime.of(15, 0), result?.toLocalTime())
            var expectedDate = LocalDate.now()
            while (expectedDate.dayOfWeek != DayOfWeek.MONDAY || !expectedDate.isAfter(LocalDate.now())) {
                expectedDate = expectedDate.plusDays(1)
            }
            assertEquals(expectedDate, result?.toLocalDate())
        }

        @Test
        fun `во вторник`() {
            val result = dateParser.parseDate("во вторник в 12:00")

            assertNotNull(result)
            assertEquals(LocalTime.of(12, 0), result?.toLocalTime())
            var expectedDate = LocalDate.now()
            while (expectedDate.dayOfWeek != DayOfWeek.TUESDAY || !expectedDate.isAfter(LocalDate.now())) {
                expectedDate = expectedDate.plusDays(1)
            }
            assertEquals(expectedDate, result?.toLocalDate())
        }

        @Test
        fun `в среду`() {
            val result = dateParser.parseDate("в среду в 16:00")

            assertNotNull(result)
            assertEquals(LocalTime.of(16, 0), result?.toLocalTime())
            var expectedDate = LocalDate.now()
            while (expectedDate.dayOfWeek != DayOfWeek.WEDNESDAY || !expectedDate.isAfter(LocalDate.now())) {
                expectedDate = expectedDate.plusDays(1)
            }
            assertEquals(expectedDate, result?.toLocalDate())
        }

        @Test
        fun `в четверг`() {
            val result = dateParser.parseDate("в четверг в 14:30")

            assertNotNull(result)
            assertEquals(LocalTime.of(14, 30), result?.toLocalTime())
            var expectedDate = LocalDate.now()
            while (expectedDate.dayOfWeek != DayOfWeek.THURSDAY || !expectedDate.isAfter(LocalDate.now())) {
                expectedDate = expectedDate.plusDays(1)
            }
            assertEquals(expectedDate, result?.toLocalDate())
        }

        @Test
        fun `в пятницу`() {
            val result = dateParser.parseDate("в пятницу в 18:00")

            assertNotNull(result)
            assertEquals(LocalTime.of(18, 0), result?.toLocalTime())
            var expectedDate = LocalDate.now()
            while (expectedDate.dayOfWeek != DayOfWeek.FRIDAY || !expectedDate.isAfter(LocalDate.now())) {
                expectedDate = expectedDate.plusDays(1)
            }
            assertEquals(expectedDate, result?.toLocalDate())
        }

        @Test
        fun `в субботу`() {
            val result = dateParser.parseDate("в субботу в 11:00")

            assertNotNull(result)
            assertEquals(LocalTime.of(11, 0), result?.toLocalTime())
            var expectedDate = LocalDate.now()
            while (expectedDate.dayOfWeek != DayOfWeek.SATURDAY || !expectedDate.isAfter(LocalDate.now())) {
                expectedDate = expectedDate.plusDays(1)
            }
            assertEquals(expectedDate, result?.toLocalDate())
        }

        @Test
        fun `в воскресенье`() {
            val result = dateParser.parseDate("в воскресенье в 10:00")

            assertNotNull(result)
            assertEquals(LocalTime.of(10, 0), result?.toLocalTime())
            var expectedDate = LocalDate.now()
            while (expectedDate.dayOfWeek != DayOfWeek.SUNDAY || !expectedDate.isAfter(LocalDate.now())) {
                expectedDate = expectedDate.plusDays(1)
            }
            assertEquals(expectedDate, result?.toLocalDate())
        }

        @Test
        fun `следующий понедельник`() {
            val result = dateParser.parseDate("следующий понедельник в 15:00")

            assertNotNull(result)
            assertEquals(LocalTime.of(15, 0), result?.toLocalTime())
            var expectedDate = LocalDate.now()
            var foundFirst = false
            while (true) {
                expectedDate = expectedDate.plusDays(1)
                if (expectedDate.dayOfWeek == DayOfWeek.MONDAY) {
                    if (foundFirst) break
                    foundFirst = true
                }
            }
            assertEquals(expectedDate, result?.toLocalDate())
        }

        @Test
        fun `следующую пятницу`() {
            val result = dateParser.parseDate("следующую пятницу в 12:00")

            assertNotNull(result)
            assertEquals(LocalTime.of(12, 0), result?.toLocalTime())
            var expectedDate = LocalDate.now()
            var foundFirst = false
            while (true) {
                expectedDate = expectedDate.plusDays(1)
                if (expectedDate.dayOfWeek == DayOfWeek.FRIDAY) {
                    if (foundFirst) break
                    foundFirst = true
                }
            }
            assertEquals(expectedDate, result?.toLocalDate())
        }
    }

    @Nested
    @DisplayName("Конкретные даты")
    inner class SpecificDatesTest {

        @Test
        fun `15 сентября`() {
            val result = dateParser.parseDate("15 сентября в 14:00")

            assertNotNull(result)
            var expectedDate = LocalDate.of(LocalDate.now().year, 9, 15)
            if (expectedDate.isBefore(LocalDate.now()) || expectedDate.isEqual(LocalDate.now())) {
                expectedDate = expectedDate.withYear(LocalDate.now().year + 1)
            }
            assertEquals(expectedDate, result?.toLocalDate())
            assertEquals(LocalTime.of(14, 0), result?.toLocalTime())
        }

        @Test
        fun `10 января`() {
            val result = dateParser.parseDate("10 января в 11:00")

            assertNotNull(result)
            val currentYear = LocalDate.now().year
            var expectedDate = LocalDate.of(currentYear, 1, 10)
            if (expectedDate.isBefore(LocalDate.now()) || expectedDate.isEqual(LocalDate.now())) {
                expectedDate = expectedDate.withYear(currentYear + 1)
            }
            assertEquals(expectedDate, result?.toLocalDate())
            assertEquals(LocalTime.of(11, 0), result?.toLocalTime())
        }

        @Test
        fun `25 точка 12`() {
            val result = dateParser.parseDate("25.12 в 18:00")

            assertNotNull(result)
            val currentYear = LocalDate.now().year
            var expectedDate = LocalDate.of(currentYear, 12, 25)
            if (expectedDate.isBefore(LocalDate.now()) || expectedDate.isEqual(LocalDate.now())) {
                expectedDate = expectedDate.withYear(currentYear + 1)
            }
            assertEquals(expectedDate, result?.toLocalDate())
            assertEquals(LocalTime.of(18, 0), result?.toLocalTime())
        }

        @Test
        fun `дата с годом`() {
            val nextYear = LocalDate.now().year + 1
            val result = dateParser.parseDate("15.06.$nextYear в 13:00")

            assertNotNull(result)
            assertEquals(LocalDate.of(nextYear, 6, 15), result?.toLocalDate())
            assertEquals(LocalTime.of(13, 0), result?.toLocalTime())
        }

        @Test
        fun `15 сен сокращенно`() {
            val result = dateParser.parseDate("15 сен в 16:00")

            assertNotNull(result)
            var expectedDate = LocalDate.of(LocalDate.now().year, 9, 15)
            if (expectedDate.isBefore(LocalDate.now()) || expectedDate.isEqual(LocalDate.now())) {
                expectedDate = expectedDate.withYear(LocalDate.now().year + 1)
            }
            assertEquals(expectedDate, result?.toLocalDate())
            assertEquals(LocalTime.of(16, 0), result?.toLocalTime())
        }
    }

    @Nested
    @DisplayName("Разные форматы времени")
    inner class TimeFormatsTest {

        @Test
        fun `время с двоеточием 14-00`() {
            val result = dateParser.parseDate("завтра в 14:00")

            assertNotNull(result)
            assertEquals(LocalTime.of(14, 0), result?.toLocalTime())
        }

        @Test
        fun `время с двоеточием 16-00`() {
            val result = dateParser.parseDate("послезавтра в 16:00")

            assertNotNull(result)
            assertEquals(LocalTime.of(16, 0), result?.toLocalTime())
        }

        @Test
        fun `время с точкой 14-30`() {
            val result = dateParser.parseDate("завтра в 14.30")

            assertNotNull(result)
            assertEquals(LocalTime.of(14, 30), result?.toLocalTime())
        }

        @Test
        fun `время с точкой 16-15`() {
            val result = dateParser.parseDate("послезавтра в 16.15")

            assertNotNull(result)
            assertEquals(LocalTime.of(16, 15), result?.toLocalTime())
        }

        @Test
        fun `время 9-00 одна цифра`() {
            val result = dateParser.parseDate("завтра в 9:00")

            assertNotNull(result)
            assertEquals(LocalTime.of(9, 0), result?.toLocalTime())
        }

        @Test
        fun `время 9-30 одна цифра`() {
            val result = dateParser.parseDate("послезавтра в 9:30")

            assertNotNull(result)
            assertEquals(LocalTime.of(9, 30), result?.toLocalTime())
        }

        @Test
        fun `без указания времени - дефолт 14-00`() {
            val result = dateParser.parseDate("завтра")

            assertNotNull(result)
            assertEquals(LocalTime.of(14, 0), result?.toLocalTime())
        }
    }

    @Nested
    @DisplayName("Граничные случаи")
    inner class EdgeCasesTest {

        @Test
        fun `невалидная дата`() {
            val result = dateParser.parseDate("в несуществующий день")

            assertNull(result)
        }

        @Test
        fun `регистронезависимость`() {
            val result = dateParser.parseDate("ЗАВТРА В 15:00")

            assertNotNull(result)
            assertEquals(LocalDate.now().plusDays(1), result?.toLocalDate())
            assertEquals(LocalTime.of(15, 0), result?.toLocalTime())
        }

        @Test
        fun `лишние пробелы`() {
            val result = dateParser.parseDate("  завтра   в   16:00  ")

            assertNotNull(result)
            assertEquals(LocalDate.now().plusDays(1), result?.toLocalDate())
            assertEquals(LocalTime.of(16, 0), result?.toLocalTime())
        }

        @Test
        fun `время в начале сообщения`() {
            val result = dateParser.parseDate("в 16:00 завтра")

            assertNotNull(result)
            assertEquals(LocalDate.now().plusDays(1), result?.toLocalDate())
            assertEquals(LocalTime.of(16, 0), result?.toLocalTime())
        }
    }

    @Nested
    @DisplayName("Реальные примеры использования")
    inner class RealWorldExamplesTest {

        @Test
        fun `стрижка послезавтра в 16-00`() {
            val result = dateParser.parseDate("стрижка послезавтра в 16:00")

            assertNotNull(result)
            assertEquals(LocalDate.now().plusDays(2), result?.toLocalDate())
            assertEquals(LocalTime.of(16, 0), result?.toLocalTime())
        }

        @Test
        fun `встреча в понедельник в 10-00`() {
            val result = dateParser.parseDate("встреча в понедельник в 10:00")

            assertNotNull(result)
            assertEquals(LocalTime.of(10, 0), result?.toLocalTime())
        }

        @Test
        fun `позвонить завтра в 15-30`() {
            val result = dateParser.parseDate("позвонить завтра в 15:30")

            assertNotNull(result)
            assertEquals(LocalDate.now().plusDays(1), result?.toLocalDate())
            assertEquals(LocalTime.of(15, 30), result?.toLocalTime())
        }

        @Test
        fun `день рождения 25 декабря в 18-00`() {
            val result = dateParser.parseDate("день рождения 25 декабря в 18:00")

            assertNotNull(result)
            var expectedDate = LocalDate.of(LocalDate.now().year, 12, 25)
            if (expectedDate.isBefore(LocalDate.now()) || expectedDate.isEqual(LocalDate.now())) {
                expectedDate = expectedDate.withYear(LocalDate.now().year + 1)
            }
            assertEquals(expectedDate, result?.toLocalDate())
            assertEquals(LocalTime.of(18, 0), result?.toLocalTime())
        }

        @Test
        fun `собрание в следующую пятницу в 14-00`() {
            val result = dateParser.parseDate("собрание в следующую пятницу в 14:00")

            assertNotNull(result)
            assertEquals(LocalTime.of(14, 0), result?.toLocalTime())
        }
    }
}
