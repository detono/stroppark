package fyi.tono.stroppark.core.location


actual fun getGeoUri(lat: Double, lng: Double): String {
  return "https://maps.apple.com/?q=$lat,$lng"
}