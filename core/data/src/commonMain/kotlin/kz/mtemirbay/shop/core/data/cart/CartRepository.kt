package kz.mtemirbay.shop.core.data.cart

import kotlinx.coroutines.flow.StateFlow

/**
 * Общая корзина для всех фич. Изменения сразу видны всем подписчикам [items].
 * Методы suspend, хотя реализация в памяти мгновенная: хранилище на диске (следующий этап)
 * будет писать асинхронно, и интерфейс менять не придётся.
 */
interface CartRepository {
    val items: StateFlow<List<CartItem>>

    /** Товар уже в корзине — количество +1, иначе добавляется новая строка */
    suspend fun add(productId: Long, title: String, priceKzt: Long, imageUrl: String)

    /** quantity <= 0 — товар удаляется */
    suspend fun setQuantity(productId: Long, quantity: Int)

    suspend fun remove(productId: Long)

    suspend fun clear()
}
