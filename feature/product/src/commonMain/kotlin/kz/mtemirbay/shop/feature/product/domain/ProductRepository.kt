package kz.mtemirbay.shop.feature.product.domain

internal interface ProductRepository {
    /** Ошибка [ProductNotFoundException] — товара нет, повтор не поможет. Остальные — сеть/сервер, можно повторить */
    suspend fun getProduct(id: Long): Result<ProductDetails>
}

internal class ProductNotFoundException(id: Long) : Exception("Product $id not found")
