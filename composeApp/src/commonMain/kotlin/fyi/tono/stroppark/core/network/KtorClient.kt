package fyi.tono.stroppark.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.logging.*
import co.touchlab.kermit.Logger as KermitLogger

fun createHttpClient(json: Json) = HttpClient {
  install(ContentNegotiation) {
    json(json)
  }

  install(Logging) {
    logger = object : Logger {
      override fun log(message: String) {
        KermitLogger.d(tag = "KtorClient", messageString = message)
      }
    }
    level = LogLevel.INFO
  }
}