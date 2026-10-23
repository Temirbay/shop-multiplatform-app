package kz.mtemirbay.shop.auth

import at.favre.lib.crypto.bcrypt.BCrypt

/**
 * bcrypt: медленный хэш с солью. Медленный специально — перебор паролей по утёкшей базе становится дорогим.
 * Соль своя у каждого пароля: одинаковые пароли дают разные хэши
 */
class PasswordHasher(private val cost: Int = DEFAULT_COST) {

    fun hash(password: String): String = BCrypt.withDefaults().hashToString(cost, password.toCharArray())

    fun verify(password: String, hash: String): Boolean =
        BCrypt.verifyer().verify(password.toCharArray(), hash).verified

    companion object {
        const val DEFAULT_COST = 12
    }
}
