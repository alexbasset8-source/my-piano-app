package com.example.mypianoapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.ui.theme.*

@Composable
fun ExerciseScreen() {
    data class Exercise(val emoji: String, val title: String, val xp: Int, val color: Color)

    val exercises = listOf(
        Exercise("🎵", "Gamme de Do — main droite", 15, KeysViolet),
        Exercise("🎶", "Arpèges de Sol", 20, NotesTeal),
        Exercise("🥁", "Rythme en noires", 10, IvoryGold),
        Exercise("🎹", "Coordination 2 mains", 30, DissonanceRed),
        Exercise("🎼", "Lecture de partition", 25, HarmonyGreen),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EbonyDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Exercices", style = MaterialTheme.typography.displaySmall, color = TextPrimary)
        Text("Choisissez votre entraînement", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(4.dp))

        exercises.forEach { ex ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EbonyCard)
                    .border(1.dp, EbonyBorder, RoundedCornerShape(16.dp))
                    .clickable {}
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ex.color.copy(alpha = 0.15f))
                        .border(1.dp, ex.color.copy(0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(ex.emoji, style = MaterialTheme.typography.titleLarge)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(ex.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ex.color.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("+${ex.xp} XP", style = MaterialTheme.typography.labelSmall, color = ex.color)
                }
            }
        }
    }
}
