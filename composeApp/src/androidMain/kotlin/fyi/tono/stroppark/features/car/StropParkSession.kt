package fyi.tono.stroppark.features.car

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import fyi.tono.stroppark.features.car.ui.screens.StropParkScreen

class StropParkSession : Session() {

  override fun onCreateScreen(intent: Intent): Screen {
    return StropParkScreen(carContext)
  }
}