package kz.mtemirbay.shop.feature.checkout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kz.mtemirbay.shop.core.data.cart.CartItem
import kz.mtemirbay.shop.core.designsystem.BackButton
import kz.mtemirbay.shop.core.designsystem.ShopTheme
import kz.mtemirbay.shop.core.ui.formatPriceKzt
import kz.mtemirbay.shop.feature.checkout.generated.resources.Res
import kz.mtemirbay.shop.feature.checkout.generated.resources.checkout_receipt_line
import kz.mtemirbay.shop.feature.checkout.generated.resources.checkout_receipt_title
import kz.mtemirbay.shop.feature.checkout.generated.resources.checkout_receipt_total
import kz.mtemirbay.shop.feature.checkout.generated.resources.review_continue
import kz.mtemirbay.shop.feature.checkout.generated.resources.review_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Шаг 1 оформления: чек. Данные доставки — на следующем экране ([CheckoutScreen]) */
@Composable
fun OrderReviewScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    // Та же CheckoutViewModel: из неё нужен только состав корзины. У каждого экрана в стеке свой экземпляр
    val viewModel = koinViewModel<CheckoutViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    OrderReviewContent(state, onBack, onContinue)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OrderReviewContent(state: CheckoutState, onBack: () -> Unit, onContinue: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.review_title)) },
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 600.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Receipt(state.items, state.itemCount, state.totalKzt)
                Button(
                    onClick = onContinue,
                    enabled = state.items.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.review_continue))
                }
            }
        }
    }
}

/** Чек: каждая позиция «количество × цена = сумма» и итог */
@Composable
internal fun Receipt(items: List<CartItem>, itemCount: Int, totalKzt: Long, modifier: Modifier = Modifier) {
    OutlinedCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(Res.string.checkout_receipt_title), style = MaterialTheme.typography.titleMedium)
            items.forEach { item ->
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(item.title, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = stringResource(Res.string.checkout_receipt_line, item.quantity, formatPriceKzt(item.priceKzt)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(formatPriceKzt(item.priceKzt * item.quantity), style = MaterialTheme.typography.bodyMedium)
                }
            }
            HorizontalDivider()
            TotalRow(itemCount, totalKzt)
        }
    }
}

/** Строка «Итого, N шт. — сумма»: в чеке и над кнопкой на экране с данными доставки */
@Composable
internal fun TotalRow(itemCount: Int, totalKzt: Long, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(Res.string.checkout_receipt_total, itemCount),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Text(formatPriceKzt(totalKzt), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Preview
@Composable
private fun OrderReviewContentPreview() {
    ShopTheme {
        OrderReviewContent(
            state = CheckoutState(
                items = listOf(
                    CartItem(1, "Смартфон Nova X", 189_990, "", quantity = 2),
                    CartItem(2, "Беспроводные наушники Pulse", 39_990, "", quantity = 1),
                ),
                itemCount = 3,
                totalKzt = 419_970,
            ),
            onBack = {},
            onContinue = {},
        )
    }
}
