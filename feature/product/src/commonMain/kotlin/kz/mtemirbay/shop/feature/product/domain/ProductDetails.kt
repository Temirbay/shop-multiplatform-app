package kz.mtemirbay.shop.feature.product.domain

internal data class ProductDetails(
    val id: Long,
    val title: String,
    val description: String,
    val priceKzt: Long,
    val imageUrl: String,
)
