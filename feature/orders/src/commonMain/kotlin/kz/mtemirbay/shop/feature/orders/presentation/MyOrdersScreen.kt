package kz.mtemirbay.shop.feature.orders.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import kz.mtemirbay.shop.core.designsystem.BackButton
import kz.mtemirbay.shop.core.designsystem.ShopTheme
import kz.mtemirbay.shop.core.ui.ErrorContent
import kz.mtemirbay.shop.core.ui.LoadingContent
import kz.mtemirbay.shop.core.ui.MessageContent
import kz.mtemirbay.shop.core.ui.formatPriceKzt
import kz.mtemirbay.shop.feature.orders.domain.OrderSummary
import kz.mtemirbay.shop.feature.orders.generated.resources.Res
import kz.mtemirbay.shop.feature.orders.generated.resources.order_items_count
import kz.mtemirbay.shop.feature.orders.generated.resources.order_number
import kz.mtemirbay.shop.feature.orders.generated.resources.orders_empty
import kz.mtemirbay.shop.feature.orders.generated.resources.orders_error
import kz.mtemirbay.shop.feature.orders.generated.resources.orders_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MyOrdersScreen(onBack: () -> Unit, onOrderClick: (orderId: Long) -> Unit) {
    val viewModel = koinViewModel<MyOrdersViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    MyOrdersContent(state, onBack, onOrderClick, onRetry = { viewModel.onEvent(MyOrdersEvent.Retry) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MyOrdersContent(
    state: MyOrdersState,
    onBack: () -> Unit,
    onOrderClick: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.orders_title)) },
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (state) {
            MyOrdersState.Loading -> LoadingContent(modifier)
            MyOrdersState.Error -> ErrorContent(stringResource(Res.string.orders_error), onRetry, modifier)
            is MyOrdersState.Content ->
                if (state.orders.isEmpty()) {
                    MessageContent(stringResource(Res.string.orders_empty), modifier)
                } else {
                    LazyColumn(
                        modifier = modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        items(state.orders, key = { it.id }) { order ->
                            OrderCard(order, onClick = { onOrderClick(order.id) }, Modifier.widthIn(max = 600.dp))
                        }
                    }
                }
        }
    }
}

@Composable
private fun OrderCard(order: OrderSummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.order_number, order.id),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(formatPriceKzt(order.totalKzt), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(
                text = formatOrderDate(order.createdAtMillis) + " · " + stringResource(Res.string.order_items_count, order.itemCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun MyOrdersContentPreview() {
    ShopTheme {
        MyOrdersContent(
            state = MyOrdersState.Content(
                listOf(
                    OrderSummary(id = 3, totalKzt = 459_970, itemCount = 3, createdAtMillis = 1_792_748_520_000),
                    OrderSummary(id = 1, totalKzt = 12_990, itemCount = 1, createdAtMillis = 1_792_662_120_000),
                ),
            ),
            onBack = {},
            onOrderClick = {},
            onRetry = {},
        )
    }
}
