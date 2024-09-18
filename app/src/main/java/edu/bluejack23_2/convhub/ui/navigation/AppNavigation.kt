package edu.bluejack23_2.convhub.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import edu.bluejack23_2.convhub.ui.screens.home.HomeScreen
import edu.bluejack23_2.convhub.ui.screens.profile.ProfileScreen
import edu.bluejack23_2.convhub.ui.theme.screens.ActiveTaskScreen
import edu.bluejack23_2.convhub.ui.theme.screens.ScheduleScreen
import edu.bluejack23_2.convhub.ui.theme.*

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController : NavHostController = rememberNavController()

    ConvHubTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry : NavBackStackEntry? by navController.currentBackStackEntryAsState()
                    val currentDestination : NavDestination? = navBackStackEntry?.destination

                    listOfNavItems.forEach{ navItem ->
                        val isSelected = currentDestination?.hierarchy?.any {it.route == navItem.route} == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(navItem.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(imageVector = navItem.icon, contentDescription = null, modifier = Modifier.size(32.dp))
                            },
                        )
                    }
                }
            }
        ) {paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screens.HomeScreen.name,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(route = Screens.HomeScreen.name) {
                    HomeScreen()
                }
                composable(route = Screens.ScheduleScreen.name) {
                    ScheduleScreen()
                }
                composable(route = Screens.ActiveTaskScreen.name) {
                    ActiveTaskScreen()
                }
                composable(route = Screens.ProfileScreen.name) {
                    ProfileScreen()
                }
            }
        }
    }

}