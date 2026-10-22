package kz.mtemirbay.shop.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class PriceTest {

    @Test
    fun groupsThousandsWithNonBreakingSpace() {
        assertEquals("189 990 ₸", formatPriceKzt(189_990))
        assertEquals("1 049 990 ₸", formatPriceKzt(1_049_990))
    }

    @Test
    fun smallPriceHasNoSeparator() {
        assertEquals("990 ₸", formatPriceKzt(990))
        assertEquals("0 ₸", formatPriceKzt(0))
    }
}
