package kz.mtemirbay.shop.feature.checkout.data

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kz.mtemirbay.shop.core.data.cart.CartItem
import kz.mtemirbay.shop.core.model.ApiError
import kz.mtemirbay.shop.core.model.CreateOrderRequest
import kz.mtemirbay.shop.core.model.OrderItemRequest
import kz.mtemirbay.shop.core.model.OrderValidation
import kz.mtemirbay.shop.core.network.safeApiCall
import kz.mtemirbay.shop.feature.checkout.domain.CheckoutRepository
import kz.mtemirbay.shop.feature.checkout.domain.OrderRejectedException
import kz.mtemirbay.shop.feature.checkout.domain.PlacedOrder

internal class CheckoutRepositoryImpl(private val api: OrderApi) : CheckoutRepository {

    override suspend fun placeOrder(
        name: String,
        phone: String,
        address: String,
        items: List<CartItem>,
    ): Result<PlacedOrder> {
        val request = CreateOrderRequest(
            customerName = name.trim(),
            phone = OrderValidation.normalizePhone(phone),
            address = address.trim(),
            items = items.map { OrderItemRequest(it.productId, it.quantity) },
        )
        val result = safeApiCall {
            val order = api.createOrder(request)
            PlacedOrder(id = order.id, totalKzt = order.totalKzt)
        }
        val error = result.exceptionOrNull()
        if (error is ClientRequestException && error.response.status == HttpStatusCode.BadRequest) {
            // Тело ответа 400 — ApiError с кодом. Не разобралось — считаем обычной ошибкой
            val apiError = safeApiCall { error.response.body<ApiError>() }.getOrNull()
            if (apiError != null) return Result.failure(OrderRejectedException(apiError.code))
        }
        return result
    }
}
