package kz.mtemirbay.shop.feature.catalog.domain

internal data class Product(
    val id: Long,
    val title: String,
    val priceKzt: Long,
    val imageUrl: String,
)
