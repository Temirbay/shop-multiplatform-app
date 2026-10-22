package kz.mtemirbay.shop.feature.product.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import kz.mtemirbay.shop.core.designsystem.BackButton
import kz.mtemirbay.shop.core.designsystem.ShopTheme
import kz.mtemirbay.shop.core.ui.ErrorContent
import kz.mtemirbay.shop.core.ui.LoadingContent
import kz.mtemirbay.shop.core.ui.MessageContent
import kz.mtemirbay.shop.core.ui.formatPriceKzt
import kz.mtemirbay.shop.feature.product.domain.ProductDetails
import kz.mtemirbay.shop.feature.product.generated.resources.Res
import kz.mtemirbay.shop.feature.product.generated.resources.product_add_to_cart
import kz.mtemirbay.shop.feature.product.generated.resources.product_added_to_cart
import kz.mtemirbay.shop.feature.product.generated.resources.product_error
import kz.mtemirbay.shop.feature.product.generated.resources.product_not_found
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ProductScreen(productId: Long, onBack: () -> Unit) {
    // parametersOf: id из маршрута уходит в конструктор ViewModel (см. productModule)
    val viewModel = koinViewModel<ProductViewModel> { parametersOf(productId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val addedMessage = stringResource(Res.string.product_added_to_cart)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProductEffect.AddedToCart -> snackbarHostState.showSnackbar(addedMessage)
            }
        }
    }

    ProductContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProductContent(
    state: ProductState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onEvent: (ProductEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state is ProductState.Content) {
                        Text(state.product.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (state) {
            ProductState.Loading -> LoadingContent(modifier)
            ProductState.NotFound -> MessageContent(stringResource(Res.string.product_not_found), modifier)
            ProductState.Error -> ErrorContent(
                message = stringResource(Res.string.product_error),
                onRetry = { onEvent(ProductEvent.Retry) },
                modifier = modifier,
            )
            is ProductState.Content -> ProductDetailsContent(
                product = state.product,
                onAddToCart = { onEvent(ProductEvent.AddToCart) },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun ProductDetailsContent(
    product: ProductDetails,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        // widthIn: на широком окне Desktop/Web контент не растягивается на весь экран
        Column(
            modifier = Modifier.widthIn(max = 600.dp).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp)),
            )
            Text(product.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = formatPriceKzt(product.priceKzt),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(product.description, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onAddToCart, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(stringResource(Res.string.product_add_to_cart))
            }
        }
    }
}

@Preview
@Composable
private fun ProductContentPreview() {
    ShopTheme {
        ProductContent(
            state = ProductState.Content(
                ProductDetails(1, "Смартфон Nova X", "6.5\" OLED, 128 ГБ, 5000 мА·ч", 189_990, ""),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onEvent = {},
        )
    }
}
