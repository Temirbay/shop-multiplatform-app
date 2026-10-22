package kz.mtemirbay.shop.feature.checkout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kz.mtemirbay.shop.core.data.cart.CartItem
import kz.mtemirbay.shop.core.designsystem.BackButton
import kz.mtemirbay.shop.core.designsystem.ShopTheme
import kz.mtemirbay.shop.core.model.OrderField
import kz.mtemirbay.shop.core.model.OrderValidationError
import kz.mtemirbay.shop.feature.checkout.generated.resources.Res
import kz.mtemirbay.shop.feature.checkout.generated.resources.checkout_address
import kz.mtemirbay.shop.feature.checkout.generated.resources.checkout_error_address
import kz.mtemirbay.shop.feature.checkout.generated.resources.checkout_error_items
import kz.mtemirbay.shop.feature.checkout.generated.resources.checkout_error_name_empty
import kz.mtemirbay.shop.feature.checkout.generated.resources.checkout_error_name_too_long
import kz.mtemirbay.shop.feature.checkout.generated.resources.checkout_error_phone
import kz.mtemirbay.shop.feature.checkout.generated.resources.checkout_name
import kz.mtemirbay.shop.feature.checkout.generated.resources.checkout_network_error
import kz.mtemirbay.shop.feature.checkout.generated.resources.checkout_phone
import kz.mtemirbay.shop.feature.checkout.generated.resources.checkout_phone_hint
import kz.mtemirbay.shop.feature.checkout.generated.resources.checkout_product_unavailable
import kz.mtemirbay.shop.feature.checkout.generated.resources.checkout_retry
import kz.mtemirbay.shop.feature.checkout.generated.resources.checkout_submit
import kz.mtemirbay.shop.feature.checkout.generated.resources.checkout_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CheckoutScreen(onBack: () -> Unit, onOrderPlaced: (orderId: Long, totalKzt: Long) -> Unit) {
    val viewModel = koinViewModel<CheckoutViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val networkError = stringResource(Res.string.checkout_network_error)
    val retryLabel = stringResource(Res.string.checkout_retry)
    val productUnavailable = stringResource(Res.string.checkout_product_unavailable)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CheckoutEffect.OrderPlaced -> onOrderPlaced(effect.orderId, effect.totalKzt)
                CheckoutEffect.NetworkError -> {
                    val result = snackbarHostState.showSnackbar(networkError, retryLabel, duration = SnackbarDuration.Long)
                    if (result == SnackbarResult.ActionPerformed) viewModel.onEvent(CheckoutEvent.Submit)
                }
                CheckoutEffect.ProductUnavailable -> snackbarHostState.showSnackbar(productUnavailable)
            }
        }
    }

    CheckoutContent(state, snackbarHostState, onBack, viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CheckoutContent(
    state: CheckoutState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onEvent: (CheckoutEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.checkout_title)) },
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 600.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FormField(
                    value = state.name,
                    onValueChange = { onEvent(CheckoutEvent.NameChanged(it)) },
                    label = Res.string.checkout_name,
                    error = state.errors[OrderField.NAME],
                    onFocusLost = { onEvent(CheckoutEvent.FieldFocusLost(OrderField.NAME)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                )
                FormField(
                    value = state.phone,
                    onValueChange = { onEvent(CheckoutEvent.PhoneChanged(it)) },
                    label = Res.string.checkout_phone,
                    placeholder = Res.string.checkout_phone_hint,
                    error = state.errors[OrderField.PHONE],
                    onFocusLost = { onEvent(CheckoutEvent.FieldFocusLost(OrderField.PHONE)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    prefix = "$COUNTRY_CODE ",
                    visualTransformation = PhoneVisualTransformation,
                )
                FormField(
                    value = state.address,
                    onValueChange = { onEvent(CheckoutEvent.AddressChanged(it)) },
                    label = Res.string.checkout_address,
                    error = state.errors[OrderField.ADDRESS],
                    onFocusLost = { onEvent(CheckoutEvent.FieldFocusLost(OrderField.ADDRESS)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { onEvent(CheckoutEvent.Submit) }),
                    singleLine = false,
                )
                TotalRow(state.itemCount, state.totalKzt, Modifier.padding(top = 8.dp))
                Button(
                    onClick = { onEvent(CheckoutEvent.Submit) },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(Res.string.checkout_submit))
                    }
                }
            }
        }
    }
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: StringResource,
    error: OrderValidationError?,
    onFocusLost: () -> Unit,
    keyboardOptions: KeyboardOptions,
    placeholder: StringResource? = null,
    prefix: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
) {
    // onFocusChanged срабатывает и при первом показе (без фокуса) — ошибку показываем, только если фокус был и ушёл
    var hadFocus by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        placeholder = placeholder?.let { { Text(stringResource(it)) } },
        prefix = prefix?.let { { Text(it) } },
        visualTransformation = visualTransformation,
        isError = error != null,
        supportingText = error?.let { { Text(stringResource(it.messageRes())) } },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 2,
        modifier = Modifier.fillMaxWidth().onFocusChanged { focus ->
            if (focus.isFocused) {
                hadFocus = true
            } else if (hadFocus) {
                onFocusLost()
            }
        },
    )
}

private fun OrderValidationError.messageRes(): StringResource = when (this) {
    OrderValidationError.NAME_EMPTY -> Res.string.checkout_error_name_empty
    OrderValidationError.NAME_TOO_LONG -> Res.string.checkout_error_name_too_long
    OrderValidationError.PHONE_INVALID -> Res.string.checkout_error_phone
    OrderValidationError.ADDRESS_TOO_SHORT -> Res.string.checkout_error_address
    OrderValidationError.ITEMS_EMPTY, OrderValidationError.QUANTITY_INVALID -> Res.string.checkout_error_items
}

@Preview
@Composable
private fun CheckoutContentPreview() {
    ShopTheme {
        CheckoutContent(
            state = CheckoutState(
                name = "Мирас",
                phone = "701",
                errors = mapOf(OrderField.PHONE to OrderValidationError.PHONE_INVALID),
                items = listOf(
                    CartItem(1, "Смартфон Nova X", 189_990, "", quantity = 2),
                    CartItem(2, "Беспроводные наушники Pulse", 39_990, "", quantity = 1),
                ),
                itemCount = 3,
                totalKzt = 419_970,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onEvent = {},
        )
    }
}
