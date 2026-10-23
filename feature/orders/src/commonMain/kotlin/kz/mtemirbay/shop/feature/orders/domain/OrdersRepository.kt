package kz.mtemirbay.shop.feature.orders.domain

internal data class OrderSummary(
    val id: Long,
    val totalKzt: Long,
    val itemCount: Int,
    val createdAtMillis: Long,
)

internal data class OrderLine(
    val title: String,
    val quantity: Int,
    val priceKzt: Long,
)

internal data class OrderDetails(
    val id: Long,
    val customerName: String,
    val phone: String,
    val address: String,
    val totalKzt: Long,
    val createdAtMillis: Long,
    val lines: List<OrderLine>,
)

internal interface OrdersRepository {
    suspend fun getMyOrders(): Result<List<OrderSummary>>

    /** Ошибка [OrderNotFoundException] — заказа нет или он чужой */
    suspend fun getOrder(id: Long): Result<OrderDetails>
}

internal class OrderNotFoundException(id: Long) : Exception("Order $id not found")
