package com.example.mypianoapp.screens.exercises

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun RhythmTapExercise(
    rhythmPattern: List<Int>,   // durées en ms entre chaque battement
    onComplete: (score: Int, total: Int) -> Unit
) {
    val total         = rhythmPattern.size
    var phase         by remember { mutableStateOf(RhythmPhase.INTRO) }
    var beatIndex     by remember { mutableStateOf(0) }
    var userTaps      by remember { mutableStateOf(listOf<Long>()) }
    var beatTimestamps by remember { mutableStateOf(listOf<Long>()) }
    var isButtonLit   by remember { mutableStateOf(false) }
    var score         by remember { mutableStateOf(0) }
    var countDown     by remember { mutableStateOf(3) }

    // ── Déroulement de l'exercice ─────────────────────────────────────
    LaunchedEffect(phase) {
        when (phase) {
            RhythmPhase.INTRO -> {
                // Compte à rebours 3-2-1
                repeat(3) { i ->
                    countDown = 3 - i
                    delay(800)
                }
                phase = RhythmPhase.PLAYING
            }
            RhythmPhase.PLAYING -> {
                val timestamps = mutableListOf<Long>()
                rhythmPattern.forEach { durationMs ->
                    isButtonLit = true
                    timestamps.add(System.currentTimeMillis())
                    delay(150)
                    isButtonLit = false
                    delay((durationMs - 150).toLong().coerceAtLeast(50))
                    beatIndex++
                }
                beatTimestamps = timestamps
                beatIndex      = 0
                phase          = RhythmPhase.TAP
            }
            RhythmPhase.TAP -> {
                // L'utilisateur tape — on attend la fin
                delay(rhythmPattern.sumOf { it }.toLong() + 2000)
                // Calcul du score
                var correct = 0
                val taps = userTaps
                beatTimestamps.forEachIndexed { i, beatTime ->
                    val closestTap = taps.minByOrNull { abs(it - beatTime) }
                    if (closestTap != null && abs(closestTap - beatTime) < 250) correct++
                }
                score = correct
                phase = RhythmPhase.RESULT
            }
            RhythmPhase.RESULT -> { /* handled by UI */ }
        }
    }

    // Pulsation visuelle pendant la phase TAP
    val infiniteTransition = rememberInfiniteTransition(label = "metronome")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(rhythmPattern.firstOrNull() ?: 600, easing = EaseInOut),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    if (phase == RhythmPhase.RESULT) {
        ExerciseResult(score = score, total = total, onContinue = { onComplete(score, total) })
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        when (phase) {
            // ── Compte à rebours ─────────────────────────────────────
            RhythmPhase.INTRO -> {
                Spacer(Modifier.weight(1f))
                Text(
                    "Écoute d'abord le rythme,\npuis reproduis-le !",
                    style     = MaterialTheme.typography.titleLarge,
                    color     = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "$countDown",
                    style = MaterialTheme.typography.displayLarge,
                    color = KeysVioletLight
                )
                Spacer(Modifier.weight(1f))
            }

            // ── Démonstration ─────────────────────────────────────────
            RhythmPhase.PLAYING -> {
                Spacer(Modifier.weight(1f))
                Text(
                    "Écoute le rythme",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Spacer(Modifier.height(16.dp))

                // Indicateurs de beats
                BeatIndicators(total = total, current = beatIndex, lit = isButtonLit)

                Spacer(Modifier.height(24.dp))

                // Cercle pulsant
                val litColor by animateColorAsState(
                    targetValue   = if (isButtonLit) KeysViolet else EbonyCard,
                    animationSpec = tween(80),
                    label         = "beat_color"
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(litColor)
                        .border(2.dp, if (isButtonLit) KeysVioletLight else EbonyBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎵", style = MaterialTheme.typography.displaySmall)
                }
                Spacer(Modifier.weight(1f))
            }

            // ── Phase de tap ──────────────────────────────────────────
            RhythmPhase.TAP -> {
                Spacer(Modifier.weight(1f))
                Text(
                    "À toi ! Tape le même rythme",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${userTaps.size} / $total taps",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                BeatIndicators(total = total, current = userTaps.size, lit = false)

                Spacer(Modifier.height(24.dp))

                // Bouton TAP
                Box(
                    modifier = Modifier
                        .scale(if (userTaps.size < total) pulseScale else 1f)
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            if (userTaps.size < total) KeysViolet else HarmonyGreen
                        )
                        .border(2.dp, KeysVioletLight.copy(0.5f), CircleShape)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                if (userTaps.size < total) {
                                    userTaps = userTaps + System.currentTimeMillis()
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TAP", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                        Text("🥁", style = MaterialTheme.typography.titleLarge)
                    }
                }
                Spacer(Modifier.weight(1f))
            }

            else -> {}
        }
    }
}

// ── Indicateurs visuels de beats ──────────────────────────────────────────

@Composable
private fun BeatIndicators(total: Int, current: Int, lit: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(total) { i ->
            val isCurrent = i == current && lit
            val isDone    = i < current
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 18.dp else 14.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCurrent -> KeysVioletLight
                            isDone    -> KeysViolet.copy(0.5f)
                            else      -> EbonySurface
                        }
                    )
            )
        }
    }
}

enum class RhythmPhase { INTRO, PLAYING, TAP, RESULT }
