package kz.mtemirbay.shop.feature.catalog.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {

    private val repository = FakeCatalogRepository()

    // viewModelScope работает на Dispatchers.Main. В тестах подменяем его тестовым диспетчером
    // с виртуальным временем: debounce в 300 мс проходит мгновенно через advanceTimeBy
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun initialLoadShowsProductsAndCategories() = runTest {
        val viewModel = CatalogViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(repository.categories, state.categories)
        assertEquals(ProductsState.Content(repository.products), state.products)
    }

    @Test
    fun errorThenRetryShowsProducts() = runTest {
        repository.productsError = RuntimeException("Network")
        val viewModel = CatalogViewModel(repository)
        advanceUntilIdle()
        assertIs<ProductsState.Error>(viewModel.state.value.products)

        repository.productsError = null
        viewModel.onEvent(CatalogEvent.Retry)
        advanceUntilIdle()

        assertEquals(ProductsState.Content(repository.products), viewModel.state.value.products)
    }

    @Test
    fun selectingCategoryRequestsProductsOfThatCategory() = runTest {
        val viewModel = CatalogViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(CatalogEvent.CategorySelected(3))
        advanceUntilIdle()

        assertEquals(3L to "", repository.productRequests.last())
        assertEquals(3L, viewModel.state.value.selectedCategoryId)
    }

    @Test
    fun fastTypingSendsSingleRequestAfterDebounce() = runTest {
        val viewModel = CatalogViewModel(repository)
        advanceUntilIdle()
        repository.productRequests.clear()

        // Печатаем быстрее, чем debounce
        for (query in listOf("k", "ko", "kot")) {
            viewModel.onEvent(CatalogEvent.QueryChanged(query))
            advanceTimeBy(100)
        }
        runCurrent()
        assertEquals(emptyList(), repository.productRequests)

        advanceUntilIdle()

        assertEquals(listOf<Pair<Long?, String>>(null to "kot"), repository.productRequests)
    }
}
