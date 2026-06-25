package com.example.mypianoapp.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mypianoapp.navigation.Screen
import com.example.mypianoapp.ui.theme.*

@Composable
fun BottomBar(navController: NavController) {
    val screens = listOf(
        Screen.Home,
        Screen.Lessons,
        Screen.Piano,
        Screen.Exercises,
        Screen.Profile
    )
    val currentRoute = navController
        .currentBackStackEntryAsState()
        .value?.destination?.route

    NavigationBar(
        containerColor = EbonyCard,
        tonalElevation = 0.dp
    ) {
        screens.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = {
                    Text(
                        screen.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = KeysVioletLight,
                    selectedTextColor = KeysVioletLight,
                    indicatorColor = KeysVioletGlow,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}
