package kz.mtemirbay.shop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kz.mtemirbay.shop.core.designsystem.ShopTheme
import kz.mtemirbay.shop.navigation.ShopNavDisplay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    val appViewModel = koinViewModel<AppViewModel>()
    val cartItemCount by appViewModel.cartItemCount.collectAsStateWithLifecycle()
    ShopTheme {
        ShopNavDisplay(cartItemCount = cartItemCount)
    }
}
