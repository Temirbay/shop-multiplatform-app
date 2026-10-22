package kz.mtemirbay.shop.core.data.cart

import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Корзина как JSON-строка под одним ключом в Settings (SharedPreferences, NSUserDefaults, localStorage...) */
class SettingsCartStorage(private val settings: Settings) : CartStorage {

    private val json = Json { ignoreUnknownKeys = true }

    override fun load(): List<CartItem> {
        val stored = settings.getStringOrNull(KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<StoredCartItem>>(stored).map { it.toCartItem() }
        } catch (e: SerializationException) {
            // Данные испорчены или записаны несовместимой версией — лучше пустая корзина, чем падение на старте
            emptyList()
        } catch (e: IllegalArgumentException) {
            emptyList()
        }
    }

    override fun save(items: List<CartItem>) {
        settings.putString(KEY, json.encodeToString(items.map { it.toStored() }))
    }

    private companion object {
        // v1 в ключе: при несовместимой смене формата заводим cart_v2, старые данные не ломают чтение
        const val KEY = "cart_v1"
    }
}

/**
 * Формат на диске — контракт с прошлыми версиями приложения, поэтому отдельная модель:
 * переименование поля в CartItem не сломает уже сохранённые корзины
 */
@Serializable
private data class StoredCartItem(
    val productId: Long,
    val title: String,
    val priceKzt: Long,
    val imageUrl: String,
    val quantity: Int,
)

private fun StoredCartItem.toCartItem() = CartItem(productId, title, priceKzt, imageUrl, quantity)

private fun CartItem.toStored() = StoredCartItem(productId, title, priceKzt, imageUrl, quantity)
