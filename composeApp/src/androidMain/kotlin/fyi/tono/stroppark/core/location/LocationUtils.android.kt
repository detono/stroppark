package fyi.tono.stroppark.core.location


actual fun getGeoUri (lat: Double, lng: Double) = "geo:$lat,$lng?q=$lat,$lng"