package kz.mtemirbay.shop.feature.cart.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kz.mtemirbay.shop.core.data.cart.CartItem
import kz.mtemirbay.shop.core.data.cart.CartRepository
import kz.mtemirbay.shop.core.ui.MviViewModel

internal class CartViewModel(
    private val cartRepository: CartRepository,
) : MviViewModel<CartState, CartEvent, Nothing>(CartState()) {

    init {
        // Итоги вычисляются из items при каждом изменении, а не хранятся отдельно — не могут разойтись со списком
        cartRepository.items
            .onEach { items -> setState { items.toState() } }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: CartEvent) {
        when (event) {
            is CartEvent.Increase -> changeQuantity(event.productId, delta = 1)
            is CartEvent.Decrease -> changeQuantity(event.productId, delta = -1)
            is CartEvent.Remove -> viewModelScope.launch { cartRepository.remove(event.productId) }
        }
    }

    private fun changeQuantity(productId: Long, delta: Int) {
        val current = state.value.items.find { it.productId == productId } ?: return
        val newQuantity = current.quantity + delta
        // Минимум 1: товар не пропадает из списка от лишнего нажатия «−». Кнопка в UI при 1 тоже неактивна,
        // а проверка здесь — чтобы правило держалось, даже если событие придёт не из этой кнопки
        if (newQuantity < MIN_QUANTITY) return
        viewModelScope.launch {
            cartRepository.setQuantity(productId, newQuantity)
        }
    }
}

internal const val MIN_QUANTITY = 1

private fun List<CartItem>.toState() = CartState(
    items = this,
    totalQuantity = sumOf { it.quantity },
    totalPriceKzt = sumOf { it.priceKzt * it.quantity },
)
