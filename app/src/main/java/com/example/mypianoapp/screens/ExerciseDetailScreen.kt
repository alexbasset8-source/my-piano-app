package com.example.mypianoapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.data.ExerciseCatalog
import com.example.mypianoapp.data.ExerciseType
import com.example.mypianoapp.screens.exercises.NoteRecognitionExercise
import com.example.mypianoapp.screens.exercises.RhythmTapExercise
import com.example.mypianoapp.screens.exercises.ScaleGuidedExercise
import com.example.mypianoapp.ui.theme.*

@Composable
fun ExerciseDetailScreen(
    exerciseId: Int,
    onComplete: (xpEarned: Int) -> Unit,
    onBack: () -> Unit
) {
    val exercise = ExerciseCatalog.exercises.firstOrNull { it.id == exerciseId } ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EbonyDeep)
    ) {
        // ── Top bar ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EbonyCard)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(EbonySurface)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(exercise.description, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(IvoryGoldGlow)
                    .border(1.dp, IvoryGold.copy(0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text("+${exercise.xpReward} XP", style = MaterialTheme.typography.labelSmall, color = IvoryGold)
            }
        }

        // ── Corps : route vers le bon exercice ───────────────────────
        Box(modifier = Modifier.weight(1f)) {
            when (exercise.type) {
                ExerciseType.NOTE_RECOGNITION -> NoteRecognitionExercise(
                    notes      = exercise.notes,
                    rounds     = exercise.rounds,
                    onComplete = { score, total ->
                        // XP proportionnel au score
                        val xpEarned = (exercise.xpReward * score / total.toFloat()).toInt()
                        onComplete(xpEarned)
                    }
                )
                ExerciseType.SCALE_GUIDED -> ScaleGuidedExercise(
                    targetNotes = exercise.targetNotes,
                    onComplete  = { score, total ->
                        val xpEarned = (exercise.xpReward * score / total.toFloat()).toInt()
                        onComplete(xpEarned)
                    }
                )
                ExerciseType.RHYTHM_TAP -> RhythmTapExercise(
                    rhythmPattern = exercise.rhythmPattern,
                    onComplete    = { score, total ->
                        val xpEarned = (exercise.xpReward * score / total.toFloat()).toInt()
                        onComplete(xpEarned)
                    }
                )
            }
        }
    }
}
