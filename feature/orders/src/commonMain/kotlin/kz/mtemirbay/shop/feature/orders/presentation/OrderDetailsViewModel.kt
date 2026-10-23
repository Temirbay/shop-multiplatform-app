package kz.mtemirbay.shop.feature.orders.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kz.mtemirbay.shop.core.ui.MviViewModel
import kz.mtemirbay.shop.feature.orders.domain.OrderDetails
import kz.mtemirbay.shop.feature.orders.domain.OrderNotFoundException
import kz.mtemirbay.shop.feature.orders.domain.OrdersRepository

internal sealed interface OrderDetailsState {
    data object Loading : OrderDetailsState
    data object NotFound : OrderDetailsState
    data object Error : OrderDetailsState
    data class Content(val order: OrderDetails) : OrderDetailsState
}

internal sealed interface OrderDetailsEvent {
    data object Retry : OrderDetailsEvent
}

internal class OrderDetailsViewModel(
    private val orderId: Long,
    private val repository: OrdersRepository,
) : MviViewModel<OrderDetailsState, OrderDetailsEvent, Nothing>(OrderDetailsState.Loading) {

    init {
        load()
    }

    override fun onEvent(event: OrderDetailsEvent) {
        when (event) {
            OrderDetailsEvent.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            setState { OrderDetailsState.Loading }
            val result = repository.getOrder(orderId)
            setState {
                result.fold(
                    onSuccess = { OrderDetailsState.Content(it) },
                    onFailure = { if (it is OrderNotFoundException) OrderDetailsState.NotFound else OrderDetailsState.Error },
                )
            }
        }
    }
}
