package fyi.tono.stroppark.features.chargers.data

import fyi.tono.stroppark.features.core.data.BaseRepositoryImplTests
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChargerRepositoryImplTests: BaseRepositoryImplTests() {
  private val CHARGER_JSON = """
        {
          "total_count": 1,
          "results": [
            {
              "geometry": {
                "type": "Feature",
                "geometry": {
                  "coordinates": [
                    3.723773785219708,
                    51.04886883556457
                  ],
                  "type": "Point"
                },
                "properties": {}
              },
              "objectid": 941,
              "status": "In dienst",
              "straatnaam": "Parkeergarage Savaanstraat",
              "huisnummer": null,
              "cpo": "Allego",
              "fotolink": null,
              "type": "Normaallader",
              "kw": 22,
              "ligging": "Parkeergebouw",
              "url": "https://stad.gent/nl/mobiliteit-openbare-werken/parkeren/parkings-gent/parking-savaanstraat",
              "urid": "mob/laadpnt7373",
              "synckey": "autoparkeren.LAADPALEN_3857.mob/laadpnt7373",
              "syncdate": "2024-10-09T22:02:00Z",
              "betrokkenadressen": "Parkeergarage Savaanstraat",
              "geo_point_2d": {
                "lon": 3.723773785219708,
                "lat": 51.04886883556457
              }
            }
          ]
        }
    """.trimIndent()

  @Test
  fun `getChargers returns mapped domain data`() = runTest {
    val client = createClientWithResponse(
      content = CHARGER_JSON,
      statusCode = HttpStatusCode.OK
    )

    val repo = ChargerRepositoryImpl(httpClient = client)
    val result = repo.getChargers()

    assertEquals(1, result.size)
    assertEquals("Allego", result.first().provider)
    assertEquals(22, result.first().powerKw)
    assertTrue(result.first().isAvailable)
  }

  @Test
  fun `getChargers returns empty list on network error`() = runTest {
    val client = createClientWithResponse(
      "",
      HttpStatusCode.InternalServerError
    )

    val repo = ChargerRepositoryImpl(httpClient = client)
    val result = repo.getChargers()

    assertTrue(result.isEmpty(), "Expected empty list on server error")
  }
}