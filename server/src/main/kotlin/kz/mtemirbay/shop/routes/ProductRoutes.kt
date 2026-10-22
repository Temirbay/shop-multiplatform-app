package kz.mtemirbay.shop.routes

import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kz.mtemirbay.shop.data.ProductRepository

fun Route.productRoutes(repository: ProductRepository) {
    get("/products") {
        val categoryId = call.queryParameters["categoryId"]?.toLongOrNull()
        val query = call.queryParameters["query"]
        call.respond(repository.getProducts(categoryId, query))
    }
    get("/products/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: throw BadRequestException("Invalid product id")
        val product = repository.getProduct(id)
            ?: throw NotFoundException("Product $id not found")
        call.respond(product)
    }
}
