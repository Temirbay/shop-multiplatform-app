package kz.mtemirbay.shop.feature.orders.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kz.mtemirbay.shop.core.model.OrderDetailsDto
import kz.mtemirbay.shop.core.model.OrderSummaryDto

/** Токен в запросы добавляет сам HttpClient (BearerSessionPlugin) — здесь о нём думать не нужно */
internal class OrdersApi(private val client: HttpClient) {

    suspend fun getMyOrders(): List<OrderSummaryDto> = client.get("orders").body()

    suspend fun getOrder(id: Long): OrderDetailsDto = client.get("orders/$id").body()
}
