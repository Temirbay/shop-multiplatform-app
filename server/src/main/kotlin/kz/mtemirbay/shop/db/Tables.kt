package kz.mtemirbay.shop.db

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object Categories : LongIdTable("categories") {
    val title = varchar("title", length = 100)
}

object Products : LongIdTable("products") {
    val title = varchar("title", length = 200)
    val description = text("description")
    val priceKzt = long("price_kzt")
    val imageUrl = varchar("image_url", length = 500)
    val categoryId = reference("category_id", Categories)
}

object Users : LongIdTable("users") {
    val name = varchar("name", length = 100)
    // uniqueIndex: два аккаунта на один номер БД не пропустит, даже если два запроса придут одновременно
    val phone = varchar("phone", length = 20).uniqueIndex()
    /** bcrypt-хэш. Сам пароль нигде не хранится */
    val passwordHash = varchar("password_hash", length = 100)
    val createdAt = long("created_at")
}

object Orders : LongIdTable("orders") {
    /** null — заказ гостя */
    val userId = optReference("user_id", Users)
    val customerName = varchar("customer_name", length = 100)
    val phone = varchar("phone", length = 20)
    val address = text("address")
    val totalKzt = long("total_kzt")
    /** Время создания, миллисекунды Unix — для сортировки «Моих заказов» */
    val createdAt = long("created_at")
}

object OrderItems : LongIdTable("order_items") {
    // CASCADE: удалили заказ — удалились и его позиции
    val orderId = reference("order_id", Orders, onDelete = ReferenceOption.CASCADE)
    val productId = reference("product_id", Products)
    val quantity = integer("quantity")
    /** Снимок цены на момент заказа: цена товара потом может измениться, а заказ — нет */
    val priceKzt = long("price_kzt")
}
