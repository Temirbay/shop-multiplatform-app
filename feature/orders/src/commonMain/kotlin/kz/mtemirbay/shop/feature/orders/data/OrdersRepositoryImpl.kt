package kz.mtemirbay.shop.feature.orders.data

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kz.mtemirbay.shop.core.model.OrderDetailsDto
import kz.mtemirbay.shop.core.model.OrderSummaryDto
import kz.mtemirbay.shop.core.network.safeApiCall
import kz.mtemirbay.shop.feature.orders.domain.OrderDetails
import kz.mtemirbay.shop.feature.orders.domain.OrderLine
import kz.mtemirbay.shop.feature.orders.domain.OrderNotFoundException
import kz.mtemirbay.shop.feature.orders.domain.OrderSummary
import kz.mtemirbay.shop.feature.orders.domain.OrdersRepository

internal class OrdersRepositoryImpl(private val api: OrdersApi) : OrdersRepository {

    override suspend fun getMyOrders(): Result<List<OrderSummary>> = safeApiCall {
        api.getMyOrders().map { it.toDomain() }
    }

    override suspend fun getOrder(id: Long): Result<OrderDetails> {
        val result = safeApiCall { api.getOrder(id).toDomain() }
        val error = result.exceptionOrNull()
        return if (error is ClientRequestException && error.response.status == HttpStatusCode.NotFound) {
            Result.failure(OrderNotFoundException(id))
        } else {
            result
        }
    }
}

private fun OrderSummaryDto.toDomain() = OrderSummary(id, totalKzt, itemCount, createdAt)

private fun OrderDetailsDto.toDomain() = OrderDetails(
    id = id,
    customerName = customerName,
    phone = phone,
    address = address,
    totalKzt = totalKzt,
    createdAtMillis = createdAt,
    lines = items.map { OrderLine(it.title, it.quantity, it.priceKzt) },
)
