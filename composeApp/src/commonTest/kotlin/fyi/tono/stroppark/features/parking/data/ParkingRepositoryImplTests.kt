package fyi.tono.stroppark.features.parking.data

import fyi.tono.stroppark.features.core.data.BaseRepositoryImplTests
import fyi.tono.stroppark.features.parking.domain.ParkingType
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
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class ParkingRepositoryImplTests: BaseRepositoryImplTests() {
  private val GHENT_PARKING_JSON = """
    {
      "total_count": 13,
      "results": [
        {
          "name": "Savaanstraat",
          "lastupdate": "2026-02-27T21:37:23+01:00",
          "totalcapacity": 510,
          "availablecapacity": 261,
          "occupation": 48,
          "type": "carPark",
          "description": "Ondergrondse parkeergarage Savaanstraat in Gent",
          "id": "https://stad.gent/nl/mobiliteit-openbare-werken/parkeren/parkings-gent/parking-savaanstraat",
          "openingtimesdescription": "24/7",
          "isopennow": 1,
          "temporaryclosed": 0,
          "operatorinformation": "Mobiliteitsbedrijf Gent",
          "freeparking": 0,
          "urllinkaddress": "https://stad.gent/nl/mobiliteit-openbare-werken/parkeren/parkings-gent/parking-savaanstraat",
          "occupancytrend": "unknown",
          "locationanddimension": "{\"specificAccessInformation\": [\"inrit\"], \"level\": \"0\", \"roadNumber\": \"?\", \"roadName\": \"Savaanstraat 13\\n9000 Gent\", \"contactDetailsTelephoneNumber\": \"Tel.: 09 266 29 40\", \"coordinatesForDisplay\": {\"latitude\": 51.04877362543108, \"longitude\": 3.7234627726667133}}",
          "location": {
            "lon": 3.7234627726667133,
            "lat": 51.04877362543108
          },
          "text": null,
          "categorie": "parking in LEZ",
          "dashboard": "True"
        },
        {
          "name": "Tolhuis",
          "lastupdate": "2026-02-27T21:37:23+01:00",
          "totalcapacity": 155,
          "availablecapacity": 38,
          "occupation": 75,
          "type": "offStreetParkingGround",
          "description": "Ondergrondse parkeergarage Tolhuis in Gent",
          "id": "https://stad.gent/nl/mobiliteit-openbare-werken/parkeren/parkings-gent/parking-tolhuis",
          "openingtimesdescription": "24/7",
          "isopennow": 1,
          "temporaryclosed": 0,
          "operatorinformation": "Mobiliteitsbedrijf Gent",
          "freeparking": 0,
          "urllinkaddress": "https://stad.gent/nl/mobiliteit-openbare-werken/parkeren/parkings-gent/parking-tolhuis",
          "occupancytrend": "unknown",
          "locationanddimension": "{\"specificAccessInformation\": [\"inrit\"], \"level\": \"0\", \"roadNumber\": \"?\", \"roadName\": \"?\", \"coordinatesForDisplay\": {\"latitude\": 51.0637023559265, \"longitude\": 3.724968367281895}}",
          "location": {
            "lon": 3.724968367281895,
            "lat": 51.0637023559265
          },
          "text": null,
          "categorie": "parking in LEZ",
          "dashboard": "True"
        }
      ]
    }
  """.trimIndent()

  //region Helpers
  private fun parkingJson(
    name: String = "Test Parking",
    type: String = "carPark",
    category: String = "parking in LEZ",
    isOpenNow: Int = 1,
    freeParking: Int = 0,
    includeLocation: Boolean = true,
    locationAndDimension: String = """{"specificAccessInformation": ["inrit"], "level": "0", "contactDetailsTelephoneNumber": "09 123 45 67", "coordinatesForDisplay": {"latitude": 51.05, "longitude": 3.72}}"""
  ): String {
    val locationBlock = if (includeLocation) """
      "location": { "lon": 3.72, "lat": 51.05 },
    """.trimIndent() else ""

    return """
      {
        "total_count": 1,
        "results": [
          {
            "name": "$name",
            "lastupdate": "2026-02-27T12:00:00+01:00",
            "totalcapacity": 100,
            "availablecapacity": 50,
            "occupation": 50,
            "type": "$type",
            "description": "Test",
            "id": "https://example.com/test",
            "openingtimesdescription": "24/7",
            "isopennow": $isOpenNow,
            "temporaryclosed": 0,
            "operatorinformation": "Test Operator",
            "freeparking": $freeParking,
            "urllinkaddress": "https://example.com/test",
            "occupancytrend": "unknown",
            "locationanddimension": ${json.encodeToString(locationAndDimension)},
            $locationBlock
            "text": null,
            "categorie": "$category",
            "dashboard": "True"
          }
        ]
      }
    """.trimIndent()
  }

  //endregion

  @Test
  fun `getParkingOccupancy returns mapped data on success`() = runTest {
    val testClient = createClientWithResponse(
      GHENT_PARKING_JSON,
      HttpStatusCode.OK
    )

    val repo = ParkingRepositoryImpl(testClient, json)

    val result = repo.getParkingOccupancy()
    assertTrue(result.isNotEmpty())
  }

  @Test
  fun `getParkingOccupancy maps correctly on success`() = runTest {
    val testClient = createClientWithResponse(
      GHENT_PARKING_JSON,
      HttpStatusCode.OK
    )

    val repo = ParkingRepositoryImpl(testClient, json)

    val result = repo.getParkingOccupancy()
    assertEquals(2, result.size)
    result.firstOrNull()?.let { first ->
      assertEquals("Savaanstraat", first.name)
      assertEquals(510, first.totalCapacity)
      assertEquals(261, first.availableCapacity)
      assertEquals(ParkingType.CAR_PARK, first.type)
      assertEquals("24/7", first.openingDescription)
      assertEquals(true, first.lez)
      assertEquals(true, first.open)
      assertEquals(false, first.free)
      assertEquals(3.7234627726667133, first.longitude)
      assertEquals(51.04877362543108, first.latitude)
      assertEquals("https://stad.gent/nl/mobiliteit-openbare-werken/parkeren/parkings-gent/parking-savaanstraat", first.url)
      assertEquals("Mobiliteitsbedrijf Gent", first.operator)
      assertEquals("Tel.: 09 266 29 40", first.phone)
      assertEquals(Instant.parse("2026-02-27T21:37:23+01:00"), first.lastUpdated)
    }

    result.lastOrNull()?.let { last ->
      assertEquals("Tolhuis", last.name)
      assertEquals(3.724968367281895, last.longitude)
      assertEquals(51.0637023559265, last.latitude)
      assertEquals(ParkingType.OFF_STREET, last.type)
    }
  }

  @Test
  fun `getParkingOccupancy returns empty list on server error`() = runTest {
    val repo = ParkingRepositoryImpl(
      createClientWithResponse(
        "{}",
        HttpStatusCode.InternalServerError
      ),
      json
    )
    val result = repo.getParkingOccupancy()
    assertTrue(result.isEmpty())
  }

  @Test
  fun `getParkingOccupancy returns empty list on malformed JSON`() = runTest {
    val repo = ParkingRepositoryImpl(
      createClientWithResponse(
        "not json at all",
        HttpStatusCode.OK
      ),
      json
    )
    val result = repo.getParkingOccupancy()
    assertTrue(result.isEmpty())
  }

  @Test
  fun `getParkingOccupancy returns empty list when results array is empty`() = runTest {
    val repo = ParkingRepositoryImpl(
      createClientWithResponse(
        """{ "total_count": 0, "results": [] }""",
        HttpStatusCode.OK
      ),
      json
    )
    val result = repo.getParkingOccupancy()
    assertTrue(result.isEmpty())
  }

  @Test
  fun `getParkingOccupancy parking outside LEZ maps lez to false`() = runTest {
    val repo = ParkingRepositoryImpl(
      createClientWithResponse(
        parkingJson(category = "parking buiten LEZ"),
        HttpStatusCode.OK
      ),
      json
    )
    val result = repo.getParkingOccupancy()
    assertEquals(false, result.first().lez)
  }

  @Test
  fun `getParkingOccupancy parking inside LEZ maps lez to true`() = runTest {
    val repo = ParkingRepositoryImpl(
      createClientWithResponse(
        parkingJson(category = "parking in LEZ"),
        HttpStatusCode.OK
      ),
      json
    )
    val result = repo.getParkingOccupancy()
    assertEquals(true, result.first().lez)
  }

  @Test
  fun `getParkingOccupancy LEZ check is case insensitive`() = runTest {
    val repo = ParkingRepositoryImpl(
      createClientWithResponse(
        parkingJson(category = "parking BUITEN LEZ"),
        HttpStatusCode.OK
      ),
      json
    )
    val result = repo.getParkingOccupancy()
    assertEquals(false, result.first().lez)
  }


  @Test
  fun `getParkingOccupancy isOpenNow 0 maps open to false`() = runTest {
    val repo = ParkingRepositoryImpl(
      createClientWithResponse(
        parkingJson(isOpenNow = 0),
        HttpStatusCode.OK
      ),
      json
    )
    assertEquals(false, repo.getParkingOccupancy().first().open)
  }

  @Test
  fun `getParkingOccupancy freeParking 1 maps free to true`() = runTest {
    val repo = ParkingRepositoryImpl(
      createClientWithResponse(
        parkingJson(freeParking = 1),
        HttpStatusCode.OK
      ),
      json
    )
    assertEquals(true, repo.getParkingOccupancy().first().free)
  }

  @Test
  fun `getParkingOccupancy offStreetParkingGround type maps correctly`() = runTest {
    val repo = ParkingRepositoryImpl(
      createClientWithResponse(
        parkingJson(type = "offStreetParkingGround"),
        HttpStatusCode.OK
      ),
      json
    )
    assertEquals(ParkingType.OFF_STREET, repo.getParkingOccupancy().first().type)
  }

  @Test
  fun `getParkingOccupancy unknown type maps to null`() = runTest {
    val repo = ParkingRepositoryImpl(
      createClientWithResponse(
        parkingJson(type = "someFutureType"),
        HttpStatusCode.OK
      ),
      json
    )

    assertNull(repo.getParkingOccupancy().first().type)
  }

  @Test
  fun `getParkingOccupancy missing location maps lat and lon to null`() = runTest {
    val repo = ParkingRepositoryImpl(
      createClientWithResponse(
        parkingJson(includeLocation = false),
        HttpStatusCode.OK
      ),
      json
    )
    val result = repo.getParkingOccupancy().first()
    assertNull(result.latitude)
    assertNull(result.longitude)
  }

  @Test
  fun `getParkingOccupancy malformed locationAndDimension does not crash and phone is null`() = runTest {
    val repo = ParkingRepositoryImpl(
      createClientWithResponse(
        parkingJson(locationAndDimension = "}{invalid json"),
        HttpStatusCode.OK
      ),
      json
    )
    val result = repo.getParkingOccupancy().first()
    assertNull(result.phone)
  }

  @Test
  fun `getParkingOccupancy locationAndDimension without phone maps phone to null`() = runTest {
    val dimensionWithoutPhone = """{"specificAccessInformation": ["inrit"], "level": "0", "coordinatesForDisplay": {"latitude": 51.0, "longitude": 3.7}}"""
    val repo = ParkingRepositoryImpl(
      createClientWithResponse(
        parkingJson(locationAndDimension = dimensionWithoutPhone),
        HttpStatusCode.OK
      ),
      json
    )
    assertNull(repo.getParkingOccupancy().first().phone)
  }
}