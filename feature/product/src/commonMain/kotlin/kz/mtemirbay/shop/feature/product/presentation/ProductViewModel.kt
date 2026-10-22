package kz.mtemirbay.shop.feature.product.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kz.mtemirbay.shop.core.ui.MviViewModel
import kz.mtemirbay.shop.feature.product.domain.ProductNotFoundException
import kz.mtemirbay.shop.feature.product.domain.ProductRepository

internal class ProductViewModel(
    private val productId: Long,
    private val repository: ProductRepository,
) : MviViewModel<ProductState, ProductEvent, ProductEffect>(ProductState.Loading) {

    init {
        load()
    }

    override fun onEvent(event: ProductEvent) {
        when (event) {
            ProductEvent.Retry -> load()
            // Настоящая корзина — на следующем этапе
            ProductEvent.AddToCart -> sendEffect(ProductEffect.AddedToCart)
        }
    }

    private fun load() {
        viewModelScope.launch {
            setState { ProductState.Loading }
            val result = repository.getProduct(productId)
            setState {
                result.fold(
                    onSuccess = { ProductState.Content(it) },
                    onFailure = { if (it is ProductNotFoundException) ProductState.NotFound else ProductState.Error },
                )
            }
        }
    }
}
