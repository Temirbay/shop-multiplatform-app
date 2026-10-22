package kz.mtemirbay.shop.feature.checkout.presentation

import kz.mtemirbay.shop.core.data.cart.CartItem
import kz.mtemirbay.shop.core.model.OrderField
import kz.mtemirbay.shop.core.model.OrderValidationError

internal data class CheckoutState(
    val name: String = "",
    /** Только 10 цифр номера, без +7 и форматирования — маску рисует PhoneVisualTransformation */
    val phone: String = "",
    val address: String = "",
    /** Ошибки, которые уже пора показать: поле потеряло фокус или была попытка отправки */
    val errors: Map<OrderField, OrderValidationError> = emptyMap(),
    /** Состав заказа для чека */
    val items: List<CartItem> = emptyList(),
    val itemCount: Int = 0,
    val totalKzt: Long = 0,
    val isSubmitting: Boolean = false,
)

internal sealed interface CheckoutEvent {
    data class NameChanged(val value: String) : CheckoutEvent
    data class PhoneChanged(val value: String) : CheckoutEvent
    data class AddressChanged(val value: String) : CheckoutEvent
    data class FieldFocusLost(val field: OrderField) : CheckoutEvent
    data object Submit : CheckoutEvent
}

internal sealed interface CheckoutEffect {
    data class OrderPlaced(val orderId: Long, val totalKzt: Long) : CheckoutEffect
    data object NetworkError : CheckoutEffect
    /** Сервер отклонил заказ по причине, не связанной с полем формы (например, товар исчез) */
    data object ProductUnavailable : CheckoutEffect
}
