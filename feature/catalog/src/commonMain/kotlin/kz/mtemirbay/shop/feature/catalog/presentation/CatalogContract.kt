package kz.mtemirbay.shop.feature.catalog.presentation

import kz.mtemirbay.shop.feature.catalog.domain.Category
import kz.mtemirbay.shop.feature.catalog.domain.Product

internal data class CatalogState(
    val categories: List<Category> = emptyList(),
    /** null — «Все категории» */
    val selectedCategoryId: Long? = null,
    val query: String = "",
    val products: ProductsState = ProductsState.Loading,
    /** Сколько штук в корзине — для бейджа */
    val cartItemCount: Int = 0,
)

internal sealed interface ProductsState {
    data object Loading : ProductsState
    data object Error : ProductsState
    data class Content(val items: List<Product>) : ProductsState
}

internal sealed interface CatalogEvent {
    data class QueryChanged(val query: String) : CatalogEvent
    data class CategorySelected(val categoryId: Long?) : CatalogEvent
    data object Retry : CatalogEvent
}
