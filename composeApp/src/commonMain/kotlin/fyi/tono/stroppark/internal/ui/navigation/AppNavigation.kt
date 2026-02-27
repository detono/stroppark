package fyi.tono.stroppark.internal.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppNavigation(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val navController = rememberNavController()

  Scaffold(
    bottomBar = {
      NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val items = listOf(
          NavItem.Parking,
          NavItem.Chargers
        )

        items.forEach { item ->
          NavigationBarItem(
            icon = { Icon(item.icon, contentDescription = null) },
            label = { Text(item.title) },
            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
            onClick = {
              navController.navigate(item.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                  saveState = true
                }
                launchSingleTop = true
                restoreState = true
              }
            }
          )
        }
      }
    },
    content = { innerPadding ->
      NavHost(
        navController = navController,
        startDestination = NavItem.Parking.route,
        modifier = modifier.padding(innerPadding)
      ) {
        composable(NavItem.Parking.route) {
          ParkingListScreen(viewModel)
        }
        composable(NavItem.Chargers.route) {
          ChargerListScreen(viewModel)
        }
      }
    }
  )
}