package fyi.tono.stroppark.core.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun StropParkBottomBar(
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController(),
) {
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
}