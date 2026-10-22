package kz.mtemirbay.shop

import androidx.compose.runtime.Composable
import kz.mtemirbay.shop.core.designsystem.ShopTheme
import kz.mtemirbay.shop.feature.catalog.presentation.CatalogScreen

@Composable
fun App() {
    ShopTheme {
        CatalogScreen()
    }
}
