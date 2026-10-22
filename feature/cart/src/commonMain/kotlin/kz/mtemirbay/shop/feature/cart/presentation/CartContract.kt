package kz.mtemirbay.shop.feature.cart.presentation

import kz.mtemirbay.shop.core.data.cart.CartItem

internal data class CartState(
    val items: List<CartItem> = emptyList(),
    val totalQuantity: Int = 0,
    val totalPriceKzt: Long = 0,
)

internal sealed interface CartEvent {
    data class Increase(val productId: Long) : CartEvent
    /** Не опускает количество ниже 1: удалить товар можно только кнопкой удаления */
    data class Decrease(val productId: Long) : CartEvent
    data class Remove(val productId: Long) : CartEvent
}
