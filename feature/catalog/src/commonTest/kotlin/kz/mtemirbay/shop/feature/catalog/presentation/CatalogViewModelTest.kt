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
import kz.mtemirbay.shop.core.data.cart.CartRepositoryImpl
import kz.mtemirbay.shop.core.data.cart.InMemoryCartStorage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {

    private val repository = FakeCatalogRepository()
    private val cartRepository = CartRepositoryImpl(InMemoryCartStorage())

    // viewModelScope работает на Dispatchers.Main. В тестах подменяем его тестовым диспетчером
    // с виртуальным временем: debounce в 300 мс проходит мгновенно через advanceTimeBy
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun initialLoadShowsProductsAndCategories() = runTest {
        val viewModel = CatalogViewModel(repository, cartRepository)

        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(repository.categories, state.categories)
        assertEquals(ProductsState.Content(repository.products), state.products)
    }

    @Test
    fun errorThenRetryShowsProducts() = runTest {
        repository.productsError = RuntimeException("Network")
        val viewModel = CatalogViewModel(repository, cartRepository)
        advanceUntilIdle()
        assertIs<ProductsState.Error>(viewModel.state.value.products)

        repository.productsError = null
        viewModel.onEvent(CatalogEvent.Retry)
        advanceUntilIdle()

        assertEquals(ProductsState.Content(repository.products), viewModel.state.value.products)
    }

    @Test
    fun selectingCategoryRequestsProductsOfThatCategory() = runTest {
        val viewModel = CatalogViewModel(repository, cartRepository)
        advanceUntilIdle()

        viewModel.onEvent(CatalogEvent.CategorySelected(3))
        advanceUntilIdle()

        assertEquals(3L to "", repository.productRequests.last())
        assertEquals(3L, viewModel.state.value.selectedCategoryId)
    }

    @Test
    fun fastTypingSendsSingleRequestAfterDebounce() = runTest {
        val viewModel = CatalogViewModel(repository, cartRepository)
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

    @Test
    fun cartBadgeFollowsCartChangesFromOtherScreens() = runTest {
        val viewModel = CatalogViewModel(repository, cartRepository)
        advanceUntilIdle()
        assertEquals(0, viewModel.state.value.cartItemCount)

        // Как будто товар добавили на экране товара — каталог узнаёт об этом через общий StateFlow
        cartRepository.add(1, "Смартфон", 189_990, "")
        cartRepository.add(1, "Смартфон", 189_990, "")
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.cartItemCount)
    }
}
