package com.example.mypianoapp.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mypianoapp.navigation.Screen

@Composable
fun BottomBar(
    navController: NavController
) {

    val screens = listOf(
        Screen.Home,
        Screen.Lessons,
        Screen.Piano,
        Screen.Exercises,
        Screen.Profile
    )

    val currentRoute =
        navController.currentBackStackEntryAsState()
            .value
            ?.destination
            ?.route

    NavigationBar {

        screens.forEach { screen ->

            NavigationBarItem(

                selected = currentRoute == screen.route,

                onClick = {

                    navController.navigate(screen.route) {

                        popUpTo(
                            navController.graph.startDestinationId
                        )

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

                    Text(screen.title)

                }

            )
        }
    }
}