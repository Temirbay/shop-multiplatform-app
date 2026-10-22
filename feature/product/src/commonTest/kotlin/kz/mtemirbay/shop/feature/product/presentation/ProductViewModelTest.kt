package kz.mtemirbay.shop.feature.product.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kz.mtemirbay.shop.core.data.cart.CartItem
import kz.mtemirbay.shop.core.data.cart.CartRepositoryImpl
import kz.mtemirbay.shop.core.data.cart.InMemoryCartStorage
import kz.mtemirbay.shop.feature.product.domain.ProductDetails
import kz.mtemirbay.shop.feature.product.domain.ProductNotFoundException
import kz.mtemirbay.shop.feature.product.domain.ProductRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModelTest {

    private val product = ProductDetails(1, "Смартфон", "Описание", 189_990, "")
    private val repository = FakeProductRepository()
    private val cartRepository = CartRepositoryImpl(InMemoryCartStorage())

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsProduct() = runTest {
        repository.result = Result.success(product)
        val viewModel = ProductViewModel(productId = 1, repository = repository, cartRepository = cartRepository)

        advanceUntilIdle()

        assertEquals(ProductState.Content(product), viewModel.state.value)
        assertEquals(listOf(1L), repository.requestedIds)
    }

    @Test
    fun missingProductShowsNotFound() = runTest {
        repository.result = Result.failure(ProductNotFoundException(999))
        val viewModel = ProductViewModel(productId = 999, repository = repository, cartRepository = cartRepository)

        advanceUntilIdle()

        assertEquals(ProductState.NotFound, viewModel.state.value)
    }

    @Test
    fun networkErrorThenRetryShowsProduct() = runTest {
        repository.result = Result.failure(RuntimeException("Network"))
        val viewModel = ProductViewModel(productId = 1, repository = repository, cartRepository = cartRepository)
        advanceUntilIdle()
        assertEquals(ProductState.Error, viewModel.state.value)

        repository.result = Result.success(product)
        viewModel.onEvent(ProductEvent.Retry)
        advanceUntilIdle()

        assertEquals(ProductState.Content(product), viewModel.state.value)
    }

    @Test
    fun addToCartPutsProductIntoCartAndSendsEffect() = runTest {
        repository.result = Result.success(product)
        val viewModel = ProductViewModel(productId = 1, repository = repository, cartRepository = cartRepository)
        advanceUntilIdle()

        val effect = async { viewModel.effects.first() }
        viewModel.onEvent(ProductEvent.AddToCart)
        advanceUntilIdle()

        assertEquals(ProductEffect.AddedToCart, effect.await())
        assertEquals(listOf(CartItem(1, "Смартфон", 189_990, "", quantity = 1)), cartRepository.items.value)
    }

    private class FakeProductRepository : ProductRepository {
        var result: Result<ProductDetails> = Result.failure(IllegalStateException("Not set"))
        val requestedIds = mutableListOf<Long>()

        override suspend fun getProduct(id: Long): Result<ProductDetails> {
            requestedIds += id
            return result
        }
    }
}
