package kz.mtemirbay.shop.core.data.cart

/** Снимок товара на момент добавления: корзине не нужно ходить на сервер, чтобы показать себя */
data class CartItem(
    val productId: Long,
    val title: String,
    val priceKzt: Long,
    val imageUrl: String,
    val quantity: Int,
)
