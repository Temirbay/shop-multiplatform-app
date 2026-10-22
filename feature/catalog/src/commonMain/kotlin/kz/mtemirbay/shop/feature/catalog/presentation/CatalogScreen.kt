package kz.mtemirbay.shop.feature.catalog.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kz.mtemirbay.shop.core.designsystem.ShopTheme
import kz.mtemirbay.shop.core.ui.ErrorContent
import kz.mtemirbay.shop.core.ui.LoadingContent
import kz.mtemirbay.shop.core.ui.MessageContent
import kz.mtemirbay.shop.core.ui.formatPriceKzt
import kz.mtemirbay.shop.feature.catalog.domain.Category
import kz.mtemirbay.shop.feature.catalog.domain.Product
import kz.mtemirbay.shop.feature.catalog.generated.resources.Res
import kz.mtemirbay.shop.feature.catalog.generated.resources.catalog_all_categories
import kz.mtemirbay.shop.feature.catalog.generated.resources.catalog_empty
import kz.mtemirbay.shop.feature.catalog.generated.resources.catalog_error
import kz.mtemirbay.shop.feature.catalog.generated.resources.catalog_search_hint
import kz.mtemirbay.shop.feature.catalog.generated.resources.catalog_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CatalogScreen(onProductClick: (productId: Long) -> Unit) {
    val viewModel = koinViewModel<CatalogViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    CatalogContent(state = state, onEvent = viewModel::onEvent, onProductClick = onProductClick)
}

@Composable
internal fun CatalogContent(
    state: CatalogState,
    onEvent: (CatalogEvent) -> Unit,
    onProductClick: (productId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier.fillMaxSize()) {
        Column(Modifier.safeDrawingPadding()) {
            Text(
                text = stringResource(Res.string.catalog_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            )
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(CatalogEvent.QueryChanged(it)) },
                placeholder = { Text(stringResource(Res.string.catalog_search_hint)) },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
            CategoryChips(
                categories = state.categories,
                selectedCategoryId = state.selectedCategoryId,
                onSelect = { onEvent(CatalogEvent.CategorySelected(it)) },
            )
            when (val products = state.products) {
                ProductsState.Loading -> LoadingContent()
                ProductsState.Error -> ErrorContent(
                    message = stringResource(Res.string.catalog_error),
                    onRetry = { onEvent(CatalogEvent.Retry) },
                )
                is ProductsState.Content ->
                    if (products.items.isEmpty()) {
                        MessageContent(stringResource(Res.string.catalog_empty))
                    } else {
                        ProductGrid(products.items, onProductClick)
                    }
            }
        }
    }
}

@Composable
private fun CategoryChips(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onSelect: (Long?) -> Unit,
) {
    if (categories.isEmpty()) return
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(Res.string.catalog_all_categories)) },
            )
        }
        items(categories, key = { it.id }) { category ->
            FilterChip(
                selected = selectedCategoryId == category.id,
                onClick = { onSelect(category.id) },
                label = { Text(category.title) },
            )
        }
    }
}

@Composable
private fun ProductGrid(products: List<Product>, onProductClick: (Long) -> Unit) {
    LazyVerticalGrid(
        // Adaptive: число колонок зависит от ширины — 2 на телефоне, больше на Desktop и Web
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(products, key = { it.id }) { product ->
            ProductCard(product, onClick = { onProductClick(product.id) })
        }
    }
}

@Composable
private fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(onClick = onClick) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        )
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.bodyMedium,
                // Всегда две строки под название — иначе карточки с коротким названием ниже соседних
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatPriceKzt(product.priceKzt),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview
@Composable
private fun CatalogContentPreview() {
    ShopTheme {
        CatalogContent(
            state = CatalogState(
                categories = listOf(Category(1, "Электроника"), Category(2, "Одежда")),
                selectedCategoryId = 1,
                products = ProductsState.Content(
                    listOf(
                        Product(1, "Смартфон Nova X", 189_990, ""),
                        Product(2, "Беспроводные наушники Pulse", 39_990, ""),
                    ),
                ),
            ),
            onEvent = {},
            onProductClick = {},
        )
    }
}
