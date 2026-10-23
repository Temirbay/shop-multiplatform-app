package kz.mtemirbay.shop.feature.account.presentation

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kz.mtemirbay.shop.core.designsystem.ShopTheme
import kz.mtemirbay.shop.core.model.AuthField
import kz.mtemirbay.shop.core.model.AuthValidationError
import kz.mtemirbay.shop.core.ui.COUNTRY_CODE
import kz.mtemirbay.shop.core.ui.LoadingContent
import kz.mtemirbay.shop.core.ui.PhoneVisualTransformation
import kz.mtemirbay.shop.core.ui.formatPhone
import kz.mtemirbay.shop.feature.account.generated.resources.Res
import kz.mtemirbay.shop.feature.account.generated.resources.account_title
import kz.mtemirbay.shop.feature.account.generated.resources.auth_error_invalid_credentials
import kz.mtemirbay.shop.feature.account.generated.resources.auth_error_name_empty
import kz.mtemirbay.shop.feature.account.generated.resources.auth_error_name_too_long
import kz.mtemirbay.shop.feature.account.generated.resources.auth_error_network
import kz.mtemirbay.shop.feature.account.generated.resources.auth_error_password_long
import kz.mtemirbay.shop.feature.account.generated.resources.auth_error_password_short
import kz.mtemirbay.shop.feature.account.generated.resources.auth_error_phone
import kz.mtemirbay.shop.feature.account.generated.resources.auth_error_phone_taken
import kz.mtemirbay.shop.feature.account.generated.resources.auth_login
import kz.mtemirbay.shop.feature.account.generated.resources.auth_name
import kz.mtemirbay.shop.feature.account.generated.resources.auth_password
import kz.mtemirbay.shop.feature.account.generated.resources.auth_phone
import kz.mtemirbay.shop.feature.account.generated.resources.auth_phone_hint
import kz.mtemirbay.shop.feature.account.generated.resources.auth_register
import kz.mtemirbay.shop.feature.account.generated.resources.auth_tab_login
import kz.mtemirbay.shop.feature.account.generated.resources.auth_tab_register
import kz.mtemirbay.shop.feature.account.generated.resources.profile_logout
import kz.mtemirbay.shop.feature.account.generated.resources.profile_my_orders
import kz.mtemirbay.shop.feature.account.generated.resources.profile_logout_cancel
import kz.mtemirbay.shop.feature.account.generated.resources.profile_logout_message
import kz.mtemirbay.shop.feature.account.generated.resources.profile_logout_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Корень вкладки «Профиль». Гость — форма входа/регистрации, вошёл — профиль. Переключается само, по сессии */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(onMyOrdersClick: () -> Unit) {
    val profileViewModel = koinViewModel<ProfileViewModel>()
    val profile by profileViewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.account_title)) },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(Modifier.widthIn(max = 600.dp).padding(16.dp)) {
                when (val state = profile) {
                    ProfileState.Loading -> LoadingContent()
                    ProfileState.Guest -> {
                        val authViewModel = koinViewModel<AuthViewModel>()
                        val authState by authViewModel.state.collectAsStateWithLifecycle()
                        AuthContent(authState, authViewModel::onEvent)
                    }
                    is ProfileState.LoggedIn -> ProfileContent(
                        state = state,
                        onMyOrdersClick = onMyOrdersClick,
                        onLogout = { profileViewModel.onEvent(ProfileEvent.Logout) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun AuthContent(state: AuthState, onEvent: (AuthEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PrimaryTabRow(selectedTabIndex = state.mode.ordinal) {
            Tab(
                selected = state.mode == AuthMode.LOGIN,
                onClick = { onEvent(AuthEvent.ModeChanged(AuthMode.LOGIN)) },
                text = { Text(stringResource(Res.string.auth_tab_login)) },
            )
            Tab(
                selected = state.mode == AuthMode.REGISTER,
                onClick = { onEvent(AuthEvent.ModeChanged(AuthMode.REGISTER)) },
                text = { Text(stringResource(Res.string.auth_tab_register)) },
            )
        }
        if (state.mode == AuthMode.REGISTER) {
            AuthField(
                value = state.name,
                onValueChange = { onEvent(AuthEvent.NameChanged(it)) },
                label = Res.string.auth_name,
                error = state.errors[AuthField.NAME],
                onFocusLost = { onEvent(AuthEvent.FieldFocusLost(AuthField.NAME)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            )
        }
        AuthField(
            value = state.phone,
            onValueChange = { onEvent(AuthEvent.PhoneChanged(it)) },
            label = Res.string.auth_phone,
            placeholder = Res.string.auth_phone_hint,
            prefix = "$COUNTRY_CODE ",
            error = state.errors[AuthField.PHONE],
            onFocusLost = { onEvent(AuthEvent.FieldFocusLost(AuthField.PHONE)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            visualTransformation = PhoneVisualTransformation,
        )
        AuthField(
            value = state.password,
            onValueChange = { onEvent(AuthEvent.PasswordChanged(it)) },
            label = Res.string.auth_password,
            error = state.errors[AuthField.PASSWORD],
            onFocusLost = { onEvent(AuthEvent.FieldFocusLost(AuthField.PASSWORD)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onEvent(AuthEvent.Submit) }),
            visualTransformation = PasswordVisualTransformation(),
        )
        state.formError?.let { error ->
            Text(
                text = stringResource(error.messageRes()),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Button(
            onClick = { onEvent(AuthEvent.Submit) },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(if (state.mode == AuthMode.LOGIN) Res.string.auth_login else Res.string.auth_register))
            }
        }
    }
}

@Composable
private fun ProfileContent(state: ProfileState.LoggedIn, onMyOrdersClick: () -> Unit, onLogout: () -> Unit) {
    // rememberSaveable: открытый диалог переживёт поворот экрана
    var showLogoutConfirmation by rememberSaveable { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(state.name, style = MaterialTheme.typography.headlineSmall)
        Text(formatPhone(state.phone), style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onMyOrdersClick, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text(stringResource(Res.string.profile_my_orders))
        }
        OutlinedButton(
            onClick = { showLogoutConfirmation = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.profile_logout))
        }
    }

    if (showLogoutConfirmation) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutConfirmation = false
                onLogout()
            },
            onDismiss = { showLogoutConfirmation = false },
        )
    }
}

@Composable
private fun LogoutConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        // Тап мимо диалога или «назад» — то же, что «Отмена»
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.profile_logout_title)) },
        text = { Text(stringResource(Res.string.profile_logout_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.profile_logout), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.profile_logout_cancel))
            }
        },
    )
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: StringResource,
    error: AuthValidationError?,
    onFocusLost: () -> Unit,
    keyboardOptions: KeyboardOptions,
    placeholder: StringResource? = null,
    prefix: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var hadFocus by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        placeholder = placeholder?.let { { Text(stringResource(it)) } },
        prefix = prefix?.let { { Text(it) } },
        isError = error != null,
        supportingText = error?.let { { Text(stringResource(it.messageRes())) } },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        modifier = Modifier.fillMaxWidth().onFocusChanged { focus ->
            if (focus.isFocused) hadFocus = true else if (hadFocus) onFocusLost()
        },
    )
}

private fun AuthValidationError.messageRes(): StringResource = when (this) {
    AuthValidationError.NAME_EMPTY -> Res.string.auth_error_name_empty
    AuthValidationError.NAME_TOO_LONG -> Res.string.auth_error_name_too_long
    AuthValidationError.PHONE_INVALID -> Res.string.auth_error_phone
    AuthValidationError.PASSWORD_TOO_SHORT -> Res.string.auth_error_password_short
    AuthValidationError.PASSWORD_TOO_LONG -> Res.string.auth_error_password_long
}

private fun AuthFormError.messageRes(): StringResource = when (this) {
    AuthFormError.INVALID_CREDENTIALS -> Res.string.auth_error_invalid_credentials
    AuthFormError.PHONE_TAKEN -> Res.string.auth_error_phone_taken
    AuthFormError.NETWORK -> Res.string.auth_error_network
}

@Preview
@Composable
private fun AuthContentPreview() {
    ShopTheme {
        AuthContent(
            state = AuthState(
                mode = AuthMode.REGISTER,
                phone = "701",
                errors = mapOf(AuthField.PHONE to AuthValidationError.PHONE_INVALID),
            ),
            onEvent = {},
        )
    }
}
