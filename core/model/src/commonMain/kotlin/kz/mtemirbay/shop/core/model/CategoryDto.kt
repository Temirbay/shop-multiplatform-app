package kz.mtemirbay.shop.core.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: Long,
    val title: String,
)
