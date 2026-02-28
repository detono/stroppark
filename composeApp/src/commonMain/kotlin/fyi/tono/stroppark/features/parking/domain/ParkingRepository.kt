package fyi.tono.stroppark.features.parking.domain

interface ParkingRepository {
  suspend fun getParkingOccupancy(): List<ParkingLocation>
}