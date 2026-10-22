package kz.mtemirbay.shop.feature.product.presentation

import kz.mtemirbay.shop.feature.product.domain.ProductDetails

internal sealed interface ProductState {
    data object Loading : ProductState
    data class Content(val product: ProductDetails) : ProductState
    /** 404: кнопку «Повторить» не показываем — товара нет */
    data object NotFound : ProductState
    data object Error : ProductState
}

internal sealed interface ProductEvent {
    data object Retry : ProductEvent
    data object AddToCart : ProductEvent
}

internal sealed interface ProductEffect {
    data object AddedToCart : ProductEffect
}
