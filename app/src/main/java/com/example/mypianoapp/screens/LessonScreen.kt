package com.example.mypianoapp.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.ui.theme.*

data class Lesson(val title: String, val subtitle: String, val duration: String, val locked: Boolean, val done: Boolean)

@Composable
fun LessonScreen() {
    val lessons = listOf(
        Lesson("Les notes de base", "Do, Ré, Mi, Fa, Sol", "5 min", false, true),
        Lesson("La gamme de Do", "Position de la main droite", "8 min", false, true),
        Lesson("Rythme et mesure", "Noires, blanches, rondes", "10 min", false, false),
        Lesson("Les accords majeurs", "Do, Sol, Fa", "12 min", true, false),
        Lesson("Main gauche", "Accompagnement simple", "10 min", true, false),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EbonyDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Parcours", style = MaterialTheme.typography.displaySmall, color = TextPrimary)
        Text("${lessons.count { it.done }} / ${lessons.size} leçons complètes",
            style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(4.dp))

        lessons.forEachIndexed { i, lesson ->
            LessonItem(index = i + 1, lesson = lesson)
        }
    }
}

@Composable
private fun LessonItem(index: Int, lesson: Lesson) {
    val alpha = if (lesson.locked) 0.4f else 1f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(EbonyCard)
            .border(1.dp, if (lesson.done) KeysViolet.copy(0.4f) else EbonyBorder, RoundedCornerShape(16.dp))
            .clickable(enabled = !lesson.locked) {}
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (lesson.done) KeysViolet else EbonySurface),
            contentAlignment = Alignment.Center
        ) {
            if (lesson.locked) Icon(Icons.Default.Lock, null, tint = TextMuted, modifier = Modifier.size(18.dp))
            else if (lesson.done) Text("✓", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            else Text("$index", style = MaterialTheme.typography.titleMedium, color = KeysVioletLight)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(lesson.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary.copy(alpha = alpha))
            Text(lesson.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary.copy(alpha = alpha))
        }
        Text(lesson.duration, style = MaterialTheme.typography.labelSmall, color = TextMuted.copy(alpha = alpha))
        if (!lesson.locked && !lesson.done) {
            Icon(Icons.Default.PlayArrow, null, tint = KeysVioletLight, modifier = Modifier.size(20.dp))
        }
    }
}
