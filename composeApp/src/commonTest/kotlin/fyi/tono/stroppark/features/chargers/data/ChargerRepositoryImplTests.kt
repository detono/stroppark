package fyi.tono.stroppark.features.chargers.data

import fyi.tono.stroppark.fakes.FakeChargerDao
import fyi.tono.stroppark.fakes.FakeCrashReporter
import fyi.tono.stroppark.features.core.data.BaseRepositoryImplTests
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
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

  private val OCM_JSON = """
    {
      "total": 2,
      "limit": 200,
      "offset": 0,
      "data": [
        {
          "id": 9082,
          "name": "CIAC Sint Amandsberg",
          "address": "Antwerpsesteenweg 683 , Sint Amandsberg, 9040 ",
          "latitude": 51.0708977695468,
          "longitude": 3.77075008890279,
          "operator": "Blue Corner (Belgium)",
          "usage_cost": null,
          "is_operational": true,
          "number_of_points": 1,
          "connectors": [
            {
              "type_name": "CEE 7/4 - Schuko - Type F",
              "formal_name": "CEE 7/4",
              "power_kw": 3.75,
              "amps": 16,
              "voltage": 230,
              "current_type": "AC (Single-Phase)",
              "is_fast_charge": false,
              "is_operational": true,
              "quantity": 1
            },
            {
              "type_name": "Type 2 (Socket Only)",
              "formal_name": "IEC 62196-2 Type 2",
              "power_kw": 22,
              "amps": 32,
              "voltage": 400,
              "current_type": "AC (Three-Phase)",
              "is_fast_charge": false,
              "is_operational": true,
              "quantity": 1
            }
          ],
          "distance_km": null
        },
        {
          "id": 9128,
          "name": "Restaurant Patyntje",
          "address": "Gordunakaai 91, Gent, 9000",
          "latitude": 51.0415487,
          "longitude": 3.6945205,
          "operator": null,
          "usage_cost": "Free",
          "is_operational": true,
          "number_of_points": 1,
          "connectors": [
            {
              "type_name": "Type 2 (Tethered Connector) ",
              "formal_name": "IEC 62196-2",
              "power_kw": 11,
              "amps": 16,
              "voltage": 380,
              "current_type": "AC (Three-Phase)",
              "is_fast_charge": false,
              "is_operational": true,
              "quantity": 2
            }
          ],
          "distance_km": null
        }
      ]
    }
  """.trimIndent()

  private lateinit var fakeChargerDao: FakeChargerDao
  private val fakeReporter = FakeCrashReporter()
  @BeforeTest
  fun setup() {
    fakeChargerDao = FakeChargerDao()
  }

  @Test
  fun `getChargers returns mapped domain data`() = runTest {
    val client = createClientWithResponse(
      content = CHARGER_JSON,
      statusCode = HttpStatusCode.OK
    )

    val repo = ChargerRepositoryImpl(httpClient = client, dao = fakeChargerDao, crashReporter = fakeReporter)
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

    val repo = ChargerRepositoryImpl(httpClient = client, dao = fakeChargerDao, crashReporter = fakeReporter)
    val result = repo.getChargers()

    assertTrue(result.isEmpty(), "Expected empty list on server error")
  }

  @Test
  fun `refreshStation has inserted correct amount in db`() = runTest {
    val client = createClientWithResponse(
      content = OCM_JSON,
      statusCode = HttpStatusCode.OK
    )

    val repo = ChargerRepositoryImpl(httpClient = client, dao = fakeChargerDao, crashReporter = fakeReporter)
    repo.refreshStations().collect()

    val connectors = fakeChargerDao.getInsertedConnectors()
    val stations = fakeChargerDao.getInsertedStations()

    assertEquals(3, connectors.size)
    assertEquals(2, stations.size)
  }

  @Test
  fun `refreshStations sets lastSyncedAt after successful fetch`() = runTest {
    val client = createClientWithResponse(
      content = OCM_JSON,
      statusCode = HttpStatusCode.OK
    )

    val repo = ChargerRepositoryImpl(httpClient = client, dao = fakeChargerDao, crashReporter = fakeReporter)
    repo.refreshStations().collect()

    assertNotNull(fakeChargerDao.getLastSyncedAt())
  }

  @Test
  fun `refreshStations upserts on subsequent calls`() = runTest {
    val client = createClientWithResponse(
      content = OCM_JSON,
      statusCode = HttpStatusCode.OK
    )

    val repo = ChargerRepositoryImpl(
      httpClient = client,
      dao = fakeChargerDao,
      crashReporter = fakeReporter,
    )

    // first call — full fetch, clears and inserts
    repo.refreshStations().collect()


    assertEquals(2, fakeChargerDao.getInsertedStations().size, "expected 2 stations")
    assertNotNull(fakeChargerDao.getLastSyncedAt())

    // second call — delta fetch, should still have same data
    repo.refreshStations().collect()

    assertEquals(2, fakeChargerDao.getInsertedStations().size, "still expecting 2 stations")
  }

  @Test
  fun `refreshStation has mapped correctly`() = runTest {
    val client = createClientWithResponse(
      content = OCM_JSON,
      statusCode = HttpStatusCode.OK
    )

    val repo = ChargerRepositoryImpl(httpClient = client, dao = fakeChargerDao, crashReporter = fakeReporter)
    repo.refreshStations().collect()

    val connectors = fakeChargerDao.getInsertedConnectors()
    val stations = fakeChargerDao.getInsertedStations()

    val amandsBergStation = stations.firstOrNull { it.id == 9082L }
    val amandsBergConnectors = connectors.filter { it.stationId == 9082L }

    val patyntjeStation = stations.firstOrNull { it.id == 9128L }
    val patyntjeConnectors = connectors.filter { it.stationId == 9128L }

    assertEquals(
      "CIAC Sint Amandsberg",
      amandsBergStation?.name,
    )
    assertEquals(
      "Antwerpsesteenweg 683 , Sint Amandsberg, 9040 ",
      amandsBergStation?.address,
    )
    assertEquals(
      51.0708977695468,
      amandsBergStation?.latitude,
    )
    assertEquals(
      3.77075008890279,
      amandsBergStation?.longitude,
    )
    assertEquals(
      "Blue Corner (Belgium)",
      amandsBergStation?.operator,
    )
    assertEquals(
      null,
      amandsBergStation?.usageCost,
    )
    assertEquals(
      true,
      amandsBergStation?.isOperational,
      "isOperational not matching"
    )
    assertEquals(
      1,
      amandsBergStation?.numberOfPoints,
    )
    assertEquals(
      2,
      amandsBergConnectors.size,
    )
    assertEquals(
      "CEE 7/4 - Schuko - Type F",
      amandsBergConnectors.firstOrNull()?.typeName,
    )
    assertEquals(
      "CEE 7/4",
      amandsBergConnectors.firstOrNull()?.formalName,
    )
    assertEquals(
      3.75,
      amandsBergConnectors.firstOrNull()?.powerKw,
    )
    assertEquals(
      true,
      amandsBergConnectors.firstOrNull()?.isOperational,
      "isOperation not matching in connector"
    )
    assertEquals(
      16.0,
      amandsBergConnectors.firstOrNull()?.amps,
    )
    assertEquals(
      230.0,
      amandsBergConnectors.firstOrNull()?.voltage,
    )
    assertEquals(
      "AC (Single-Phase)",
      amandsBergConnectors.firstOrNull()?.currentType,
    )
    assertEquals(
      false,
      amandsBergConnectors.firstOrNull()?.isFastCharge,
      "isFastCharge not matching"
    )
    assertEquals(
      1,
      amandsBergConnectors.firstOrNull()?.quantity,
    )

    //Don't need to recheck all the fields because if above is OK
    //this one will be as well (same for 2nd connector)
    assertNotNull(patyntjeStation)
    assertEquals(1, patyntjeConnectors.size)
  }

  @Test
  fun `getStationsWithConnectors emits correctly`() = runTest {
    val client = createClientWithResponse(
      content = OCM_JSON,
      statusCode = HttpStatusCode.OK
    )

    val repo = ChargerRepositoryImpl(httpClient = client, dao = fakeChargerDao, crashReporter = fakeReporter)
    repo.refreshStations().collect()

    val stationsWithConnectors = fakeChargerDao.getStationsWithConnectors().first()

    assertEquals(
      2,
      stationsWithConnectors.size,
      "expected 2 stations with connectors"
    )
    assertEquals(
      "CIAC Sint Amandsberg",
      stationsWithConnectors.firstOrNull()?.station?.name
    )
    assertEquals(
      2,
      stationsWithConnectors.firstOrNull()?.connectors?.size,
      "expected 2 connectors"
    )
    assertEquals(
      1,
      stationsWithConnectors.lastOrNull()?.connectors?.size,
      "expected 1 connector"
    )
  }
}