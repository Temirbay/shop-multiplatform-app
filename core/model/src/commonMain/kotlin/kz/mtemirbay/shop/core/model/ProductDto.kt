package kz.mtemirbay.shop.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Long,
    val title: String,
    val description: String,
    /** Цена в тенге. Деньги храним целым числом, не Double: 0.1 + 0.2 != 0.3 */
    val priceKzt: Long,
    val imageUrl: String,
    val categoryId: Long,
)
