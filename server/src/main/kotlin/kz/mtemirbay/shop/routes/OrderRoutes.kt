package kz.mtemirbay.shop.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kz.mtemirbay.shop.auth.JwtConfig
import kz.mtemirbay.shop.core.model.ApiError
import kz.mtemirbay.shop.core.model.CreateOrderRequest
import kz.mtemirbay.shop.core.model.OrderDto
import kz.mtemirbay.shop.core.model.OrderValidation
import kz.mtemirbay.shop.data.NewOrderItem
import kz.mtemirbay.shop.data.OrderRepository
import kz.mtemirbay.shop.data.ProductRepository

const val PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND"

fun Route.orderRoutes(products: ProductRepository, orders: OrderRepository) {
    // optional: без токена — заказ гостя, с токеном — привязывается к аккаунту
    authenticate(JwtConfig.AUTH_NAME, optional = true) {
        post("/orders") { createOrder(products, orders) }
    }
    authenticate(JwtConfig.AUTH_NAME) {
        get("/orders") {
            call.respond(orders.listForUser(call.userId()))
        }
        get("/orders/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: throw BadRequestException("Invalid order id")
            // Чужой заказ — 404, а не 403: не подтверждаем, что заказ с таким номером существует
            val order = orders.getForUser(id, call.userId()) ?: throw NotFoundException("Order $id not found")
            call.respond(order)
        }
    }
}

private suspend fun RoutingContext.createOrder(products: ProductRepository, orders: OrderRepository) {
    val request = call.receive<CreateOrderRequest>()

    // Та же валидация, что в приложении: клиенту доверять нельзя
    OrderValidation.validate(request).firstOrNull()?.let { error ->
        return call.respondError(error.name, "Validation failed: $error")
    }

    // Цену берём из своих данных, а не из запроса. Заодно проверяем, что товары существуют
    val pricedItems = request.items.map { item ->
        val product = products.getProduct(item.productId)
            ?: return call.respondError(PRODUCT_NOT_FOUND, "Product ${item.productId} not found")
        NewOrderItem(productId = item.productId, quantity = item.quantity, priceKzt = product.priceKzt)
    }

    val order = orders.create(
        userId = call.principal<JWTPrincipal>()?.let { call.userId() },
        customerName = request.customerName.trim(),
        phone = OrderValidation.normalizePhone(request.phone),
        address = request.address.trim(),
        items = pricedItems,
    )
    call.respond(HttpStatusCode.Created, OrderDto(id = order.id, totalKzt = order.totalKzt, itemCount = order.itemCount))
}

private suspend fun RoutingCall.respondError(code: String, message: String) {
    respond(HttpStatusCode.BadRequest, ApiError(code, message))
}
