package kz.mtemirbay.shop.data

import kz.mtemirbay.shop.core.model.OrderDetailsDto
import kz.mtemirbay.shop.core.model.OrderItemDto
import kz.mtemirbay.shop.db.OrderItems
import kz.mtemirbay.shop.db.Orders
import kz.mtemirbay.shop.db.Products
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class ExposedOrderRepository(private val database: Database) : OrderRepository {

    override suspend fun create(
        customerName: String,
        phone: String,
        address: String,
        items: List<NewOrderItem>,
    ): CreatedOrder {
        val totalKzt = items.sumOf { it.priceKzt * it.quantity }
        // Одна транзакция: если запись позиций упадёт, откатится и сам заказ — «пустых» заказов в БД не будет
        val orderId = suspendTransaction(database) {
            val orderId = Orders.insertAndGetId {
                it[Orders.customerName] = customerName
                it[Orders.phone] = phone
                it[Orders.address] = address
                it[Orders.totalKzt] = totalKzt
            }
            OrderItems.batchInsert(items) { item ->
                this[OrderItems.orderId] = orderId
                this[OrderItems.productId] = item.productId
                this[OrderItems.quantity] = item.quantity
                this[OrderItems.priceKzt] = item.priceKzt
            }
            orderId.value
        }
        return CreatedOrder(id = orderId, totalKzt = totalKzt, itemCount = items.sumOf { it.quantity })
    }

    override suspend fun get(id: Long): OrderDetailsDto? = suspendTransaction(database) {
        val order = Orders.selectAll().where { Orders.id eq id }.singleOrNull() ?: return@suspendTransaction null
        // JOIN с товарами — ради названия; цена берётся из позиции заказа, а не из товара
        val items = OrderItems.join(Products, JoinType.INNER, OrderItems.productId, Products.id)
            .selectAll()
            .where { OrderItems.orderId eq id }
            .orderBy(OrderItems.id)
            .map { row ->
                OrderItemDto(
                    productId = row[OrderItems.productId].value,
                    title = row[Products.title],
                    quantity = row[OrderItems.quantity],
                    priceKzt = row[OrderItems.priceKzt],
                )
            }
        OrderDetailsDto(
            id = order[Orders.id].value,
            customerName = order[Orders.customerName],
            phone = order[Orders.phone],
            address = order[Orders.address],
            totalKzt = order[Orders.totalKzt],
            items = items,
        )
    }
}
