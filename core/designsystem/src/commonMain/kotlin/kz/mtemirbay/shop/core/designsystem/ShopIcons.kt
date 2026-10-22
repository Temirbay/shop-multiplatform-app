package kz.mtemirbay.shop.core.designsystem

import kz.mtemirbay.shop.core.designsystem.generated.resources.Res
import kz.mtemirbay.shop.core.designsystem.generated.resources.ic_add
import kz.mtemirbay.shop.core.designsystem.generated.resources.ic_delete
import kz.mtemirbay.shop.core.designsystem.generated.resources.ic_remove
import org.jetbrains.compose.resources.DrawableResource

/** Иконки дизайн-системы для других модулей: Res-класс модуля internal, наружу отдаём только нужное */
object ShopIcons {
    val Add: DrawableResource get() = Res.drawable.ic_add
    val Remove: DrawableResource get() = Res.drawable.ic_remove
    val Delete: DrawableResource get() = Res.drawable.ic_delete
}
