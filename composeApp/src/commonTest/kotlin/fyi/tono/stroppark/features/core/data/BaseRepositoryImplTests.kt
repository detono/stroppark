package fyi.tono.stroppark.features.core.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngine.Companion.invoke
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.json.Json

abstract class BaseRepositoryImplTests {
  protected val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    isLenient = true
  }

  protected fun createClientWithResponse(
    content: String,
    statusCode: HttpStatusCode
  ): HttpClient {
    val mockEngine = MockEngine { _ ->
      respond(
        content = ByteReadChannel(content),
        status = statusCode,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
      )
    }

    return HttpClient(mockEngine) {
      install(ContentNegotiation) {
        json(json)
      }
    }
  }
}