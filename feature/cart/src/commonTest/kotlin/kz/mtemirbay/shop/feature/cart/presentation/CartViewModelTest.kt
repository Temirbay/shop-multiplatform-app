package kz.mtemirbay.shop.feature.cart.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kz.mtemirbay.shop.core.data.cart.InMemoryCartRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    // Настоящий репозиторий в памяти вместо фейка: он простой и уже покрыт своими тестами
    private val cartRepository = InMemoryCartRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun emptyCartHasNoItemsAndZeroTotal() = runTest {
        val viewModel = createViewModel()

        assertEquals(CartState(), viewModel.state.value)
    }

    @Test
    fun totalsAreCalculatedFromItems() = runTest {
        cartRepository.add(1, "Смартфон", 189_990, "")
        cartRepository.add(1, "Смартфон", 189_990, "")
        cartRepository.add(11, "Книга", 12_990, "")

        val viewModel = createViewModel()

        assertEquals(3, viewModel.state.value.totalQuantity)
        assertEquals(392_970, viewModel.state.value.totalPriceKzt)
    }

    @Test
    fun increaseAndDecreaseChangeQuantity() = runTest {
        cartRepository.add(1, "Смартфон", 189_990, "")
        val viewModel = createViewModel()

        viewModel.onEvent(CartEvent.Increase(1))
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.items.single().quantity)

        viewModel.onEvent(CartEvent.Decrease(1))
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.items.single().quantity)
    }

    @Test
    fun decreaseDoesNotGoBelowOne() = runTest {
        cartRepository.add(1, "Смартфон", 189_990, "")
        val viewModel = createViewModel()

        viewModel.onEvent(CartEvent.Decrease(1))
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.items.single().quantity)
    }

    @Test
    fun removeDeletesItem() = runTest {
        cartRepository.add(1, "Смартфон", 189_990, "")
        val viewModel = createViewModel()

        viewModel.onEvent(CartEvent.Remove(1))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.items.isEmpty())
        assertEquals(0L, viewModel.state.value.totalPriceKzt)
    }

    @Test
    fun checkoutSendsNotAvailableEffect() = runTest {
        val viewModel = createViewModel()

        val effect = async { viewModel.effects.first() }
        viewModel.onEvent(CartEvent.Checkout)
        advanceUntilIdle()

        assertEquals(CartEffect.CheckoutNotAvailable, effect.await())
    }

    private fun TestScope.createViewModel() = CartViewModel(cartRepository).also { advanceUntilIdle() }
}
