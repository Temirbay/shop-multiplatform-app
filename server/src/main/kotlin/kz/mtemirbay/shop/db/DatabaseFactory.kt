package kz.mtemirbay.shop.db

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {

    /**
     * Подключается к БД, создаёт недостающие таблицы и заполняет пустую БД начальными данными.
     * SchemaUtils.create подходит для учебного проекта; в продакшене схему меняют миграциями (Flyway, Liquibase)
     */
    fun init(url: String): Database {
        val database = Database.connect(url = url, driver = "org.h2.Driver")
        transaction(database) {
            SchemaUtils.create(Categories, Products, Users, Orders, OrderItems)
            if (Products.selectAll().empty()) seed()
        }
        return database
    }

    private fun seed() {
        Categories.batchInsert(SeedData.categories) { category ->
            this[Categories.id] = category.id
            this[Categories.title] = category.title
        }
        Products.batchInsert(SeedData.products) { product ->
            this[Products.id] = product.id
            this[Products.title] = product.title
            this[Products.description] = product.description
            this[Products.priceKzt] = product.priceKzt
            this[Products.imageUrl] = product.imageUrl
            this[Products.categoryId] = product.categoryId
        }
    }
}
