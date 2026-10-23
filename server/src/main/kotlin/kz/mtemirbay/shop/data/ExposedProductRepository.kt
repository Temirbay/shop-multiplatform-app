package kz.mtemirbay.shop.data

import kz.mtemirbay.shop.core.model.CategoryDto
import kz.mtemirbay.shop.core.model.ProductDto
import kz.mtemirbay.shop.db.Categories
import kz.mtemirbay.shop.db.Products
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class ExposedProductRepository(private val database: Database) : ProductRepository {

    override suspend fun getCategories(): List<CategoryDto> = suspendTransaction(database) {
        Categories.selectAll()
            .orderBy(Categories.id)
            .map { CategoryDto(id = it[Categories.id].value, title = it[Categories.title]) }
    }

    override suspend fun getProducts(categoryId: Long?, query: String?): List<ProductDto> = suspendTransaction(database) {
        val conditions = listOfNotNull(
            categoryId?.let { Products.categoryId eq it },
            // Поиск без учёта регистра: «kotlin» найдёт «Kotlin in Action»
            query?.takeIf { it.isNotBlank() }?.let { Products.title.lowerCase() like "%${it.lowercase()}%" },
        )
        Products.selectAll()
            .apply { if (conditions.isNotEmpty()) where { conditions.reduce { acc, op -> acc and op } } }
            .orderBy(Products.id)
            .map { it.toProductDto() }
    }

    override suspend fun getProduct(id: Long): ProductDto? = suspendTransaction(database) {
        Products.selectAll()
            .where { Products.id eq id }
            .singleOrNull()
            ?.toProductDto()
    }
}

private fun ResultRow.toProductDto() = ProductDto(
    id = this[Products.id].value,
    title = this[Products.title],
    description = this[Products.description],
    priceKzt = this[Products.priceKzt],
    imageUrl = this[Products.imageUrl],
    categoryId = this[Products.categoryId].value,
)
