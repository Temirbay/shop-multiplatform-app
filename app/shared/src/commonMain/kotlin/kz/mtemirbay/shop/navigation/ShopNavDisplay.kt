package kz.mtemirbay.shop.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kz.mtemirbay.shop.feature.account.presentation.AccountScreen
import kz.mtemirbay.shop.feature.cart.presentation.CartScreen
import kz.mtemirbay.shop.feature.catalog.presentation.CatalogScreen
import kz.mtemirbay.shop.feature.checkout.presentation.CheckoutScreen
import kz.mtemirbay.shop.feature.checkout.presentation.OrderReviewScreen
import kz.mtemirbay.shop.feature.checkout.presentation.OrderSuccessScreen
import kz.mtemirbay.shop.feature.orders.presentation.MyOrdersScreen
import kz.mtemirbay.shop.feature.orders.presentation.OrderDetailsScreen
import kz.mtemirbay.shop.feature.product.presentation.ProductScreen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Три вкладки, у каждой свой стек: открыл товар в каталоге, сходил в корзину, вернулся — товар на месте.
 * На экране — стек каталога + стек текущей вкладки. Поэтому «назад» с корня корзины/профиля ведёт в каталог,
 * а с корня каталога закрывает приложение — как принято в Android
 */
@Composable
fun ShopNavDisplay(cartItemCount: Int) {
    val catalogStack = rememberNavBackStack(navSavedStateConfig, CatalogRoute)
    val cartStack = rememberNavBackStack(navSavedStateConfig, CartRoute)
    val accountStack = rememberNavBackStack(navSavedStateConfig, AccountRoute)
    val stackOf: (TopLevelTab) -> NavBackStack<NavKey> = { tab ->
        when (tab) {
            TopLevelTab.CATALOG -> catalogStack
            TopLevelTab.CART -> cartStack
            TopLevelTab.ACCOUNT -> accountStack
        }
    }
    var currentTab by rememberSaveable { mutableStateOf(TopLevelTab.CATALOG) }
    val currentStack = stackOf(currentTab)
    val visibleStack: List<NavKey> = if (currentTab == TopLevelTab.CATALOG) catalogStack else catalogStack + currentStack

    val goBack: () -> Unit = {
        if (currentStack.size > 1) {
            currentStack.removeLastOrNull()
        } else if (currentTab != TopLevelTab.CATALOG) {
            currentTab = TopLevelTab.CATALOG
        }
    }
    val openTab: (TopLevelTab) -> Unit = { tab ->
        // Повторное нажатие на активную вкладку — к её первому экрану
        if (tab == currentTab) stackOf(tab).resetTo(tab.root) else currentTab = tab
    }

    // Во время оформления заказа панели нет: меньше отвлекает и не занимает место под клавиатурой
    val showBottomBar = visibleStack.last() !is OrderReviewRoute &&
        visibleStack.last() !is CheckoutRoute &&
        visibleStack.last() !is OrderSuccessRoute

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                ShopBottomBar(currentTab = currentTab, cartItemCount = cartItemCount, onTabClick = openTab)
            }
        },
        // Отступы под статус-бар и т.п. экраны делают сами; Scaffold отвечает только за место под панелью
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        NavDisplay(
            backStack = visibleStack,
            onBack = goBack,
            // consumeWindowInsets: системную навигацию внизу уже закрывает панель — экранам не нужно отступать ещё раз
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            entryDecorators = listOf(
                // Состояние rememberSaveable (скролл, введённый текст) — своё у каждого экрана в стеке
                rememberSaveableStateHolderNavEntryDecorator(),
                // ViewModel привязана к записи в стеке: у каждого открытого товара своя ProductViewModel
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<CatalogRoute> {
                    CatalogScreen(onProductClick = { productId -> catalogStack.add(ProductRoute(productId)) })
                }
                entry<ProductRoute> { route ->
                    ProductScreen(
                        productId = route.productId,
                        onBack = goBack,
                        onOpenCart = { currentTab = TopLevelTab.CART },
                    )
                }
                entry<CartRoute> {
                    CartScreen(
                        onGoToCatalog = { currentTab = TopLevelTab.CATALOG },
                        onCheckout = { cartStack.add(OrderReviewRoute) },
                    )
                }
                entry<OrderReviewRoute> {
                    OrderReviewScreen(onBack = goBack, onContinue = { cartStack.add(CheckoutRoute) })
                }
                entry<CheckoutRoute> {
                    CheckoutScreen(
                        onBack = goBack,
                        // Стек корзины станет [Корзина, Успех]: «назад» с экрана успеха не вернёт в форму заказа
                        onOrderPlaced = { orderId, totalKzt -> cartStack.resetTo(CartRoute, OrderSuccessRoute(orderId, totalKzt)) },
                    )
                }
                entry<OrderSuccessRoute> { route ->
                    OrderSuccessScreen(
                        orderId = route.orderId,
                        totalKzt = route.totalKzt,
                        onGoToCatalog = {
                            cartStack.resetTo(CartRoute)
                            currentTab = TopLevelTab.CATALOG
                        },
                    )
                }
                entry<AccountRoute> {
                    AccountScreen(onMyOrdersClick = { accountStack.add(MyOrdersRoute) })
                }
                entry<MyOrdersRoute> {
                    MyOrdersScreen(onBack = goBack, onOrderClick = { orderId -> accountStack.add(OrderDetailsRoute(orderId)) })
                }
                entry<OrderDetailsRoute> { route ->
                    OrderDetailsScreen(orderId = route.orderId, onBack = goBack)
                }
            },
        )
    }
}

@Composable
private fun ShopBottomBar(currentTab: TopLevelTab, cartItemCount: Int, onTabClick: (TopLevelTab) -> Unit) {
    NavigationBar {
        TopLevelTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == currentTab,
                onClick = { onTabClick(tab) },
                label = { Text(stringResource(tab.label)) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (tab == TopLevelTab.CART && cartItemCount > 0) {
                                Badge { Text(if (cartItemCount > 99) "99+" else cartItemCount.toString()) }
                            }
                        },
                    ) {
                        Icon(painterResource(tab.icon), contentDescription = null)
                    }
                },
            )
        }
    }
}

private fun NavBackStack<NavKey>.resetTo(vararg routes: Route) {
    clear()
    addAll(routes)
}
