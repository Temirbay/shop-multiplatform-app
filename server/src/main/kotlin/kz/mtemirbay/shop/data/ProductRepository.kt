package kz.mtemirbay.shop.data

import kz.mtemirbay.shop.core.model.CategoryDto
import kz.mtemirbay.shop.core.model.ProductDto

interface ProductRepository {
    suspend fun getCategories(): List<CategoryDto>
    suspend fun getProducts(categoryId: Long? = null, query: String? = null): List<ProductDto>
    suspend fun getProduct(id: Long): ProductDto?
}
