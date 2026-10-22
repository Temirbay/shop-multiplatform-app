package kz.mtemirbay.shop.feature.product.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kz.mtemirbay.shop.core.model.ProductDto

internal class ProductApi(private val client: HttpClient) {

    suspend fun getProduct(id: Long): ProductDto =
        client.get("products/$id").body()
}
