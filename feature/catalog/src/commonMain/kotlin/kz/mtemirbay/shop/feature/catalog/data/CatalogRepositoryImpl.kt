package kz.mtemirbay.shop.feature.catalog.data

import kz.mtemirbay.shop.core.network.safeApiCall
import kz.mtemirbay.shop.feature.catalog.domain.CatalogRepository
import kz.mtemirbay.shop.feature.catalog.domain.Category
import kz.mtemirbay.shop.feature.catalog.domain.Product

internal class CatalogRepositoryImpl(private val api: CatalogApi) : CatalogRepository {

    override suspend fun getCategories(): Result<List<Category>> = safeApiCall {
        api.getCategories().map { it.toDomain() }
    }

    override suspend fun getProducts(categoryId: Long?, query: String): Result<List<Product>> = safeApiCall {
        api.getProducts(categoryId, query).map { it.toDomain() }
    }
}
