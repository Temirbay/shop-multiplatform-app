package kz.mtemirbay.shop.feature.checkout.presentation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kz.mtemirbay.shop.core.data.cart.CartItem
import kz.mtemirbay.shop.core.data.cart.CartRepositoryImpl
import kz.mtemirbay.shop.core.data.cart.InMemoryCartStorage
import kz.mtemirbay.shop.core.data.session.Session
import kz.mtemirbay.shop.core.data.session.SessionRepository
import kz.mtemirbay.shop.core.model.OrderField
import kz.mtemirbay.shop.core.model.OrderValidationError
import kz.mtemirbay.shop.feature.checkout.domain.CheckoutRepository
import kz.mtemirbay.shop.feature.checkout.domain.OrderRejectedException
import kz.mtemirbay.shop.feature.checkout.domain.PlacedOrder
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {

    private val cartRepository = CartRepositoryImpl(
        InMemoryCartStorage(listOf(CartItem(1, "Смартфон", 189_990, "", quantity = 2))),
    )
    private val checkoutRepository = FakeCheckoutRepository()
    private val sessionRepository = FakeSessionRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun showsReceiptFromCart() = runTest {
        val viewModel = createViewModel()

        assertEquals(listOf(CartItem(1, "Смартфон", 189_990, "", quantity = 2)), viewModel.state.value.items)
        assertEquals(2, viewModel.state.value.itemCount)
        assertEquals(379_980, viewModel.state.value.totalKzt)
    }

    @Test
    fun guestFormStartsEmpty() = runTest {
        val viewModel = createViewModel()

        assertEquals("", viewModel.state.value.name)
        assertEquals("", viewModel.state.value.phone)
    }

    @Test
    fun loggedInUserGetsNameAndPhonePrefilled() = runTest {
        sessionRepository.save(Session(token = "jwt", userId = 1, name = "Мирас", phone = "+77011234567"))

        val viewModel = createViewModel()

        assertEquals("Мирас", viewModel.state.value.name)
        // В форме — только 10 цифр, +7 рисует префикс поля
        assertEquals("7011234567", viewModel.state.value.phone)
        assertTrue(viewModel.state.value.errors.isEmpty())
    }

    @Test
    fun noErrorsUntilFieldLosesFocusOrSubmit() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(CheckoutEvent.PhoneChanged("+7 701"))
        assertTrue(viewModel.state.value.errors.isEmpty())

        viewModel.onEvent(CheckoutEvent.FieldFocusLost(OrderField.PHONE))
        assertEquals(mapOf(OrderField.PHONE to OrderValidationError.PHONE_INVALID), viewModel.state.value.errors)
    }

    @Test
    fun submitWithInvalidFormShowsAllErrorsAndSendsNothing() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(CheckoutEvent.Submit)
        advanceUntilIdle()

        assertEquals(setOf(OrderField.NAME, OrderField.PHONE, OrderField.ADDRESS), viewModel.state.value.errors.keys)
        assertEquals(0, checkoutRepository.calls)
    }

    @Test
    fun successfulOrderClearsCartAndOpensSuccess() = runTest {
        val viewModel = createViewModel().fillValidForm()

        val effect = async { viewModel.effects.first() }
        viewModel.onEvent(CheckoutEvent.Submit)
        advanceUntilIdle()

        assertEquals(CheckoutEffect.OrderPlaced(orderId = 1042, totalKzt = 379_980), effect.await())
        assertTrue(cartRepository.items.value.isEmpty())
        assertFalse(viewModel.state.value.isSubmitting)
    }

    @Test
    fun doubleSubmitCreatesOneOrder() = runTest {
        val response = CompletableDeferred<Result<PlacedOrder>>()
        checkoutRepository.response = { response.await() }
        val viewModel = createViewModel().fillValidForm()

        viewModel.onEvent(CheckoutEvent.Submit)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isSubmitting)
        viewModel.onEvent(CheckoutEvent.Submit)
        response.complete(Result.success(PlacedOrder(1042, 379_980)))
        advanceUntilIdle()

        assertEquals(1, checkoutRepository.calls)
    }

    @Test
    fun serverFieldErrorIsShownUnderFieldUntilEdited() = runTest {
        checkoutRepository.response = { Result.failure(OrderRejectedException(OrderValidationError.PHONE_INVALID.name)) }
        val viewModel = createViewModel().fillValidForm()

        viewModel.onEvent(CheckoutEvent.Submit)
        advanceUntilIdle()
        assertEquals(OrderValidationError.PHONE_INVALID, viewModel.state.value.errors[OrderField.PHONE])
        assertEquals(2, cartRepository.items.value.single().quantity)

        viewModel.onEvent(CheckoutEvent.PhoneChanged("+77011234568"))
        assertTrue(viewModel.state.value.errors.isEmpty())
    }

    @Test
    fun networkErrorSendsEffectAndKeepsCart() = runTest {
        checkoutRepository.response = { Result.failure(RuntimeException("Network")) }
        val viewModel = createViewModel().fillValidForm()

        val effect = async { viewModel.effects.first() }
        viewModel.onEvent(CheckoutEvent.Submit)
        advanceUntilIdle()

        assertEquals(CheckoutEffect.NetworkError, effect.await())
        assertEquals(1, cartRepository.items.value.size)
    }

    @Test
    fun unknownProductSendsProductUnavailable() = runTest {
        checkoutRepository.response = { Result.failure(OrderRejectedException("PRODUCT_NOT_FOUND")) }
        val viewModel = createViewModel().fillValidForm()

        val effect = async { viewModel.effects.first() }
        viewModel.onEvent(CheckoutEvent.Submit)
        advanceUntilIdle()

        assertEquals(CheckoutEffect.ProductUnavailable, effect.await())
    }

    private fun TestScope.createViewModel() =
        CheckoutViewModel(checkoutRepository, cartRepository, sessionRepository).also { advanceUntilIdle() }

    private fun CheckoutViewModel.fillValidForm() = apply {
        onEvent(CheckoutEvent.NameChanged("Мирас"))
        onEvent(CheckoutEvent.PhoneChanged("+7 701 123 45 67"))
        onEvent(CheckoutEvent.AddressChanged("Алматы, Абая 1"))
    }

    private class FakeCheckoutRepository : CheckoutRepository {
        var calls = 0
        var response: suspend () -> Result<PlacedOrder> = { Result.success(PlacedOrder(1042, 379_980)) }

        override suspend fun placeOrder(
            name: String,
            phone: String,
            address: String,
            items: List<CartItem>,
        ): Result<PlacedOrder> {
            calls++
            return response()
        }
    }

    private class FakeSessionRepository : SessionRepository {
        private val _session = MutableStateFlow<Session?>(null)
        override val session: StateFlow<Session?> = _session

        override suspend fun save(session: Session) {
            _session.value = session
        }

        override suspend fun clear() {
            _session.value = null
        }
    }
}
