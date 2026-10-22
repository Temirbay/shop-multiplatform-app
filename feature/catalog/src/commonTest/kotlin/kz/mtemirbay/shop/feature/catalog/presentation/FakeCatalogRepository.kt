package kz.mtemirbay.shop.feature.catalog.presentation

import kz.mtemirbay.shop.feature.catalog.domain.CatalogRepository
import kz.mtemirbay.shop.feature.catalog.domain.Category
import kz.mtemirbay.shop.feature.catalog.domain.Product

internal class FakeCatalogRepository : CatalogRepository {

    var categories: List<Category> = listOf(Category(1, "Электроника"), Category(3, "Книги"))
    var products: List<Product> = listOf(
        Product(1, "Смартфон", 189_990, ""),
        Product(11, "Kotlin in Action", 12_990, ""),
    )
    var productsError: Throwable? = null

    /** Параметры каждого запроса товаров — чтобы проверить, сколько раз и с чем ходили «на сервер» */
    val productRequests = mutableListOf<Pair<Long?, String>>()

    override suspend fun getCategories(): Result<List<Category>> = Result.success(categories)

    override suspend fun getProducts(categoryId: Long?, query: String): Result<List<Product>> {
        productRequests += categoryId to query
        return productsError?.let { Result.failure(it) } ?: Result.success(products)
    }
}
