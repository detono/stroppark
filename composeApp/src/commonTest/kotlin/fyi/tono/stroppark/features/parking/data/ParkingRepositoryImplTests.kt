package fyi.tono.stroppark.features.parking.data

import fyi.tono.stroppark.fakes.FakeParkingDao
import fyi.tono.stroppark.features.core.data.BaseRepositoryImplTests
import fyi.tono.stroppark.features.parking.domain.ParkingType
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


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

  private lateinit var fakeParkingDao: FakeParkingDao
  @BeforeTest
  fun setup() {
    fakeParkingDao = FakeParkingDao()
  }

  @Test
  fun `getParkingOccupancy maps correctly on success`() = runTest {
    val testClient = createClientWithResponse(GHENT_PARKING_JSON, HttpStatusCode.OK)
    val repo = ParkingRepositoryImpl(testClient, json, dao = fakeParkingDao)

    repo.refreshParkingOccupancy()

    val results = fakeParkingDao.getInsertedLocations()
    assertEquals(2, results.size)

    results.firstOrNull()?.let { first ->
      assertEquals("Savaanstraat", first.name)
      assertEquals(510, first.totalCapacity)
      assertEquals(261, first.availableCapacity)
      assertEquals(ParkingType.CAR_PARK.type, first.type)
      assertEquals("24/7", first.openingDescription)
      assertEquals(true, first.lez)
      assertEquals(true, first.open)
      assertEquals(false, first.free)
      assertEquals(3.7234627726667133, first.lon)
      assertEquals(51.04877362543108, first.lat)
      assertEquals("Mobiliteitsbedrijf Gent", first.operator)
    }

    results.lastOrNull()?.let { last ->
      assertEquals("Tolhuis", last.name)
      assertEquals(3.724968367281895, last.lon)
      assertEquals(51.0637023559265, last.lat)
      assertEquals(ParkingType.OFF_STREET.type, last.type)
    }
  }

  @Test
  fun `refreshParkingOccupancy returns failure on server error`() = runTest {
    val repo = ParkingRepositoryImpl(
      createClientWithResponse("{}", HttpStatusCode.InternalServerError),
      json, dao = fakeParkingDao
    )
    val result = repo.refreshParkingOccupancy()
    assertTrue(result.isFailure)
  }

  @Test
  fun `refreshParkingOccupancy returns failure on malformed JSON`() = runTest {
    val repo = ParkingRepositoryImpl(
      createClientWithResponse("not json at all", HttpStatusCode.OK),
      json, dao = fakeParkingDao
    )
    val result = repo.refreshParkingOccupancy()
    assertTrue(result.isFailure)
  }

  @Test
  fun `refreshParkingOccupancy with empty results inserts nothing`() = runTest {
    val repo = ParkingRepositoryImpl(
      createClientWithResponse("""{ "total_count": 0, "results": [] }""", HttpStatusCode.OK),
      json, dao = fakeParkingDao
    )
    repo.refreshParkingOccupancy()
    assertTrue(fakeParkingDao.getInsertedLocations().isEmpty())
  }

  @Test
  fun `parking outside LEZ maps lez to false`() = runTest {
    val repo = ParkingRepositoryImpl(createClientWithResponse(parkingJson(category = "parking buiten LEZ"), HttpStatusCode.OK), json, dao = fakeParkingDao)
    repo.refreshParkingOccupancy()
    assertEquals(false, fakeParkingDao.getInsertedLocations().first().lez)
  }

  @Test
  fun `parking inside LEZ maps lez to true`() = runTest {
    val repo = ParkingRepositoryImpl(createClientWithResponse(parkingJson(category = "parking in LEZ"), HttpStatusCode.OK), json, dao = fakeParkingDao)
    repo.refreshParkingOccupancy()
    assertEquals(true, fakeParkingDao.getInsertedLocations().first().lez)
  }

  @Test
  fun `LEZ check is case insensitive`() = runTest {
    val repo = ParkingRepositoryImpl(createClientWithResponse(parkingJson(category = "parking BUITEN LEZ"), HttpStatusCode.OK), json, dao = fakeParkingDao)
    repo.refreshParkingOccupancy()
    assertEquals(false, fakeParkingDao.getInsertedLocations().first().lez)
  }

  @Test
  fun `isOpenNow 0 maps open to false`() = runTest {
    val repo = ParkingRepositoryImpl(createClientWithResponse(parkingJson(isOpenNow = 0), HttpStatusCode.OK), json, dao = fakeParkingDao)
    repo.refreshParkingOccupancy()
    assertEquals(false, fakeParkingDao.getInsertedLocations().first().open)
  }

  @Test
  fun `freeParking 1 maps free to true`() = runTest {
    val repo = ParkingRepositoryImpl(createClientWithResponse(parkingJson(freeParking = 1), HttpStatusCode.OK), json, dao = fakeParkingDao)
    repo.refreshParkingOccupancy()
    assertEquals(true, fakeParkingDao.getInsertedLocations().first().free)
  }

  @Test
  fun `offStreetParkingGround type maps correctly`() = runTest {
    val repo = ParkingRepositoryImpl(createClientWithResponse(parkingJson(type = "offStreetParkingGround"), HttpStatusCode.OK), json, dao = fakeParkingDao)
    repo.refreshParkingOccupancy()
    assertEquals(ParkingType.OFF_STREET.type, fakeParkingDao.getInsertedLocations().first().type)
  }

  @Test
  fun `unknown type maps to null`() = runTest {
    val repo = ParkingRepositoryImpl(createClientWithResponse(parkingJson(type = "someFutureType"), HttpStatusCode.OK), json, dao = fakeParkingDao)
    repo.refreshParkingOccupancy()
    assertNull(fakeParkingDao.getInsertedLocations().first().type)
  }

  @Test
  fun `missing location block maps lat and lon to null`() = runTest {
    val repo = ParkingRepositoryImpl(createClientWithResponse(parkingJson(includeLocation = false), HttpStatusCode.OK), json, dao = fakeParkingDao)
    repo.refreshParkingOccupancy()
    val result = fakeParkingDao.getInsertedLocations().first()
    assertNull(result.lat)
    assertNull(result.lon)
  }

  @Test
  fun `getParkingFlow emits domain models from dao`() = runTest {
    val testClient = createClientWithResponse(GHENT_PARKING_JSON, HttpStatusCode.OK)
    val repo = ParkingRepositoryImpl(testClient, json, dao = fakeParkingDao)

    repo.refreshParkingOccupancy()

    val emitted = repo.getParkingFlow().first()
    assertEquals(2, emitted.size)
    assertEquals("Savaanstraat", emitted.first().name)
  }
}