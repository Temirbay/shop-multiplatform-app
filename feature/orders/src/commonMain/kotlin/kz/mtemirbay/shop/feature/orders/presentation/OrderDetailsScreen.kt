package kz.mtemirbay.shop.feature.orders.presentation

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
import kz.mtemirbay.shop.core.designsystem.BackButton
import kz.mtemirbay.shop.core.designsystem.ShopTheme
import kz.mtemirbay.shop.core.ui.ErrorContent
import kz.mtemirbay.shop.core.ui.LoadingContent
import kz.mtemirbay.shop.core.ui.MessageContent
import kz.mtemirbay.shop.core.ui.formatPhone
import kz.mtemirbay.shop.core.ui.formatPriceKzt
import kz.mtemirbay.shop.feature.orders.domain.OrderDetails
import kz.mtemirbay.shop.feature.orders.domain.OrderLine
import kz.mtemirbay.shop.feature.orders.generated.resources.Res
import kz.mtemirbay.shop.feature.orders.generated.resources.order_delivery
import kz.mtemirbay.shop.feature.orders.generated.resources.order_error
import kz.mtemirbay.shop.feature.orders.generated.resources.order_items
import kz.mtemirbay.shop.feature.orders.generated.resources.order_line
import kz.mtemirbay.shop.feature.orders.generated.resources.order_not_found
import kz.mtemirbay.shop.feature.orders.generated.resources.order_number
import kz.mtemirbay.shop.feature.orders.generated.resources.order_total
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun OrderDetailsScreen(orderId: Long, onBack: () -> Unit) {
    val viewModel = koinViewModel<OrderDetailsViewModel> { parametersOf(orderId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    OrderDetailsContent(orderId, state, onBack, onRetry = { viewModel.onEvent(OrderDetailsEvent.Retry) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OrderDetailsContent(orderId: Long, state: OrderDetailsState, onBack: () -> Unit, onRetry: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.order_number, orderId)) },
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (state) {
            OrderDetailsState.Loading -> LoadingContent(modifier)
            OrderDetailsState.NotFound -> MessageContent(stringResource(Res.string.order_not_found), modifier)
            OrderDetailsState.Error -> ErrorContent(stringResource(Res.string.order_error), onRetry, modifier)
            is OrderDetailsState.Content -> OrderBody(state.order, modifier)
        }
    }
}

@Composable
private fun OrderBody(order: OrderDetails, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 600.dp).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = formatOrderDate(order.createdAtMillis),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Section(stringResource(Res.string.order_delivery)) {
                Text(order.customerName, style = MaterialTheme.typography.bodyLarge)
                Text(formatPhone(order.phone), style = MaterialTheme.typography.bodyMedium)
                Text(order.address, style = MaterialTheme.typography.bodyMedium)
            }
            Section(stringResource(Res.string.order_items)) {
                order.lines.forEach { line -> LineRow(line) }
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.order_total, order.lines.sumOf { it.quantity }),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(formatPriceKzt(order.totalKzt), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun LineRow(line: OrderLine) {
    Row(verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(line.title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(Res.string.order_line, line.quantity, formatPriceKzt(line.priceKzt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(formatPriceKzt(line.priceKzt * line.quantity), style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview
@Composable
private fun OrderDetailsContentPreview() {
    ShopTheme {
        OrderDetailsContent(
            orderId = 3,
            state = OrderDetailsState.Content(
                OrderDetails(
                    id = 3,
                    customerName = "Мирас",
                    phone = "+77011234567",
                    address = "Алматы, Абая 1",
                    totalKzt = 459_970,
                    createdAtMillis = 1_792_748_520_000,
                    lines = listOf(OrderLine("Ноутбук Air 14", 1, 449_990), OrderLine("Абай. Слова назидания", 2, 4_990)),
                ),
            ),
            onBack = {},
            onRetry = {},
        )
    }
}
