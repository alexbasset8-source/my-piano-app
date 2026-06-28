package com.example.mypianoapp.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.data.LessonCatalog
import com.example.mypianoapp.data.LessonData
import com.example.mypianoapp.data.UserProgress
import com.example.mypianoapp.ui.theme.*

@Composable
fun LessonScreen(
    progress: UserProgress,
    onLessonClick: (Int) -> Unit
) {
    val lessons   = LessonCatalog.lessons
    val doneCount = progress.completedLessonIds.size

    // Leçon suivante à faire
    val nextLesson = lessons.firstOrNull { !progress.isLessonCompleted(it.id) && progress.isLessonUnlocked(it.id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EbonyDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────
        Text("Parcours", style = MaterialTheme.typography.displaySmall, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(
            "$doneCount / ${lessons.size} leçons complètes",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(12.dp))

        // Barre de progression globale
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(EbonySurface)
        ) {
            val animProg by animateFloatAsState(
                targetValue   = progress.globalProgressFraction,
                animationSpec = tween(800),
                label         = "global_prog"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(animProg)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(Brush.horizontalGradient(listOf(KeysViolet, KeysVioletLight)))
            )
        }
        Spacer(Modifier.height(24.dp))

        // ── Carte "prochaine leçon" mise en avant ─────────────────────
        if (nextLesson != null && doneCount < lessons.size) {
            NextLessonCard(lesson = nextLesson, onClick = { onLessonClick(nextLesson.id) })
            Spacer(Modifier.height(20.dp))
            Text("TOUTES LES LEÇONS", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(Modifier.height(12.dp))
        } else if (doneCount == lessons.size) {
            CompletionBanner()
            Spacer(Modifier.height(20.dp))
        }

        // ── Liste des leçons avec paliers QCM ────────────────────────
        lessons.forEachIndexed { index, lesson ->
            val done    = progress.isLessonCompleted(lesson.id)
            val locked  = !progress.isLessonUnlocked(lesson.id)
            val isNext  = lesson.id == nextLesson?.id

            AnimatedLessonItem(
                lesson  = lesson,
                done    = done,
                locked  = locked,
                isNext  = isNext,
                onClick = { if (!locked) onLessonClick(lesson.id) }
            )

            // Palier QCM après les leçons 3, 6, 9
            if (lesson.id in listOf(3, 6, 9)) {
                val quizPassed = progress.isQuizPassed(lesson.id)
                val quizLocked = !done
                LessonConnector(topDone = done, bottomUnlocked = !quizLocked)
                QuizPalierItem(lessonId = lesson.id, passed = quizPassed, locked = quizLocked)
                LessonConnector(
                    topDone        = quizPassed,
                    bottomUnlocked = index + 1 < lessons.size && progress.isLessonUnlocked(lesson.id + 1)
                )
            } else if (index < lessons.size - 1) {
                LessonConnector(
                    topDone        = done,
                    bottomUnlocked = progress.isLessonUnlocked(lesson.id + 1)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ── Carte "prochaine leçon" ────────────────────────────────────────────────

@Composable
private fun NextLessonCard(lesson: LessonData, onClick: () -> Unit) {
    // Animation pulsante sur le bouton
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue   = 1f,
        targetValue    = 1.03f,
        animationSpec  = infiniteRepeatable(tween(900, easing = EaseInOut), RepeatMode.Reverse),
        label          = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(listOf(KeysViolet, Color(0xFF4C1D95)))
            )
            .border(1.dp, KeysVioletLight.copy(0.3f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Prochaine leçon",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary.copy(0.65f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        lesson.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        lesson.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary.copy(0.65f)
                    )
                }
                Text(lesson.emoji, style = MaterialTheme.typography.displaySmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoChip("⏱ ${lesson.duration}")
                    InfoChip("+${lesson.xpReward} XP")
                }
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TextPrimary.copy(0.2f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Commencer", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                        Icon(Icons.Default.PlayArrow, null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(TextPrimary.copy(0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = TextPrimary.copy(0.85f))
    }
}

// ── Bannière de complétion ────────────────────────────────────────────────

@Composable
private fun CompletionBanner() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(EbonyCard)
            .border(1.dp, IvoryGold.copy(0.4f), RoundedCornerShape(20.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("🎓", style = MaterialTheme.typography.displayMedium)
        Text("Parcours terminé !", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, textAlign = TextAlign.Center)
        Text("Tu as complété les 10 leçons du parcours débutant.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = TextAlign.Center)
    }
}


// ── Palier QCM visuel ─────────────────────────────────────────────────────

@Composable
private fun QuizPalierItem(lessonId: Int, passed: Boolean, locked: Boolean) {
    val alpha = if (locked) 0.38f else 1f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(EbonyCard)
            .border(
                1.dp,
                when {
                    passed -> IvoryGold.copy(0.5f)
                    locked -> EbonyBorder
                    else   -> IvoryGold.copy(0.3f)
                },
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    when {
                        passed -> IvoryGold.copy(0.2f)
                        locked -> EbonySurface
                        else   -> IvoryGoldGlow
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                when {
                    passed -> "✓"
                    locked -> "🔒"
                    else   -> "📝"
                },
                style = MaterialTheme.typography.titleSmall
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Palier QCM — Leçon $lessonId",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary.copy(alpha = alpha)
            )
            Text(
                when {
                    passed -> "Validé — suite débloquée"
                    locked -> "Terminez la leçon $lessonId d'abord"
                    else   -> "6/6 requis pour continuer"
                },
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    passed -> IvoryGold.copy(0.8f)
                    else   -> TextSecondary.copy(alpha = alpha)
                }
            )
        }
        if (passed) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(IvoryGoldGlow)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("✓", style = MaterialTheme.typography.labelSmall, color = IvoryGold)
            }
        }
    }
}

// ── Connecteur visuel entre deux leçons ──────────────────────────────────

@Composable
private fun LessonConnector(topDone: Boolean, bottomUnlocked: Boolean) {
    Box(
        modifier = Modifier
            .padding(start = 39.dp) // aligné avec le centre de l'icône (46dp/2 + 16dp padding)
            .width(2.dp)
            .height(16.dp)
            .background(
                if (topDone && bottomUnlocked) KeysViolet.copy(0.6f)
                else EbonyBorder
            )
    )
}

// ── Item leçon avec animation d'entrée ───────────────────────────────────

@Composable
private fun AnimatedLessonItem(
    lesson: LessonData,
    done: Boolean,
    locked: Boolean,
    isNext: Boolean,
    onClick: () -> Unit
) {
    // Animation d'apparition au premier rendu
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible  = visible,
        enter    = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 },
    ) {
        LessonItem(
            lesson  = lesson,
            done    = done,
            locked  = locked,
            isNext  = isNext,
            onClick = onClick
        )
    }
}

// ── Item leçon ────────────────────────────────────────────────────────────

@Composable
private fun LessonItem(
    lesson: LessonData,
    done: Boolean,
    locked: Boolean,
    isNext: Boolean,
    onClick: () -> Unit
) {
    val alpha = if (locked) 0.35f else 1f

    // Bordure animée pour la leçon suivante
    val borderColor by animateColorAsState(
        targetValue = when {
            done   -> KeysViolet.copy(0.5f)
            isNext -> KeysVioletLight.copy(0.6f)
            locked -> EbonyBorder
            else   -> EbonyBorder
        },
        animationSpec = tween(500),
        label = "border_${lesson.id}"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isNext) EbonyCard
                else EbonyCard
            )
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = !locked, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icône état
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    when {
                        done   -> KeysViolet
                        isNext -> EbonySurface
                        locked -> EbonySurface.copy(alpha = 0.5f)
                        else   -> EbonySurface
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                locked -> Icon(Icons.Default.Lock, null, tint = TextMuted.copy(alpha), modifier = Modifier.size(18.dp))
                done   -> Text("✓", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                else   -> Text(lesson.emoji, style = MaterialTheme.typography.titleMedium)
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
                Icons.Default.PlayArrow, null,
                tint = if (isNext) KeysVioletLight else TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
