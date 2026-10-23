package kz.mtemirbay.shop.data

import kz.mtemirbay.shop.core.model.OrderDetailsDto

/** Позиция нового заказа с ценой, которую сервер взял из своих данных */
data class NewOrderItem(
    val productId: Long,
    val quantity: Int,
    val priceKzt: Long,
)

data class CreatedOrder(
    val id: Long,
    val totalKzt: Long,
    val itemCount: Int,
)

interface OrderRepository {
    suspend fun create(customerName: String, phone: String, address: String, items: List<NewOrderItem>): CreatedOrder

    suspend fun get(id: Long): OrderDetailsDto?
}
