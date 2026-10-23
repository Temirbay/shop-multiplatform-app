package kz.mtemirbay.shop.feature.orders.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kz.mtemirbay.shop.feature.orders.domain.OrderDetails
import kz.mtemirbay.shop.feature.orders.domain.OrderNotFoundException
import kz.mtemirbay.shop.feature.orders.domain.OrderSummary
import kz.mtemirbay.shop.feature.orders.domain.OrdersRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OrdersViewModelsTest {

    private val repository = FakeOrdersRepository()
    private val summary = OrderSummary(3, 459_970, 3, 1)
    private val details = OrderDetails(3, "Мирас", "+77011234567", "Алматы", 459_970, 1, emptyList())

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun myOrdersLoad() = runTest {
        repository.orders = Result.success(listOf(summary))
        val viewModel = MyOrdersViewModel(repository)

        advanceUntilIdle()

        assertEquals(MyOrdersState.Content(listOf(summary)), viewModel.state.value)
    }

    @Test
    fun myOrdersErrorThenRetry() = runTest {
        repository.orders = Result.failure(RuntimeException("Network"))
        val viewModel = MyOrdersViewModel(repository)
        advanceUntilIdle()
        assertEquals(MyOrdersState.Error, viewModel.state.value)

        repository.orders = Result.success(emptyList())
        viewModel.onEvent(MyOrdersEvent.Retry)
        advanceUntilIdle()

        assertEquals(MyOrdersState.Content(emptyList()), viewModel.state.value)
    }

    @Test
    fun orderDetailsLoad() = runTest {
        repository.order = Result.success(details)
        val viewModel = OrderDetailsViewModel(orderId = 3, repository = repository)

        advanceUntilIdle()

        assertEquals(OrderDetailsState.Content(details), viewModel.state.value)
        assertEquals(listOf(3L), repository.requestedOrderIds)
    }

    @Test
    fun foreignOrNonexistentOrderShowsNotFound() = runTest {
        repository.order = Result.failure(OrderNotFoundException(999))
        val viewModel = OrderDetailsViewModel(orderId = 999, repository = repository)

        advanceUntilIdle()

        assertEquals(OrderDetailsState.NotFound, viewModel.state.value)
    }

    private class FakeOrdersRepository : OrdersRepository {
        var orders: Result<List<OrderSummary>> = Result.success(emptyList())
        var order: Result<OrderDetails> = Result.failure(IllegalStateException("Not set"))
        val requestedOrderIds = mutableListOf<Long>()

        override suspend fun getMyOrders() = orders

        override suspend fun getOrder(id: Long): Result<OrderDetails> {
            requestedOrderIds += id
            return order
        }
    }
}
