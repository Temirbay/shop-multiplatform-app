package kz.mtemirbay.shop.routes

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kz.mtemirbay.shop.data.ProductRepository

fun Route.categoryRoutes(repository: ProductRepository) {
    get("/categories") {
        call.respond(repository.getCategories())
    }
}
