package kz.mtemirbay.shop.feature.orders.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kz.mtemirbay.shop.core.ui.MviViewModel
import kz.mtemirbay.shop.feature.orders.domain.OrderSummary
import kz.mtemirbay.shop.feature.orders.domain.OrdersRepository

internal sealed interface MyOrdersState {
    data object Loading : MyOrdersState
    data object Error : MyOrdersState
    data class Content(val orders: List<OrderSummary>) : MyOrdersState
}

internal sealed interface MyOrdersEvent {
    data object Retry : MyOrdersEvent
}

internal class MyOrdersViewModel(
    private val repository: OrdersRepository,
) : MviViewModel<MyOrdersState, MyOrdersEvent, Nothing>(MyOrdersState.Loading) {

    init {
        load()
    }

    override fun onEvent(event: MyOrdersEvent) {
        when (event) {
            MyOrdersEvent.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            setState { MyOrdersState.Loading }
            val result = repository.getMyOrders()
            setState { result.fold(onSuccess = { MyOrdersState.Content(it) }, onFailure = { MyOrdersState.Error }) }
        }
    }
}
