package kz.mtemirbay.shop.feature.catalog.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.mtemirbay.shop.core.ui.MviViewModel
import kz.mtemirbay.shop.feature.catalog.domain.CatalogRepository
import kotlin.time.Duration.Companion.milliseconds

internal class CatalogViewModel(
    private val repository: CatalogRepository,
) : MviViewModel<CatalogState, CatalogEvent, Nothing>(CatalogState()) {

    private val reloadTrigger = MutableStateFlow(0)

    init {
        loadCategories()
        observeProducts()
    }

    override fun onEvent(event: CatalogEvent) {
        when (event) {
            is CatalogEvent.QueryChanged -> setState { copy(query = event.query) }
            is CatalogEvent.CategorySelected -> setState { copy(selectedCategoryId = event.categoryId) }
            CatalogEvent.Retry -> {
                if (state.value.categories.isEmpty()) loadCategories()
                reloadTrigger.update { it + 1 }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            // Без категорий экран работает — просто нет чипсов. Поэтому ошибку здесь не показываем
            repository.getCategories().onSuccess { categories ->
                setState { copy(categories = categories) }
            }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeProducts() {
        combine(
            state.map { it.query.trim() }.distinctUntilChanged().debounce(SEARCH_DEBOUNCE_MS.milliseconds),
            state.map { it.selectedCategoryId }.distinctUntilChanged(),
            reloadTrigger,
        ) { query, categoryId, _ -> query to categoryId }
            .mapLatest { (query, categoryId) ->
                setState { copy(products = ProductsState.Loading) }
                repository.getProducts(categoryId, query)
            }
            .onEach { result ->
                val products = result.fold(
                    onSuccess = { ProductsState.Content(it) },
                    onFailure = { ProductsState.Error },
                )
                setState { copy(products = products) }
            }
            .launchIn(viewModelScope)
    }

    companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
