package kz.mtemirbay.shop.feature.checkout.domain

import kz.mtemirbay.shop.core.data.cart.CartItem

internal data class PlacedOrder(
    val id: Long,
    val totalKzt: Long,
)

internal interface CheckoutRepository {
    /**
     * Ошибки: [OrderRejectedException] — сервер отклонил заказ (повтор с теми же данными не поможет),
     * остальные — сеть или сервер недоступен (можно повторить)
     */
    suspend fun placeOrder(name: String, phone: String, address: String, items: List<CartItem>): Result<PlacedOrder>
}

/** @param code — [kz.mtemirbay.shop.core.model.ApiError.code] из ответа сервера */
internal class OrderRejectedException(val code: String) : Exception("Order rejected: $code")
