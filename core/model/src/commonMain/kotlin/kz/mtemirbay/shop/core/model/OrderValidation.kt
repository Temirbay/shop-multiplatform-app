package kz.mtemirbay.shop.core.model

enum class OrderField { NAME, PHONE, ADDRESS, ITEMS }

enum class OrderValidationError(val field: OrderField) {
    NAME_EMPTY(OrderField.NAME),
    NAME_TOO_LONG(OrderField.NAME),
    PHONE_INVALID(OrderField.PHONE),
    ADDRESS_TOO_SHORT(OrderField.ADDRESS),
    ITEMS_EMPTY(OrderField.ITEMS),
    QUANTITY_INVALID(OrderField.ITEMS),
}

/**
 * Правила заказа — один код для приложения и сервера. Приложение проверяет форму сразу, без запроса.
 * Сервер проверяет то же самое ещё раз: запрос можно отправить и в обход приложения
 */
object OrderValidation {

    const val NAME_MAX_LENGTH = 100
    const val ADDRESS_MIN_LENGTH = 5
    const val QUANTITY_MAX = 99

    private val phoneRegex = Regex("""^\+7\d{10}$""")

    /** "+7 (701) 123-45-67" → "+77011234567". Пробелы, скобки и дефисы — только для удобства ввода */
    fun normalizePhone(raw: String): String = raw.filter { it.isDigit() || it == '+' }

    fun validateName(name: String): OrderValidationError? = when {
        name.isBlank() -> OrderValidationError.NAME_EMPTY
        name.trim().length > NAME_MAX_LENGTH -> OrderValidationError.NAME_TOO_LONG
        else -> null
    }

    fun validatePhone(phone: String): OrderValidationError? =
        if (phoneRegex.matches(normalizePhone(phone))) null else OrderValidationError.PHONE_INVALID

    fun validateAddress(address: String): OrderValidationError? =
        if (address.trim().length < ADDRESS_MIN_LENGTH) OrderValidationError.ADDRESS_TOO_SHORT else null

    fun validateItems(items: List<OrderItemRequest>): OrderValidationError? = when {
        items.isEmpty() -> OrderValidationError.ITEMS_EMPTY
        items.any { it.quantity !in 1..QUANTITY_MAX } -> OrderValidationError.QUANTITY_INVALID
        else -> null
    }

    fun validate(request: CreateOrderRequest): List<OrderValidationError> = listOfNotNull(
        validateName(request.customerName),
        validatePhone(request.phone),
        validateAddress(request.address),
        validateItems(request.items),
    )
}
