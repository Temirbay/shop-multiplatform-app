package kz.mtemirbay.shop.feature.checkout.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kz.mtemirbay.shop.core.model.CreateOrderRequest
import kz.mtemirbay.shop.core.model.OrderDto

internal class OrderApi(private val client: HttpClient) {

    suspend fun createOrder(request: CreateOrderRequest): OrderDto =
        client.post("orders") {
            // У POST есть тело — здесь Content-Type нужен (в отличие от GET)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
