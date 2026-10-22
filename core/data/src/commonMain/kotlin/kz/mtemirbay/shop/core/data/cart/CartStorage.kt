package kz.mtemirbay.shop.core.data.cart

/** Где лежит корзина между запусками приложения */
interface CartStorage {
    fun load(): List<CartItem>
    fun save(items: List<CartItem>)
}

/** Ничего не сохраняет между запусками — для тестов */
class InMemoryCartStorage(initial: List<CartItem> = emptyList()) : CartStorage {
    private var items = initial

    override fun load(): List<CartItem> = items

    override fun save(items: List<CartItem>) {
        this.items = items
    }
}
