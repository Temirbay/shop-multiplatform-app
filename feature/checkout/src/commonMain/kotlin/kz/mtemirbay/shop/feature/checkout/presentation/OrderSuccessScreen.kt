package kz.mtemirbay.shop.feature.checkout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kz.mtemirbay.shop.core.designsystem.ShopTheme
import kz.mtemirbay.shop.core.ui.formatPriceKzt
import kz.mtemirbay.shop.feature.checkout.generated.resources.Res
import kz.mtemirbay.shop.feature.checkout.generated.resources.order_success_hint
import kz.mtemirbay.shop.feature.checkout.generated.resources.order_success_title
import kz.mtemirbay.shop.feature.checkout.generated.resources.order_success_to_catalog
import kz.mtemirbay.shop.feature.checkout.generated.resources.order_success_total
import org.jetbrains.compose.resources.stringResource

/** Без ViewModel: всё нужное приходит в аргументах из маршрута, загружать нечего */
@Composable
fun OrderSuccessScreen(orderId: Long, totalKzt: Long, onGoToCatalog: () -> Unit) {
    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.safeDrawingPadding().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.order_success_title, orderId),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.order_success_total, formatPriceKzt(totalKzt)),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(Res.string.order_success_hint),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onGoToCatalog) {
                Text(stringResource(Res.string.order_success_to_catalog))
            }
        }
    }
}

@Preview
@Composable
private fun OrderSuccessScreenPreview() {
    ShopTheme {
        OrderSuccessScreen(orderId = 1042, totalKzt = 419_970, onGoToCatalog = {})
    }
}
