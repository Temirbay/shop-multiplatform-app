package kz.mtemirbay.shop.feature.orders.presentation

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private val MONTHS = listOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря",
)

/**
 * 1792748520000 → «23 октября 2026, 10:42» в часовом поясе устройства.
 * Сервер хранит время в UTC, а в местное переводит клиент — так у пользователя в Астане и в Лондоне время своё
 */
internal fun formatOrderDate(epochMillis: Long, timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val dateTime = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone)
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "${dateTime.day} ${MONTHS[dateTime.month.ordinal]} ${dateTime.year}, ${dateTime.hour}:$minute"
}
