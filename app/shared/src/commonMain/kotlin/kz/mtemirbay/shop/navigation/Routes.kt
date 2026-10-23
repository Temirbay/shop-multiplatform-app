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

@Serializable
data object CartRoute : Route

@Serializable
data object AccountRoute : Route

@Serializable
data object MyOrdersRoute : Route

@Serializable
data class OrderDetailsRoute(val orderId: Long) : Route

/** Шаг 1 оформления: чек */
@Serializable
data object OrderReviewRoute : Route

/** Шаг 2 оформления: данные доставки */
@Serializable
data object CheckoutRoute : Route

@Serializable
data class OrderSuccessRoute(val orderId: Long, val totalKzt: Long) : Route

/**
 * Стек экранов сохраняется как список NavKey. На Android подтипы находятся сами, а на iOS, Desktop и Web
 * рефлексии нет — каждый маршрут нужно зарегистрировать здесь, иначе сохранение стека упадёт
 */
internal val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(CatalogRoute::class, CatalogRoute.serializer())
            subclass(ProductRoute::class, ProductRoute.serializer())
            subclass(CartRoute::class, CartRoute.serializer())
            subclass(AccountRoute::class, AccountRoute.serializer())
            subclass(MyOrdersRoute::class, MyOrdersRoute.serializer())
            subclass(OrderDetailsRoute::class, OrderDetailsRoute.serializer())
            subclass(OrderReviewRoute::class, OrderReviewRoute.serializer())
            subclass(CheckoutRoute::class, CheckoutRoute.serializer())
            subclass(OrderSuccessRoute::class, OrderSuccessRoute.serializer())
        }
    }
}
