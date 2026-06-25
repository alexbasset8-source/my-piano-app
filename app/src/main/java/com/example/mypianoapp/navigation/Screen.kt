package com.example.mypianoapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {

    object Home : Screen(
        "home",
        "Accueil",
        Icons.Default.Home
    )

    object Lessons : Screen(
        "lessons",
        "Leçons",
        Icons.Default.LibraryMusic
    )

    object Piano : Screen(
        "piano",
        "Piano",
        Icons.Default.MusicNote
    )

    object Exercises : Screen(
        "exercises",
        "Exercices",
        Icons.Default.SportsEsports
    )

    object Profile : Screen(
        "profile",
        "Profil",
        Icons.Default.Person
    )
}