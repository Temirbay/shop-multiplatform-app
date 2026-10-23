package kz.mtemirbay.shop.core.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderRequest(
    val customerName: String,
    /** В нормализованном виде: +7XXXXXXXXXX */
    val phone: String,
    val address: String,
    val items: List<OrderItemRequest>,
)

/** Только id и количество: цену считает сервер по своим данным, клиенту в этом доверять нельзя */
@Serializable
data class OrderItemRequest(
    val productId: Long,
    val quantity: Int,
)

@Serializable
data class OrderDto(
    val id: Long,
    val totalKzt: Long,
    val itemCount: Int,
)

/**
 * Тело ответа 400. [code] — машинный код (например, имя [OrderValidationError]): по нему клиент
 * показывает свой локализованный текст. [message] — для логов и отладки, пользователю не показывается
 */
@Serializable
data class ApiError(
    val code: String,
    val message: String,
)

/** Строка списка «Мои заказы» — GET /orders */
@Serializable
data class OrderSummaryDto(
    val id: Long,
    val totalKzt: Long,
    val itemCount: Int,
    /** Миллисекунды Unix, UTC. В местное время переводит клиент */
    val createdAt: Long,
)

/** Заказ целиком — GET /orders/{id} */
@Serializable
data class OrderDetailsDto(
    val id: Long,
    val customerName: String,
    val phone: String,
    val address: String,
    val totalKzt: Long,
    val createdAt: Long,
    val items: List<OrderItemDto>,
)

/** [priceKzt] — цена на момент заказа, а не текущая цена товара */
@Serializable
data class OrderItemDto(
    val productId: Long,
    val title: String,
    val quantity: Int,
    val priceKzt: Long,
)
