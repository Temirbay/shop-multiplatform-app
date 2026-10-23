package kz.mtemirbay.shop.navigation

import kz.mtemirbay.shop.core.designsystem.ShopIcons
import kz.mtemirbay.shop.app.shared.generated.resources.Res
import kz.mtemirbay.shop.app.shared.generated.resources.tab_account
import kz.mtemirbay.shop.app.shared.generated.resources.tab_cart
import kz.mtemirbay.shop.app.shared.generated.resources.tab_catalog
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** Вкладки нижней панели. У каждой — свой стек экранов, начиная с [root] */
enum class TopLevelTab(val root: Route, val label: StringResource, val icon: DrawableResource) {
    CATALOG(CatalogRoute, Res.string.tab_catalog, ShopIcons.Catalog),
    CART(CartRoute, Res.string.tab_cart, ShopIcons.Cart),
    ACCOUNT(AccountRoute, Res.string.tab_account, ShopIcons.Person),
}
