package kz.mtemirbay.shop.feature.catalog.domain

/** Контракт для presentation-слоя. ViewModel не знает, откуда данные: сервер, БД или фейк в тесте. */
internal interface CatalogRepository {
    suspend fun getCategories(): Result<List<Category>>
    suspend fun getProducts(categoryId: Long?, query: String): Result<List<Product>>
}
