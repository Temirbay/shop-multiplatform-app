package kz.mtemirbay.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kz.mtemirbay.shop.core.data.cart.CartRepository

/** Состояние «оболочки» приложения — того, что видно поверх всех экранов: бейдж корзины на нижней панели */
class AppViewModel(cartRepository: CartRepository) : ViewModel() {

    val cartItemCount: StateFlow<Int> = cartRepository.items
        .map { items -> items.sumOf { it.quantity } }
        // Eagerly: панель видна всё время, подписчик есть почти всегда
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = cartRepository.items.value.sumOf { it.quantity })
}
