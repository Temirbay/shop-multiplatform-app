package kz.mtemirbay.shop.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B6E4F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA6F2CF),
    onPrimaryContainer = Color(0xFF002116),
    secondary = Color(0xFF4C6358),
    secondaryContainer = Color(0xFFCEE9DA),
    onSecondaryContainer = Color(0xFF092016),
    background = Color(0xFFF7FAF7),
    surface = Color(0xFFF7FAF7),
    surfaceContainer = Color(0xFFEBEFEB),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8BD6B4),
    onPrimary = Color(0xFF003827),
    primaryContainer = Color(0xFF005139),
    onPrimaryContainer = Color(0xFFA6F2CF),
    secondary = Color(0xFFB3CCBE),
    secondaryContainer = Color(0xFF354B40),
    onSecondaryContainer = Color(0xFFCEE9DA),
    background = Color(0xFF0F1512),
    surface = Color(0xFF0F1512),
    surfaceContainer = Color(0xFF1B211E),
)

@Composable
fun ShopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
