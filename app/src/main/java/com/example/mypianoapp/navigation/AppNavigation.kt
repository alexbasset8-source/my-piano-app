package com.example.mypianoapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mypianoapp.components.BottomBar
import com.example.mypianoapp.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentRoute by navController.currentBackStackEntryAsState()
    val showBottomBar = currentRoute?.destination?.route?.startsWith("lesson_detail") == false

    Scaffold(
        bottomBar = {
            if (showBottomBar) BottomBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route)      { HomeScreen() }

            composable(Screen.Lessons.route)   {
                LessonScreen(
                    onLessonClick = { id ->
                        navController.navigate("lesson_detail/$id")
                    }
                )
            }

            composable(Screen.Piano.route)     { PianoScreen() }
            composable(Screen.Exercises.route) { ExerciseScreen() }
            composable(Screen.Profile.route)   { ProfileScreen() }

            composable(
                route = "lesson_detail/{lessonId}",
                arguments = listOf(navArgument("lessonId") { type = NavType.IntType })
            ) { backStackEntry ->
                val lessonId = backStackEntry.arguments?.getInt("lessonId") ?: 1
                LessonDetailScreen(
                    lessonId = lessonId,
                    onBack   = { navController.popBackStack() }
                )
            }
        }
    }
}
