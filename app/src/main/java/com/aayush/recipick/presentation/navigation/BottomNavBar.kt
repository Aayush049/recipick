package com.aayush.recipick.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

sealed class BottomNavItem(
    val route: Any,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val routeName: String
) {
    object Home : BottomNavItem(HomeRoute, "Explore", Icons.Default.Explore, "HomeRoute")
    object MyRecipes : BottomNavItem(MyRecipesRoute, "My Recipes", Icons.AutoMirrored.Filled.MenuBook, "MyRecipesRoute")
    object Planner : BottomNavItem(PlannerRoute, "Planner", Icons.Default.CalendarMonth, "PlannerRoute")
    object Settings : BottomNavItem(SettingsRoute, "Settings", Icons.Default.Settings, "SettingsRoute")
}

fun NavHostController.navigateToTopLevelDestination(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = (route != HomeRoute)
    }
}

@Composable
fun BottomNavBar(
    navController: NavHostController,
    currentDestination: NavDestination?
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.MyRecipes,
        BottomNavItem.Planner,
        BottomNavItem.Settings
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { destination ->
                destination.route?.contains(item.routeName) == true
            } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = if (selected) 12.sp else 11.sp
                    )
                },
                selected = selected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                ),
                onClick = {
                    if (!selected) {
                        navController.navigateToTopLevelDestination(item.route)
                    }
                }
            )
        }
    }
}
