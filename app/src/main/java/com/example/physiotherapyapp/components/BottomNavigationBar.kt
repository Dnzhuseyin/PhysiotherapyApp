package com.example.physiotherapyapp.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.physiotherapyapp.navigation.NavigationRoutes

/**
 * Bottom Navigation Bar component
 */
@Composable
fun BottomNavigationBar(
    navController: NavController
) {
    val items = listOf(
        BottomNavItem(
            title = "Ana Sayfa",
            icon = Icons.Default.Home,
            route = NavigationRoutes.MAIN_HOME
        ),
        BottomNavItem(
            title = "Egzersiz Yap",
            icon = Icons.Default.FitnessCenter,
            route = NavigationRoutes.MAIN_WORKOUT
        ),
        BottomNavItem(
            title = "Geçmiş",
            icon = Icons.Default.History,
            route = NavigationRoutes.MAIN_HISTORY
        ),
        BottomNavItem(
            title = "Profil",
            icon = Icons.Default.Person,
            route = NavigationRoutes.MAIN_PROFILE
        )
    )
    
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
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
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Bottom navigation'da geri dönüş yığınını temizle
                            popUpTo(NavigationRoutes.MAIN_HOME) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

/**
 * Bottom navigation item data class
 */
data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) 