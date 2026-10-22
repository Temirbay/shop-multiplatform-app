package kz.mtemirbay.shop.feature.catalog.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kz.mtemirbay.shop.core.model.CategoryDto
import kz.mtemirbay.shop.core.model.ProductDto

internal class CatalogApi(private val client: HttpClient) {

    suspend fun getCategories(): List<CategoryDto> =
        client.get("categories").body()

    suspend fun getProducts(categoryId: Long?, query: String): List<ProductDto> =
        client.get("products") {
            categoryId?.let { parameter("categoryId", it) }
            if (query.isNotBlank()) parameter("query", query)
        }.body()
}
