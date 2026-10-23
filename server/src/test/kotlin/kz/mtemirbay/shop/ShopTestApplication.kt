package kz.mtemirbay.shop

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Уникальная H2 в памяти: у каждого теста своя пустая БД, файл server/data не трогается */
@OptIn(ExperimentalUuidApi::class)
fun inMemoryDatabaseUrl() = "jdbc:h2:mem:test-${Uuid.random()};DB_CLOSE_DELAY=-1"

/** Сервер целиком (Koin, БД, роуты) + клиент с JSON */
fun shopTestApplication(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) = testApplication {
    // Свой конфиг вместо application.conf: иначе тесты писали бы в настоящий файл БД
    environment {
        config = MapApplicationConfig("database.url" to inMemoryDatabaseUrl())
    }
    application { module() }
    val client = createClient {
        install(ContentNegotiation) { json() }
    }
    block(client)
}
