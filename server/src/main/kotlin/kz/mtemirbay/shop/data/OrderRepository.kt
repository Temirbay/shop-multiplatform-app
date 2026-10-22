package kz.mtemirbay.shop.data

import kz.mtemirbay.shop.core.model.OrderItemRequest

data class Order(
    val id: Long,
    val customerName: String,
    val phone: String,
    val address: String,
    val items: List<OrderItemRequest>,
    val totalKzt: Long,
)

interface OrderRepository {
    suspend fun create(
        customerName: String,
        phone: String,
        address: String,
        items: List<OrderItemRequest>,
        totalKzt: Long,
    ): Order
}
