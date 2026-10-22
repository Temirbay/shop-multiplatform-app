package kz.mtemirbay.shop.feature.catalog.data

import kz.mtemirbay.shop.core.model.CategoryDto
import kz.mtemirbay.shop.core.model.ProductDto
import kz.mtemirbay.shop.feature.catalog.domain.Category
import kz.mtemirbay.shop.feature.catalog.domain.Product

internal fun ProductDto.toDomain() = Product(
    id = id,
    title = title,
    priceKzt = priceKzt,
    imageUrl = imageUrl,
)

internal fun CategoryDto.toDomain() = Category(
    id = id,
    title = title,
)
