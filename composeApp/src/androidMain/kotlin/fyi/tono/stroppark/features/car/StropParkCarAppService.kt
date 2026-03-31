package fyi.tono.stroppark.features.car

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

import fyi.tono.stroppark.R

class StropParkCarAppService : CarAppService() {
  override fun createHostValidator(): HostValidator {
    return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR // dev only!
    /*return HostValidator.Builder(applicationContext)
         .addAllowedHosts(R.array.hosts_allowlist)
         .build()*/
  }

  override fun onCreateSession(): Session = StropParkSession()
}