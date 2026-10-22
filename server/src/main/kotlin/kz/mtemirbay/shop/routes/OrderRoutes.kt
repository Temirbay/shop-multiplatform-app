package kz.mtemirbay.shop.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.post
import kz.mtemirbay.shop.core.model.ApiError
import kz.mtemirbay.shop.core.model.CreateOrderRequest
import kz.mtemirbay.shop.core.model.OrderDto
import kz.mtemirbay.shop.core.model.OrderValidation
import kz.mtemirbay.shop.data.OrderRepository
import kz.mtemirbay.shop.data.ProductRepository

const val PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND"

fun Route.orderRoutes(products: ProductRepository, orders: OrderRepository) {
    post("/orders") {
        val request = call.receive<CreateOrderRequest>()

        // Та же валидация, что в приложении: клиенту доверять нельзя
        OrderValidation.validate(request).firstOrNull()?.let { error ->
            return@post call.respondError(error.name, "Validation failed: $error")
        }

        // Цену берём из своих данных, а не из запроса. Заодно проверяем, что товары существуют
        var totalKzt = 0L
        for (item in request.items) {
            val product = products.getProduct(item.productId)
                ?: return@post call.respondError(PRODUCT_NOT_FOUND, "Product ${item.productId} not found")
            totalKzt += product.priceKzt * item.quantity
        }

        val order = orders.create(
            customerName = request.customerName.trim(),
            phone = OrderValidation.normalizePhone(request.phone),
            address = request.address.trim(),
            items = request.items,
            totalKzt = totalKzt,
        )
        call.respond(
            HttpStatusCode.Created,
            OrderDto(id = order.id, totalKzt = order.totalKzt, itemCount = request.items.sumOf { it.quantity }),
        )
    }
}

private suspend fun RoutingCall.respondError(code: String, message: String) {
    respond(HttpStatusCode.BadRequest, ApiError(code, message))
}
