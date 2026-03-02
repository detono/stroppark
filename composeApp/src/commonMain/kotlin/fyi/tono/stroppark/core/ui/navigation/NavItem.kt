package fyi.tono.stroppark.core.ui.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Garage
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector


sealed class NavItem(
  val route: String,
  val title: String,
  val icon: ImageVector
) {
  object Parking : NavItem(
    route = "parking",
    title = "Car Parks",
    icon = Icons.Filled.LocalParking
  )

  object Chargers : NavItem(
    route = "chargers",
    title = "Chargers",
    icon = Icons.Filled.ElectricalServices
  )
}
