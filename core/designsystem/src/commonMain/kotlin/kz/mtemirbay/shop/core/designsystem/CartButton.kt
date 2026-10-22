package kz.mtemirbay.shop.core.designsystem

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kz.mtemirbay.shop.core.designsystem.generated.resources.Res
import kz.mtemirbay.shop.core.designsystem.generated.resources.ds_cart
import kz.mtemirbay.shop.core.designsystem.generated.resources.ic_cart
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Иконка корзины с бейджем количества товаров. При 0 бейдж скрыт */
@Composable
fun CartButton(itemCount: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (itemCount > 0) {
                    Badge { Text(if (itemCount > 99) "99+" else itemCount.toString()) }
                }
            },
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_cart),
                contentDescription = stringResource(Res.string.ds_cart),
            )
        }
    }
}
