package kz.mtemirbay.shop.feature.orders.presentation

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderDateTest {

    // 2026-10-23 05:02 UTC
    private val millis = 1_792_731_720_000L

    @Test
    fun formatsInGivenTimeZone() {
        assertEquals("23 октября 2026, 5:02", formatOrderDate(millis, TimeZone.UTC))
        // Алматы — UTC+5: то же мгновение, но местное время другое
        assertEquals("23 октября 2026, 10:02", formatOrderDate(millis, TimeZone.of("Asia/Almaty")))
    }
}
