package kz.mtemirbay.shop.core.data.cart

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Корзина в памяти: живёт, пока живёт приложение. update {} — атомарно, без гонок между корутинами */
class InMemoryCartRepository : CartRepository {

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    override val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    override suspend fun add(productId: Long, title: String, priceKzt: Long, imageUrl: String) {
        _items.update { items ->
            if (items.any { it.productId == productId }) {
                items.map { if (it.productId == productId) it.copy(quantity = it.quantity + 1) else it }
            } else {
                items + CartItem(productId, title, priceKzt, imageUrl, quantity = 1)
            }
        }
    }

    override suspend fun setQuantity(productId: Long, quantity: Int) {
        if (quantity <= 0) {
            remove(productId)
            return
        }
        _items.update { items ->
            items.map { if (it.productId == productId) it.copy(quantity = quantity) else it }
        }
    }

    override suspend fun remove(productId: Long) {
        _items.update { items -> items.filterNot { it.productId == productId } }
    }

    override suspend fun clear() {
        _items.value = emptyList()
    }
}
