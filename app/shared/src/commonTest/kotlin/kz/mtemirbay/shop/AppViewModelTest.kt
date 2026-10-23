package kz.mtemirbay.shop

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kz.mtemirbay.shop.core.data.cart.CartRepositoryImpl
import kz.mtemirbay.shop.core.data.cart.InMemoryCartStorage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    private val cartRepository = CartRepositoryImpl(InMemoryCartStorage())

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun cartBadgeFollowsCartFromAnyScreen() = runTest {
        val viewModel = AppViewModel(cartRepository)
        advanceUntilIdle()
        assertEquals(0, viewModel.cartItemCount.value)

        // Как будто товар добавили на экране товара — панель узнаёт об этом через общий StateFlow
        cartRepository.add(1, "Смартфон", 189_990, "")
        cartRepository.add(1, "Смартфон", 189_990, "")
        advanceUntilIdle()

        assertEquals(2, viewModel.cartItemCount.value)
    }
}
