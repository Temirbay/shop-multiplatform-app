package kz.mtemirbay.shop.feature.product.data

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kz.mtemirbay.shop.core.model.ProductDto
import kz.mtemirbay.shop.core.network.safeApiCall
import kz.mtemirbay.shop.feature.product.domain.ProductDetails
import kz.mtemirbay.shop.feature.product.domain.ProductNotFoundException
import kz.mtemirbay.shop.feature.product.domain.ProductRepository

internal class ProductRepositoryImpl(private val api: ProductApi) : ProductRepository {

    override suspend fun getProduct(id: Long): Result<ProductDetails> {
        val result = safeApiCall { api.getProduct(id).toDomain() }
        // HTTP-подробности (ClientRequestException, 404) не выходят за пределы data-слоя:
        // ViewModel получает доменную ошибку
        val error = result.exceptionOrNull()
        return if (error is ClientRequestException && error.response.status == HttpStatusCode.NotFound) {
            Result.failure(ProductNotFoundException(id))
        } else {
            result
        }
    }
}

private fun ProductDto.toDomain() = ProductDetails(
    id = id,
    title = title,
    description = description,
    priceKzt = priceKzt,
    imageUrl = imageUrl,
)
