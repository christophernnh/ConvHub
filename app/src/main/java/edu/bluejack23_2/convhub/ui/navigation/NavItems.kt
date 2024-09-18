package edu.bluejack23_2.convhub.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val label : String,
    val icon: ImageVector,
    val route: String
)

val listOfNavItems : List<NavItem> = listOf(
    NavItem(
        label = "Home",
        icon = Icons.Outlined.Home,
        route = Screens.HomeScreen.name
    ),
    NavItem(
        label = "Schedule",
        icon = Icons.Outlined.DateRange,
        route = Screens.ScheduleScreen.name
    ),
    NavItem(
        label = "Active Task",
        icon = Icons.Outlined.Check,
        route = Screens.ActiveTaskScreen.name
    ),
    NavItem(
        label = "Profile",
        icon = Icons.Outlined.Person,
        route = Screens.ProfileScreen.name
    )
)
