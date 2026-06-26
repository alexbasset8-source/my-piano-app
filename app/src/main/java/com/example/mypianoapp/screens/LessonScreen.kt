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
import com.example.mypianoapp.data.LessonCatalog
import com.example.mypianoapp.data.LessonData
import com.example.mypianoapp.ui.theme.*

// Simule les leçons débloquées/terminées (à brancher sur un vrai ViewModel plus tard)
private val DONE_IDS   = setOf(1, 2)
private val LOCKED_IDS = setOf(5, 6, 7, 8, 9, 10)

@Composable
fun LessonScreen(onLessonClick: (Int) -> Unit) {
    val lessons = LessonCatalog.lessons
    val doneCount = lessons.count { it.id in DONE_IDS }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EbonyDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Text("Parcours", style = MaterialTheme.typography.displaySmall, color = TextPrimary)
        Text(
            "$doneCount / ${lessons.size} leçons complètes",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        // Barre progression globale
        Spacer(Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(EbonySurface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(doneCount.toFloat() / lessons.size.toFloat())
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(KeysViolet)
            )
        }
        Spacer(Modifier.height(4.dp))

        lessons.forEachIndexed { i, lesson ->
            val done   = lesson.id in DONE_IDS
            val locked = lesson.id in LOCKED_IDS
            LessonItem(
                index  = i + 1,
                lesson = lesson,
                done   = done,
                locked = locked,
                onClick = { if (!locked) onLessonClick(lesson.id) }
            )
        }
    }
}

@Composable
private fun LessonItem(
    index: Int,
    lesson: LessonData,
    done: Boolean,
    locked: Boolean,
    onClick: () -> Unit
) {
    val alpha = if (locked) 0.38f else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(EbonyCard)
            .border(
                1.dp,
                when {
                    done   -> KeysViolet.copy(alpha = 0.45f)
                    locked -> EbonyBorder
                    else   -> EbonyBorder
                },
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !locked, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Numéro / état
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    when {
                        done   -> KeysViolet
                        locked -> EbonySurface
                        else   -> EbonySurface
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                locked -> Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                done   -> Text("✓", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                else   -> Text(
                    lesson.emoji,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // Texte
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                lesson.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary.copy(alpha = alpha)
            )
            Text(
                lesson.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary.copy(alpha = alpha)
            )
        }

        // Méta droite
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                lesson.duration,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted.copy(alpha = alpha)
            )
            if (!locked) {
                Text(
                    "+${lesson.xpReward} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = IvoryGold.copy(alpha = alpha)
                )
            }
        }

        if (!locked && !done) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Commencer",
                tint = KeysVioletLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
