package fyi.tono.stroppark.features.map.domain

import eu.buney.maps.LatLng
import eu.buney.maps.utils.clustering.ClusterItem

data class MapMarker(
  val id: String,
  private val poiTitle: String,
  val latitude: Double,
  val longitude: Double,
  val type: PoiType
) : ClusterItem {
  override val position: LatLng
    get() = LatLng(latitude, longitude)

  override val title: String
    get() = poiTitle

  override val snippet: String?
    get() = null

  override val zIndex: Float?
    get() = null
}