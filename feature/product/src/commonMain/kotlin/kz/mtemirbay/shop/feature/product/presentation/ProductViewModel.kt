package kz.mtemirbay.shop.feature.product.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kz.mtemirbay.shop.core.data.cart.CartRepository
import kz.mtemirbay.shop.core.ui.MviViewModel
import kz.mtemirbay.shop.feature.product.domain.ProductNotFoundException
import kz.mtemirbay.shop.feature.product.domain.ProductRepository

internal class ProductViewModel(
    private val productId: Long,
    private val repository: ProductRepository,
    private val cartRepository: CartRepository,
) : MviViewModel<ProductState, ProductEvent, ProductEffect>(ProductState.Loading) {

    init {
        load()
    }

    override fun onEvent(event: ProductEvent) {
        when (event) {
            ProductEvent.Retry -> load()
            ProductEvent.AddToCart -> addToCart()
        }
    }

    private fun addToCart() {
        val product = (state.value as? ProductState.Content)?.product ?: return
        viewModelScope.launch {
            cartRepository.add(product.id, product.title, product.priceKzt, product.imageUrl)
            sendEffect(ProductEffect.AddedToCart)
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
