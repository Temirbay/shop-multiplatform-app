package kz.mtemirbay.shop.data

import kz.mtemirbay.shop.db.Users
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class ExposedUserRepository(private val database: Database) : UserRepository {

    override suspend fun create(name: String, phone: String, passwordHash: String): User? =
        try {
            suspendTransaction(database) {
                val id = Users.insertAndGetId {
                    it[Users.name] = name
                    it[Users.phone] = phone
                    it[Users.passwordHash] = passwordHash
                    it[Users.createdAt] = System.currentTimeMillis()
                }
                User(id.value, name, phone, passwordHash)
            }
        } catch (e: ExposedSQLException) {
            // Нарушение uniqueIndex на phone. Проверка «есть ли такой телефон» перед вставкой не спасла бы
            // от двух одновременных регистраций — надёжнее положиться на ограничение в БД
            null
        }

    override suspend fun findByPhone(phone: String): User? = suspendTransaction(database) {
        Users.selectAll().where { Users.phone eq phone }.singleOrNull()?.toUser()
    }

    override suspend fun findById(id: Long): User? = suspendTransaction(database) {
        Users.selectAll().where { Users.id eq id }.singleOrNull()?.toUser()
    }
}

private fun ResultRow.toUser() = User(
    id = this[Users.id].value,
    name = this[Users.name],
    phone = this[Users.phone],
    passwordHash = this[Users.passwordHash],
)
