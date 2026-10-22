package kz.mtemirbay.shop

import androidx.compose.runtime.Composable
import kz.mtemirbay.shop.core.designsystem.ShopTheme
import kz.mtemirbay.shop.navigation.ShopNavDisplay

@Composable
fun App() {
    ShopTheme {
        ShopNavDisplay()
    }
}
