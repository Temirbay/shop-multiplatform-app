package kz.mtemirbay.shop.data

import kz.mtemirbay.shop.core.model.OrderDetailsDto
import kz.mtemirbay.shop.core.model.OrderSummaryDto

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
    /** @param userId null — заказ гостя */
    suspend fun create(
        userId: Long?,
        customerName: String,
        phone: String,
        address: String,
        items: List<NewOrderItem>,
    ): CreatedOrder

    /** Заказы пользователя, новые сверху */
    suspend fun listForUser(userId: Long): List<OrderSummaryDto>

    /** null — заказа нет или он чужой. Различать нельзя: иначе по ответу можно узнать, какие номера заказов существуют */
    suspend fun getForUser(id: Long, userId: Long): OrderDetailsDto?
}
