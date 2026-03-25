package fyi.tono.stroppark.core.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fyi.tono.stroppark.core.utils.LocalSnackbar
import fyi.tono.stroppark.features.chargers.ui.screens.ChargerListScreen
import fyi.tono.stroppark.features.parking.ui.screens.ParkingListScreen


@Composable
fun AppNavigation(
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController()
) {
  val snackBarHostState = remember { SnackbarHostState() }

  CompositionLocalProvider(value = LocalSnackbar provides snackBarHostState) {
    Scaffold(
      snackbarHost = {
        SnackbarHost(
          hostState = snackBarHostState
        )
      },
      bottomBar = {
        StropParkBottomBar(navController = navController)
      },
      content = { innerPadding ->
        NavHost(
          navController = navController,
          startDestination = NavItem.Parking.route,
          modifier = modifier.padding(innerPadding),
          builder = {
            composable(NavItem.Parking.route) {
              ParkingListScreen()
            }
            composable(NavItem.Chargers.route) {
              ChargerListScreen()
            }
          }
        )
      }
    )
  }
}