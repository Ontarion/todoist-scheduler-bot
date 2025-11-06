package com.example.todoistschedulerbot.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.DayOfWeek

@Service
class DateParser {

    private val logger = LoggerFactory.getLogger(DateParser::class.java)

    private val monthsRu = mapOf(
        "январь" to 1, "января" to 1, "янв" to 1,
        "февраль" to 2, "февраля" to 2, "фев" to 2,
        "март" to 3, "марта" to 3, "мар" to 3,
        "апрель" to 4, "апреля" to 4, "апр" to 4,
        "май" to 5, "мая" to 5,
        "июнь" to 6, "июня" to 6, "июн" to 6,
        "июль" to 7, "июля" to 7, "июл" to 7,
        "август" to 8, "августа" to 8, "авг" to 8,
        "сентябрь" to 9, "сентября" to 9, "сен" to 9,
        "октябрь" to 10, "октября" to 10, "окт" to 10,
        "ноябрь" to 11, "ноября" to 11, "ноя" to 11,
        "декабрь" to 12, "декабря" to 12, "дек" to 12
    )

    private val weekdaysRu = mapOf(
        "понедельник" to DayOfWeek.MONDAY, "пн" to DayOfWeek.MONDAY, "пон" to DayOfWeek.MONDAY,
        "вторник" to DayOfWeek.TUESDAY, "вт" to DayOfWeek.TUESDAY, "втор" to DayOfWeek.TUESDAY,
        "среда" to DayOfWeek.WEDNESDAY, "среду" to DayOfWeek.WEDNESDAY, "ср" to DayOfWeek.WEDNESDAY, "сред" to DayOfWeek.WEDNESDAY,
        "четверг" to DayOfWeek.THURSDAY, "чт" to DayOfWeek.THURSDAY, "четв" to DayOfWeek.THURSDAY,
        "пятница" to DayOfWeek.FRIDAY, "пятницу" to DayOfWeek.FRIDAY, "пт" to DayOfWeek.FRIDAY, "пятн" to DayOfWeek.FRIDAY,
        "суббота" to DayOfWeek.SATURDAY, "субботу" to DayOfWeek.SATURDAY, "сб" to DayOfWeek.SATURDAY, "субб" to DayOfWeek.SATURDAY,
        "воскресенье" to DayOfWeek.SUNDAY, "вс" to DayOfWeek.SUNDAY, "воскр" to DayOfWeek.SUNDAY
    )

    private val relativeDates = mapOf(
        "послезавтра" to 2,
        "завтра" to 1,
        "сегодня" to 0
    )

    private val timePattern = Regex("""(?:^|\s)в\s+(\d{1,2})[:.](\d{2})(?:\s|$)|(?<=\s)(\d{1,2})[:.](\d{2})(?=\s|$)""")

    fun parseDate(text: String): LocalDateTime? {
        val normalizedText = text.lowercase().trim()
        logger.info("Парсим текст: $normalizedText")

        val parsedTime = parseTime(normalizedText)
        val finalTime = parsedTime ?: Pair(14, 0)
        logger.info("Найдено время: ${finalTime.first}:${finalTime.second.toString().padStart(2, '0')}")

        val parsedDate = parseRelativeDate(normalizedText)
            ?: parseWeekday(normalizedText)
            ?: parseSpecificDate(normalizedText)

        if (parsedDate != null) {
            logger.info("Найдена дата: $parsedDate")
        } else {
            logger.warn("Не удалось распарсить дату из текста: $normalizedText")
        }

        return parsedDate?.atTime(finalTime.first, finalTime.second)
    }

    private fun parseTime(text: String): Pair<Int, Int>? {
        val match = timePattern.find(text)
        return if (match != null) {
            try {
                val hour = (match.groupValues[1].takeIf { it.isNotEmpty() } 
                    ?: match.groupValues[3]).toInt()
                val minute = (match.groupValues[2].takeIf { it.isNotEmpty() } 
                    ?: match.groupValues[4]).toInt()
                if (hour in 0..23 && minute in 0..59) {
                    Pair(hour, minute)
                } else null
            } catch (e: NumberFormatException) {
                null
            }
        } else null
    }

    private fun parseRelativeDate(text: String): LocalDate? {
        for ((word, daysOffset) in relativeDates) {
            if (containsWord(text, word)) {
                logger.debug("Найдено относительное слово '$word', добавляем дней: $daysOffset")
                return LocalDate.now().plusDays(daysOffset.toLong())
            }
        }
        return null
    }

    private fun parseWeekday(text: String): LocalDate? {
        val sortedWeekdays = weekdaysRu.entries.sortedByDescending { it.key.length }
        
        for ((weekdayName, weekday) in sortedWeekdays) {
            if (containsWord(text, weekdayName)) {
                val today = LocalDate.now()
                val daysAhead = weekday.value - today.dayOfWeek.value
                val hasNextModifier = containsWord(text, "следующую") || 
                                     containsWord(text, "следующий") || 
                                     containsWord(text, "следующая")
                var adjustedDaysAhead = daysAhead

                if (hasNextModifier) {
                    // "Следующий X" означает пропустить ближайший X и взять следующий
                    // Если день недели еще впереди на этой неделе, добавляем всю неделю
                    // Если день недели уже прошел, добавляем неделю к стандартному смещению
                    if (daysAhead > 0) {
                        // День еще впереди на этой неделе, но мы хотим следующий, поэтому +7
                        adjustedDaysAhead = daysAhead + 7
                    } else {
                        // День уже прошел, берем следующую неделю
                        adjustedDaysAhead = daysAhead + 14
                    }
                } else if (daysAhead <= 0) {
                    adjustedDaysAhead += 7
                }

                logger.debug("Распознан день недели '$weekdayName', добавляем дней: $adjustedDaysAhead")
                return today.plusDays(adjustedDaysAhead.toLong())
            }
        }
        return null
    }

    private fun parseSpecificDate(text: String): LocalDate? {
        val currentYear = LocalDate.now().year

        val monthPattern = Regex("""(\d{1,2})\s+(январь|января|янв|февраль|февраля|фев|март|марта|мар|апрель|апреля|апр|май|мая|июнь|июня|июн|июль|июля|июл|август|августа|авг|сентябрь|сентября|сен|октябрь|октября|окт|ноябрь|ноября|ноя|декабрь|декабря|дек)""")
        val monthMatch = monthPattern.find(text)

        if (monthMatch != null) {
            return try {
                val day = monthMatch.groupValues[1].toInt()
                val monthName = monthMatch.groupValues[2]
                val month = monthsRu[monthName]

                if (month != null && day in 1..31) {
                    var resultDate = LocalDate.of(currentYear, month, day)
                    if (resultDate.isBefore(LocalDate.now()) || resultDate.isEqual(LocalDate.now())) {
                        resultDate = LocalDate.of(currentYear + 1, month, day)
                    }
                    resultDate
                } else null
            } catch (e: Exception) {
                null
            }
        }

        val datePattern = Regex("""(?:^|\s)(\d{1,2})[./](\d{1,2})(?:[./](\d{4}))?(?:\s|$)""")
        val dateMatch = datePattern.find(text)

        if (dateMatch != null) {
            return try {
                val day = dateMatch.groupValues[1].toInt()
                val month = dateMatch.groupValues[2].toInt()
                val year = dateMatch.groupValues.getOrNull(3)?.toIntOrNull() ?: currentYear

                if (day in 1..31 && month in 1..12) {
                    var resultDate = LocalDate.of(year, month, day)
                    if (dateMatch.groupValues.getOrNull(3) == null && (resultDate.isBefore(LocalDate.now()) || resultDate.isEqual(LocalDate.now()))) {
                        resultDate = LocalDate.of(currentYear + 1, month, day)
                    }
                    resultDate
                } else null
            } catch (e: Exception) {
                null
            }
        }

        return null
    }

    private fun containsWord(text: String, word: String): Boolean {
        val words = text.split(Regex("""[\s,.:;!?]+"""))
        return words.contains(word)
    }
}
