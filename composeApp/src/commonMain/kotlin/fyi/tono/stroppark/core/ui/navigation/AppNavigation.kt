package fyi.tono.stroppark.core.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fyi.tono.stroppark.features.chargers.ui.screens.ChargerListScreen
import fyi.tono.stroppark.features.parking.ui.screens.ParkingListScreen


@Composable
fun AppNavigation(
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController()
) {

  Scaffold(
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