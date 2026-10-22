package kz.mtemirbay.shop.data

import kz.mtemirbay.shop.core.model.OrderItemRequest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/** Заказы в памяти: пропадают при перезапуске сервера. БД — на следующем этапе */
class InMemoryOrderRepository : OrderRepository {

    // Сервер обрабатывает запросы параллельно — счётчик и хранилище потокобезопасные
    private val nextId = AtomicLong(FIRST_ORDER_ID)
    private val orders = ConcurrentHashMap<Long, Order>()

    override suspend fun create(
        customerName: String,
        phone: String,
        address: String,
        items: List<OrderItemRequest>,
        totalKzt: Long,
    ): Order {
        val order = Order(nextId.getAndIncrement(), customerName, phone, address, items, totalKzt)
        orders[order.id] = order
        return order
    }

    private companion object {
        const val FIRST_ORDER_ID = 1001L
    }
}
