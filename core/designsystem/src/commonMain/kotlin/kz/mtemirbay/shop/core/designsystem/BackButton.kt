package kz.mtemirbay.shop.core.designsystem

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import kz.mtemirbay.shop.core.designsystem.generated.resources.Res
import kz.mtemirbay.shop.core.designsystem.generated.resources.ds_back
import kz.mtemirbay.shop.core.designsystem.generated.resources.ic_arrow_back
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Стрелка «назад» для TopAppBar. Иконка — векторный ресурс: material-icons в Compose Multiplatform устарели */
@Composable
fun BackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_back),
            contentDescription = stringResource(Res.string.ds_back),
        )
    }
}
