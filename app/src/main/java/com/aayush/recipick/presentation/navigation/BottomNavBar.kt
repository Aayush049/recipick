package com.aayush.recipick.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

sealed class BottomNavItem(
    val route: Any,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : BottomNavItem(HomeRoute, "Explore", Icons.Default.Explore)
    object MyRecipes : BottomNavItem(MyRecipesRoute, "My Recipes", Icons.AutoMirrored.Filled.MenuBook)
    object Settings : BottomNavItem(SettingsRoute, "Settings", Icons.Default.Settings)
}

@Composable
fun BottomNavBar(
    navController: NavHostController,
    currentRoute: Any?
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.MyRecipes,
        BottomNavItem.Settings
    )

    NavigationBar {
        items.forEach { item ->
            val selected = currentRoute?.javaClass?.name == item.route::class.java.name

            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
