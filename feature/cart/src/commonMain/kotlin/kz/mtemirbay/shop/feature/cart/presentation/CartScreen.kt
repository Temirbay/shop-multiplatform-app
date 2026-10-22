package kz.mtemirbay.shop.feature.cart.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kz.mtemirbay.shop.core.data.cart.CartItem
import kz.mtemirbay.shop.core.designsystem.BackButton
import kz.mtemirbay.shop.core.designsystem.ShopIcons
import kz.mtemirbay.shop.core.designsystem.ShopTheme
import kz.mtemirbay.shop.core.ui.formatPriceKzt
import kz.mtemirbay.shop.feature.cart.generated.resources.Res
import kz.mtemirbay.shop.feature.cart.generated.resources.cart_checkout
import kz.mtemirbay.shop.feature.cart.generated.resources.cart_decrease
import kz.mtemirbay.shop.feature.cart.generated.resources.cart_empty
import kz.mtemirbay.shop.feature.cart.generated.resources.cart_go_to_catalog
import kz.mtemirbay.shop.feature.cart.generated.resources.cart_increase
import kz.mtemirbay.shop.feature.cart.generated.resources.cart_remove
import kz.mtemirbay.shop.feature.cart.generated.resources.cart_title
import kz.mtemirbay.shop.feature.cart.generated.resources.cart_total
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CartScreen(onBack: () -> Unit, onGoToCatalog: () -> Unit, onCheckout: () -> Unit) {
    val viewModel = koinViewModel<CartViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    CartContent(
        state = state,
        onBack = onBack,
        onGoToCatalog = onGoToCatalog,
        onCheckout = onCheckout,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CartContent(
    state: CartState,
    onBack: () -> Unit,
    onGoToCatalog: () -> Unit,
    onCheckout: () -> Unit,
    onEvent: (CartEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.cart_title)) },
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
        bottomBar = {
            if (state.items.isNotEmpty()) {
                CartSummary(state.totalQuantity, state.totalPriceKzt, onCheckout = onCheckout)
            }
        },
    ) { padding ->
        if (state.items.isEmpty()) {
            EmptyCart(onGoToCatalog, Modifier.padding(padding))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                items(state.items, key = { it.productId }) { item ->
                    CartItemRow(item, onEvent, Modifier.widthIn(max = 600.dp))
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(item: CartItem, onEvent: (CartEvent) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(88.dp).clip(RoundedCornerShape(12.dp)),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.title, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                text = formatPriceKzt(item.priceKzt),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                CartIconButton(
                    icon = ShopIcons.Remove,
                    description = stringResource(Res.string.cart_decrease),
                    enabled = item.quantity > MIN_QUANTITY,
                ) {
                    onEvent(CartEvent.Decrease(item.productId))
                }
                Text(item.quantity.toString(), style = MaterialTheme.typography.titleMedium)
                CartIconButton(ShopIcons.Add, stringResource(Res.string.cart_increase)) {
                    onEvent(CartEvent.Increase(item.productId))
                }
                Box(Modifier.weight(1f))
                CartIconButton(ShopIcons.Delete, stringResource(Res.string.cart_remove)) {
                    onEvent(CartEvent.Remove(item.productId))
                }
            }
        }
    }
}

@Composable
private fun CartIconButton(
    icon: DrawableResource,
    description: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(painterResource(icon), contentDescription = description)
    }
}

@Composable
private fun CartSummary(totalQuantity: Int, totalPriceKzt: Long, onCheckout: () -> Unit) {
    Surface(tonalElevation = 3.dp) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalDivider(Modifier.padding(bottom = 12.dp))
            Column(Modifier.widthIn(max = 600.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.cart_total, totalQuantity),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = formatPriceKzt(totalPriceKzt),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.cart_checkout))
                }
            }
        }
    }
}

@Composable
private fun EmptyCart(onGoToCatalog: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(Res.string.cart_empty), style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(onClick = onGoToCatalog) {
            Text(stringResource(Res.string.cart_go_to_catalog))
        }
    }
}

@Preview
@Composable
private fun CartContentPreview() {
    ShopTheme {
        CartContent(
            state = CartState(
                items = listOf(
                    CartItem(1, "Смартфон Nova X", 189_990, "", quantity = 2),
                    CartItem(11, "Kotlin in Action", 12_990, "", quantity = 1),
                ),
                totalQuantity = 3,
                totalPriceKzt = 392_970,
            ),
            onBack = {},
            onGoToCatalog = {},
            onCheckout = {},
            onEvent = {},
        )
    }
}
