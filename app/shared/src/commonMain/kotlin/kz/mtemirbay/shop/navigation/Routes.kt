package kz.mtemirbay.shop.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/** Все экраны приложения. Маршруты знает только app:shared — фичи друг о друге не знают */
@Serializable
sealed interface Route : NavKey

@Serializable
data object CatalogRoute : Route

@Serializable
data class ProductRoute(val productId: Long) : Route

/**
 * Стек экранов сохраняется как список NavKey. На Android подтипы находятся сами, а на iOS, Desktop и Web
 * рефлексии нет — каждый маршрут нужно зарегистрировать здесь, иначе сохранение стека упадёт
 */
internal val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(CatalogRoute::class, CatalogRoute.serializer())
            subclass(ProductRoute::class, ProductRoute.serializer())
        }
    }
}
