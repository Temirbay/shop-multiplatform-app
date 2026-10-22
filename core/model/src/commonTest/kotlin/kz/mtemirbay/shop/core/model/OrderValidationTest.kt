package kz.mtemirbay.shop.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OrderValidationTest {

    private val validRequest = CreateOrderRequest(
        customerName = "Мирас",
        phone = "+77011234567",
        address = "Алматы, Абая 1",
        items = listOf(OrderItemRequest(productId = 1, quantity = 2)),
    )

    @Test
    fun validRequestHasNoErrors() {
        assertEquals(emptyList(), OrderValidation.validate(validRequest))
    }

    @Test
    fun phoneWithFormattingIsValid() {
        assertNull(OrderValidation.validatePhone("+7 (701) 123-45-67"))
        assertEquals("+77011234567", OrderValidation.normalizePhone("+7 (701) 123-45-67"))
    }

    @Test
    fun phoneWithoutPlusSevenOrWrongLengthIsInvalid() {
        assertEquals(OrderValidationError.PHONE_INVALID, OrderValidation.validatePhone("87011234567"))
        assertEquals(OrderValidationError.PHONE_INVALID, OrderValidation.validatePhone("+7701123456"))
        assertEquals(OrderValidationError.PHONE_INVALID, OrderValidation.validatePhone(""))
    }

    @Test
    fun blankNameAndShortAddressAreInvalid() {
        val errors = OrderValidation.validate(validRequest.copy(customerName = "  ", address = "Ал"))

        assertEquals(listOf(OrderValidationError.NAME_EMPTY, OrderValidationError.ADDRESS_TOO_SHORT), errors)
    }

    @Test
    fun tooLongNameIsInvalid() {
        assertEquals(OrderValidationError.NAME_TOO_LONG, OrderValidation.validateName("а".repeat(101)))
    }

    @Test
    fun emptyItemsAndWrongQuantityAreInvalid() {
        assertEquals(OrderValidationError.ITEMS_EMPTY, OrderValidation.validateItems(emptyList()))
        assertEquals(
            OrderValidationError.QUANTITY_INVALID,
            OrderValidation.validateItems(listOf(OrderItemRequest(1, quantity = 0))),
        )
        assertEquals(
            OrderValidationError.QUANTITY_INVALID,
            OrderValidation.validateItems(listOf(OrderItemRequest(1, quantity = 100))),
        )
    }

    @Test
    fun everyErrorBelongsToItsField() {
        assertEquals(OrderField.PHONE, OrderValidationError.PHONE_INVALID.field)
        assertEquals(OrderField.ITEMS, OrderValidationError.QUANTITY_INVALID.field)
    }
}
