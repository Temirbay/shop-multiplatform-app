package kz.mtemirbay.shop.data

data class User(
    val id: Long,
    val name: String,
    val phone: String,
    val passwordHash: String,
)

interface UserRepository {
    /** null — телефон уже занят */
    suspend fun create(name: String, phone: String, passwordHash: String): User?

    suspend fun findByPhone(phone: String): User?

    suspend fun findById(id: Long): User?
}
