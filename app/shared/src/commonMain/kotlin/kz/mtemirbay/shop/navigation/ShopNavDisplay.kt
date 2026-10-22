package kz.mtemirbay.shop.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kz.mtemirbay.shop.feature.cart.presentation.CartScreen
import kz.mtemirbay.shop.feature.catalog.presentation.CatalogScreen
import kz.mtemirbay.shop.feature.checkout.presentation.CheckoutScreen
import kz.mtemirbay.shop.feature.checkout.presentation.OrderReviewScreen
import kz.mtemirbay.shop.feature.checkout.presentation.OrderSuccessScreen
import kz.mtemirbay.shop.feature.product.presentation.ProductScreen

@Composable
fun ShopNavDisplay() {
    // Стек экранов — обычный список: открыть экран = add, вернуться = removeLast
    val backStack = rememberNavBackStack(navSavedStateConfig, CatalogRoute)
    val goBack: () -> Unit = { if (backStack.size > 1) backStack.removeLastOrNull() }
    // Сбросить стек до каталога: с экрана успеха или из пустой корзины «назад» не должно вести в форму/корзину
    val resetTo: (Route) -> Unit = { route ->
        backStack.clear()
        backStack.add(CatalogRoute)
        if (route != CatalogRoute) backStack.add(route)
    }

    NavDisplay(
        backStack = backStack,
        onBack = goBack,
        entryDecorators = listOf(
            // Состояние rememberSaveable (скролл, введённый текст) — своё у каждого экрана в стеке
            rememberSaveableStateHolderNavEntryDecorator(),
            // ViewModel привязана к записи в стеке: у каждого открытого товара своя ProductViewModel,
            // и она очищается, когда экран убирают из стека
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<CatalogRoute> {
                CatalogScreen(
                    onProductClick = { productId -> backStack.add(ProductRoute(productId)) },
                    onCartClick = { backStack.add(CartRoute) },
                )
            }
            entry<ProductRoute> { route ->
                ProductScreen(
                    productId = route.productId,
                    onBack = goBack,
                    onOpenCart = { backStack.add(CartRoute) },
                )
            }
            entry<CartRoute> {
                CartScreen(
                    onBack = goBack,
                    // «В каталог» из пустой корзины: убираем всё поверх каталога, а не кладём второй каталог в стек
                    onGoToCatalog = { resetTo(CatalogRoute) },
                    onCheckout = { backStack.add(OrderReviewRoute) },
                )
            }
            entry<OrderReviewRoute> {
                OrderReviewScreen(onBack = goBack, onContinue = { backStack.add(CheckoutRoute) })
            }
            entry<CheckoutRoute> {
                CheckoutScreen(
                    onBack = goBack,
                    // Стек станет [Каталог, Успех]: «назад» с экрана успеха — в каталог, а не в форму заказа
                    onOrderPlaced = { orderId, totalKzt -> resetTo(OrderSuccessRoute(orderId, totalKzt)) },
                )
            }
            entry<OrderSuccessRoute> { route ->
                OrderSuccessScreen(
                    orderId = route.orderId,
                    totalKzt = route.totalKzt,
                    onGoToCatalog = { resetTo(CatalogRoute) },
                )
            }
        },
    )
}
