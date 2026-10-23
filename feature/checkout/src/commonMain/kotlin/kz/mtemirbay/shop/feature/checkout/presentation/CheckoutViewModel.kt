package kz.mtemirbay.shop.feature.checkout.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kz.mtemirbay.shop.core.data.cart.CartRepository
import kz.mtemirbay.shop.core.data.session.SessionRepository
import kz.mtemirbay.shop.core.model.OrderField
import kz.mtemirbay.shop.core.model.OrderValidation
import kz.mtemirbay.shop.core.model.OrderValidationError
import kz.mtemirbay.shop.core.ui.COUNTRY_CODE
import kz.mtemirbay.shop.core.ui.MviViewModel
import kz.mtemirbay.shop.core.ui.phoneDigitsFromInput
import kz.mtemirbay.shop.feature.checkout.domain.CheckoutRepository
import kz.mtemirbay.shop.feature.checkout.domain.OrderRejectedException

internal class CheckoutViewModel(
    private val checkoutRepository: CheckoutRepository,
    private val cartRepository: CartRepository,
    sessionRepository: SessionRepository,
) : MviViewModel<CheckoutState, CheckoutEvent, CheckoutEffect>(CheckoutState()) {

    /** Поля, ошибки которых уже показываем. Пока пользователь не дошёл до поля, не ругаемся на пустое */
    private var visibleErrorFields = emptySet<OrderField>()

    /** Ошибка от сервера: держится, пока пользователь не изменит это поле */
    private var serverError: OrderValidationError? = null

    init {
        // Вошёл — имя и телефон берём из профиля. Один раз, при открытии формы: дальше поля в руках пользователя,
        // например, чтобы оформить заказ на другого человека
        sessionRepository.session.value?.let { session ->
            setState { copy(name = session.name, phone = session.phone.removePrefix(COUNTRY_CODE)) }
        }
        cartRepository.items
            .onEach { items ->
                setState {
                    copy(
                        items = items,
                        itemCount = items.sumOf { it.quantity },
                        totalKzt = items.sumOf { it.priceKzt * it.quantity },
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: CheckoutEvent) {
        when (event) {
            is CheckoutEvent.NameChanged -> onFieldChanged(OrderField.NAME) { copy(name = event.value) }
            is CheckoutEvent.PhoneChanged -> onFieldChanged(OrderField.PHONE) {
                copy(phone = phoneDigitsFromInput(event.value, previousDigits = phone))
            }
            is CheckoutEvent.AddressChanged -> onFieldChanged(OrderField.ADDRESS) { copy(address = event.value) }
            is CheckoutEvent.FieldFocusLost -> {
                visibleErrorFields = visibleErrorFields + event.field
                refreshErrors()
            }
            CheckoutEvent.Submit -> submit()
        }
    }

    private fun onFieldChanged(field: OrderField, change: CheckoutState.() -> CheckoutState) {
        if (serverError?.field == field) serverError = null
        setState(change)
        refreshErrors()
    }

    private fun submit() {
        // Повторное нажатие во время отправки — второй заказ не создаётся
        if (state.value.isSubmitting) return

        visibleErrorFields = setOf(OrderField.NAME, OrderField.PHONE, OrderField.ADDRESS)
        refreshErrors()
        val items = cartRepository.items.value
        if (localErrors(state.value).isNotEmpty() || items.isEmpty()) return

        setState { copy(isSubmitting = true) }
        viewModelScope.launch {
            val current = state.value
            val result = checkoutRepository.placeOrder(current.name, COUNTRY_CODE + current.phone, current.address, items)
            setState { copy(isSubmitting = false) }
            result
                .onSuccess { order ->
                    cartRepository.clear()
                    sendEffect(CheckoutEffect.OrderPlaced(order.id, order.totalKzt))
                }
                .onFailure { error -> handleFailure(error) }
        }
    }

    private fun handleFailure(error: Throwable) {
        if (error !is OrderRejectedException) {
            sendEffect(CheckoutEffect.NetworkError)
            return
        }
        val validationError = OrderValidationError.entries.find { it.name == error.code }
        if (validationError != null && validationError.field != OrderField.ITEMS) {
            // Сервер нашёл ошибку в поле, которую не поймала проверка на клиенте (например, старая версия приложения)
            serverError = validationError
            refreshErrors()
        } else {
            sendEffect(CheckoutEffect.ProductUnavailable)
        }
    }

    private fun refreshErrors() {
        val visible = localErrors(state.value).filterKeys { it in visibleErrorFields }
        val withServer = serverError?.let { visible + (it.field to it) } ?: visible
        setState { copy(errors = withServer) }
    }

    private fun localErrors(state: CheckoutState): Map<OrderField, OrderValidationError> = listOfNotNull(
        OrderValidation.validateName(state.name),
        OrderValidation.validatePhone(COUNTRY_CODE + state.phone),
        OrderValidation.validateAddress(state.address),
    ).associateBy { it.field }
}
