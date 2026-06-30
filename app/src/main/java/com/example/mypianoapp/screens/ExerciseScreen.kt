package com.example.mypianoapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.data.ExerciseCatalog
import com.example.mypianoapp.data.ExerciseData
import com.example.mypianoapp.data.UserProgress
import com.example.mypianoapp.ui.theme.*

@Composable
fun ExerciseScreen(
    progress: UserProgress,
    onExerciseClick: (Int) -> Unit,
) {
    val exercises = ExerciseCatalog.exercises
    val unlockedCount = exercises.count { progress.completedLessonIds.size >= it.minLessonsRequired }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EbonyDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Text("Exercices", style = MaterialTheme.typography.displaySmall, color = TextPrimary)
        Text(
            "$unlockedCount / ${exercises.size} débloqués",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(4.dp))

        exercises.forEach { exercise ->
            val unlocked = progress.completedLessonIds.size >= exercise.minLessonsRequired
            var visible by remember { mutableStateOf(value = false) }
            LaunchedEffect(Unit) { visible = true }

            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
            ) {
                ExerciseCard(
                    exercise = exercise,
                    unlocked = unlocked,
                    onClick  = { if (unlocked) onExerciseClick(exercise.id) },
                )
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: ExerciseData,
    unlocked: Boolean,
    onClick: () -> Unit
) {
    val alpha    = if (unlocked) 1f else 0.38f
    val accent   = Color(exercise.accentColorHex)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(EbonyCard)
            .border(
                1.dp,
                if (unlocked) accent.copy(0.25f) else EbonyBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = unlocked, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icône
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.12f * alpha))
                .border(1.dp, accent.copy(0.25f * alpha), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (unlocked) {
                Text(exercise.emoji, style = MaterialTheme.typography.titleLarge)
            } else {
                Icon(Icons.Default.Lock, null, tint = TextMuted, modifier = Modifier.size(20.dp))
            }
        }

        // Texte
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                exercise.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary.copy(alpha = alpha)
            )
            Text(
                exercise.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary.copy(alpha = alpha)
            )
            if (!unlocked) {
                Text(
                    "Requiert ${exercise.minLessonsRequired} leçon(s) complétée(s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        // Badge XP + flèche
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(0.15f * alpha))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "+${exercise.xpReward} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = accent.copy(alpha = alpha)
                )
            }
            if (unlocked) {
                Icon(
                    Icons.Default.PlayArrow,
                    null,
                    tint = accent.copy(alpha),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
