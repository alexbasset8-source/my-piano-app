package com.example.mypianoapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mypianoapp.components.BottomBar
import com.example.mypianoapp.screens.ExerciseScreen
import com.example.mypianoapp.screens.HomeScreen
import com.example.mypianoapp.screens.LessonScreen
import com.example.mypianoapp.screens.PianoScreen
import com.example.mypianoapp.screens.ProfileScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    Scaffold(

        bottomBar = {

            BottomBar(navController)

        }

    ) { innerPadding ->

        NavHost(

            navController = navController,

            startDestination = Screen.Home.route,

            modifier = Modifier.padding(innerPadding)

        ) {

            composable(Screen.Home.route) {

                HomeScreen()

            }

            composable(Screen.Lessons.route) {

                LessonScreen()

            }

            composable(Screen.Piano.route) {

                PianoScreen()

            }

            composable(Screen.Exercises.route) {

                ExerciseScreen()

            }

            composable(Screen.Profile.route) {

                ProfileScreen()

            }
        }
    }
}