package fyi.tono.stroppark.features.car.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Tab
import androidx.car.app.model.TabContents
import androidx.car.app.model.TabTemplate
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import fyi.tono.stroppark.R
import fyi.tono.stroppark.features.chargers.domain.ChargerRepository
import fyi.tono.stroppark.features.chargers.ui.ChargerUiModel
import fyi.tono.stroppark.features.chargers.ui.toUiModel
import fyi.tono.stroppark.features.parking.domain.ParkingLocation
import fyi.tono.stroppark.features.parking.domain.ParkingRepository
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform


class StropParkScreen(
  carContext: CarContext,
  private val parkingRepo: ParkingRepository = KoinPlatform.getKoin().get(),
  private val chargerRepo: ChargerRepository = KoinPlatform.getKoin().get(),
) : Screen(carContext) {

  companion object {
    private const val TAB_PARKING = "parking"
    private const val TAB_CHARGERS = "chargers"
    private const val ALMOST_FULL_THRESHOLD = 0.8f
  }

  private var activeTab = TAB_PARKING
  private var parking: List<ParkingLocation> = emptyList()
  private var chargers: List<ChargerUiModel> = emptyList()
  private var isLoadingParking = true
  private var isLoadingChargers = true

  init {
    lifecycleScope.launch {
      parkingRepo.getParkingFlow().collect {
        parking = it
        isLoadingParking = false
        invalidate()
      }
    }
    lifecycleScope.launch {
      chargerRepo.getStationFlow().collect {
        chargers = it.map { s -> s.toUiModel() }
        isLoadingChargers = false
        invalidate()
      }
    }
  }

  override fun onGetTemplate(): Template {
    val parkingTab = Tab.Builder()
      .setTitle(carContext.getString(R.string.parking_title))
      .setIcon(carIcon(R.drawable.ic_local_parking))
      .setContentId(TAB_PARKING)
      .build()

    val chargersTab = Tab.Builder()
      .setTitle(carContext.getString(R.string.charger_title))
      .setIcon(carIcon(R.drawable.ic_ev_station))
      .setContentId(TAB_CHARGERS)
      .build()

    val contents = TabContents.Builder(
      when (activeTab) {
        TAB_CHARGERS -> buildChargerList()
        else -> buildParkingList()
      }
    ).build()

    return TabTemplate.Builder(object : TabTemplate.TabCallback {
      override fun onTabSelected(tabContentId: String) {
        activeTab = tabContentId
        invalidate()
      }
    })
      .setHeaderAction(Action.APP_ICON)
      .addTab(parkingTab)
      .addTab(chargersTab)
      .setTabContents(contents)
      .setActiveTabContentId(activeTab)
      .build()
  }

  private fun buildParkingList(): ListTemplate {
    if (isLoadingParking) return ListTemplate.Builder().setLoading(true).build()

    val itemList = ItemList.Builder().apply {
      parking.take(6).forEach { p ->
        val occupancyIcon = when {
          p.availableCapacity == 0 -> carIcon(R.drawable.ic_block)
          p.occupancyProgress >= ALMOST_FULL_THRESHOLD -> carIcon(R.drawable.ic_warning)
          else -> carIcon(R.drawable.ic_check_circle)
        }
        addItem(
          Row.Builder()
            .setTitle(p.name)
            .addText("${p.availableCapacity}/${p.totalCapacity} available")
            .addText(p.distanceKm?.let { "%.1fkm".format(it) } ?: "Calculating...")
            .setImage(occupancyIcon, Row.IMAGE_TYPE_ICON)
            .setOnClickListener {
              if (p.hasCoordinates) {
                try {
                  val uri = "geo:${p.latitude},${p.longitude}?q=${p.latitude},${p.longitude}(${Uri.encode(p.name)})".toUri()
                  carContext.startCarApp(Intent(CarContext.ACTION_NAVIGATE, uri))
                } catch (_: ActivityNotFoundException) { }
              }
            }
            .build()
        )
      }
    }.build()

    return ListTemplate.Builder()
      .setHeader(Header.Builder().setTitle(carContext.getString(R.string.parking_nearby)).build())
      .setSingleList(itemList)
      .build()
  }

  private fun buildChargerList(): ListTemplate {
    if (isLoadingChargers) return ListTemplate.Builder().setLoading(true).build()

    val itemList = ItemList.Builder().apply {
      chargers.take(6).forEach { c ->
        val statusIcon = if (c.isOperational) carIcon(R.drawable.ic_check_circle)
        else carIcon(R.drawable.ic_block)
        addItem(
          Row.Builder()
            .setTitle(c.name)
            .addText(buildString {
              c.fastestChargerKw?.let { append("%.0fkW • ".format(it)) }
              append(c.connectorSummary)
            })
            .addText(c.distanceKm?.let { "%.1fkm".format(it) } ?: "Calculating...")
            .setImage(statusIcon, Row.IMAGE_TYPE_ICON)
            .setOnClickListener {
              if (c.hasCoordinates) {
                try {
                  val uri = "geo:${c.latitude},${c.longitude}?q=${c.latitude},${c.longitude}(${c.name})".toUri()
                  carContext.startCarApp(
                    Intent(CarContext.ACTION_NAVIGATE, uri)
                  )
                } catch (_: ActivityNotFoundException) { }
              }
            }
            .build()
        )
      }
    }.build()

    return ListTemplate.Builder()
      .setHeader(Header.Builder().setTitle(carContext.getString(R.string.charger_nearby)).build())
      .setSingleList(itemList)
      .build()
  }

  private fun carIcon(res: Int) = CarIcon.Builder(
    IconCompat.createWithResource(carContext, res)
  ).build()
}