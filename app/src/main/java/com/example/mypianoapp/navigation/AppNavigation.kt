package com.example.mypianoapp.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mypianoapp.components.BadgeUnlockOverlay
import com.example.mypianoapp.components.BottomBar
import com.example.mypianoapp.components.LevelUpOverlay
import com.example.mypianoapp.screens.*
import com.example.mypianoapp.viewmodel.UserProgressViewModel

@Composable
fun AppNavigation() {
    val viewModel: UserProgressViewModel = viewModel()
    val progress        by viewModel.progress.collectAsStateWithLifecycle()
    val onboardingDone  by viewModel.onboardingDone.collectAsStateWithLifecycle()
    val levelUpEvent    by viewModel.levelUpEvent.collectAsStateWithLifecycle()
    val newBadges       by viewModel.newBadges.collectAsStateWithLifecycle()

    // ── Phase de démarrage : splash → onboarding → app ───────────────
    var splashDone by remember { mutableStateOf(value = false) }

    AnimatedContent(
        targetState  = Triple(splashDone, onboardingDone, progress.userName),
        transitionSpec = {
            fadeIn(tween(400)) togetherWith fadeOut(tween(300))
        },
        label = "app_phase",
    ) { (splash, onboarded, _) ->
        when {
            // 1. Splash
            !splash -> SplashScreen { splashDone = true }

            // 2. Onboarding (premier lancement)
            !onboarded -> OnboardingScreen(
                onComplete = { name, goal ->
                    viewModel.completeOnboarding(name, goal)
                }
            )

            // 3. App principale
            else -> MainApp(
                viewModel    = viewModel,
                levelUpEvent = levelUpEvent,
                newBadges    = newBadges
            )
        }
    }
}

// ── App principale ────────────────────────────────────────────────────────

@Composable
private fun MainApp(
    viewModel: UserProgressViewModel,
    levelUpEvent: Int?,
    newBadges: List<com.example.mypianoapp.data.Badge>
) {
    val navController = rememberNavController()
    val currentRoute  by navController.currentBackStackEntryAsState()
    val progress      by viewModel.progress.collectAsStateWithLifecycle()

    val hideBottomBar = listOf("lesson_detail/", "exercise_detail/", "goblin_game")
        .any { currentRoute?.destination?.route?.startsWith(it) == true }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = { if (!hideBottomBar) BottomBar(navController) }
        ) { innerPadding ->
            NavHost(
                navController    = navController,
                startDestination = Screen.Home.route,
                modifier         = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(progress = progress, onNavigate = { navController.navigate(it) })
                }
                composable(Screen.Lessons.route) {
                    LessonScreen(
                        progress      = progress,
                        onLessonClick = { id -> navController.navigate("lesson_detail/$id") }
                    )
                }
                composable(Screen.Piano.route) {
                    PianoScreen(onMinutesPlayed = { viewModel.addPracticeMinutes(it) })
                }
                composable(Screen.Exercises.route) {
                    ExerciseScreen(
                        progress        = progress,
                        onExerciseClick = { id -> navController.navigate("exercise_detail/$id") }
                    )
                }
                composable("goblin_game") {
                    GoblinGameScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        progress        = progress,
                        onResetProgress = { viewModel.resetProgress() }
                    )
                }
                composable(
                    route     = "lesson_detail/{lessonId}",
                    arguments = listOf(navArgument("lessonId") { type = NavType.IntType })
                ) { back ->
                    LessonDetailScreen(
                        lessonId         = back.arguments?.getInt("lessonId") ?: 1,
                        progress         = progress,
                        onLessonComplete = { id, xp -> viewModel.completeLesson(id, xp) },
                        onQuizPassed     = { id -> viewModel.passQuiz(id) },
                        onBack           = { navController.popBackStack() }
                    )
                }
                composable(
                    route     = "exercise_detail/{exerciseId}",
                    arguments = listOf(navArgument("exerciseId") { type = NavType.IntType })
                ) { back ->
                    ExerciseDetailScreen(
                        exerciseId = back.arguments?.getInt("exerciseId") ?: 1,
                        onComplete = { xpEarned ->
                            viewModel.completeExercise(xpEarned)
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // ── Overlays gamification ─────────────────────────────────────
        levelUpEvent?.let { level ->
            LevelUpOverlay(newLevel = level, onDismiss = { viewModel.dismissLevelUp() })
        }
        newBadges.firstOrNull()?.let { badge ->
            BadgeUnlockOverlay(badge = badge, onDismiss = { viewModel.dismissBadge(badge.id) })
        }
    }
}
