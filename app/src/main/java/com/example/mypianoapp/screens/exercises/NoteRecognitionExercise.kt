package com.example.mypianoapp.screens.exercises

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun NoteRecognitionExercise(
    notes: List<String>,
    rounds: Int,
    onComplete: (score: Int, total: Int) -> Unit
) {
    var currentRound  by remember { mutableStateOf(0) }
    var score         by remember { mutableStateOf(0) }
    var targetNote    by remember { mutableStateOf(notes.random()) }
    var feedback      by remember { mutableStateOf<Boolean?>(null) } // null=en attente, true=correct, false=faux
    var pressedKey    by remember { mutableStateOf<String?>(null) }
    var finished      by remember { mutableStateOf(false) }

    val whiteKeys = listOf("Do", "Ré", "Mi", "Fa", "Sol", "La", "Si")
    val blackKeys = listOf(0 to "Do#", 1 to "Ré#", 3 to "Fa#", 4 to "Sol#", 5 to "La#")

    // Passe à la note suivante après feedback
    LaunchedEffect(feedback) {
        if (feedback != null) {
            delay(800)
            pressedKey = null
            feedback   = null
            if (currentRound + 1 >= rounds) {
                finished = true
            } else {
                currentRound++
                // Nouvelle note différente de la précédente
                var next = notes.random()
                while (next == targetNote && notes.size > 1) next = notes.random()
                targetNote = next
            }
        }
    }

    if (finished) {
        ExerciseResult(score = score, total = rounds, onContinue = { onComplete(score, rounds) })
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Barre de progression rounds
        RoundProgressBar(current = currentRound, total = rounds)

        // Instruction
        Text(
            "Trouve cette note sur le clavier",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Note cible — animée
        AnimatedContent(
            targetState   = targetNote,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label         = "target_note",
            modifier      = Modifier.fillMaxWidth()
        ) { note ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        when (feedback) {
                            true  -> HarmonyGreen.copy(0.15f)
                            false -> DissonanceRed.copy(0.15f)
                            null  -> KeysVioletGlow
                        }
                    )
                    .border(
                        2.dp,
                        when (feedback) {
                            true  -> HarmonyGreen.copy(0.5f)
                            false -> DissonanceRed.copy(0.5f)
                            null  -> KeysViolet.copy(0.4f)
                        },
                        RoundedCornerShape(20.dp)
                    )
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text  = note,
                        style = MaterialTheme.typography.displayLarge,
                        color = when (feedback) {
                            true  -> HarmonyGreen
                            false -> DissonanceRed
                            null  -> TextPrimary
                        }
                    )
                    when (feedback) {
                        true  -> Text("✓ Correct !", style = MaterialTheme.typography.labelLarge, color = HarmonyGreen)
                        false -> Text("✗ C'était $targetNote", style = MaterialTheme.typography.labelLarge, color = DissonanceRed)
                        null  -> Text("Appuie sur la touche", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
            }
        }

        // Clavier interactif
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().height(180.dp)
        ) {
            val totalWidth = maxWidth
            val whiteCount = whiteKeys.size
            val whiteWidth = totalWidth / whiteCount
            val gap        = 2.dp
            val blackWidth = whiteWidth * 0.62f
            val blackHeight = 110.dp

            // Touches blanches
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                whiteKeys.forEach { note ->
                    val isPressed  = pressedKey == note
                    val isTarget   = note == targetNote
                    val showTarget = feedback == null && isTarget

                    val bgColor by animateColorAsState(
                        targetValue = when {
                            feedback == true  && isTarget  -> HarmonyGreen.copy(0.7f)
                            feedback == false && isPressed -> DissonanceRed.copy(0.6f)
                            isPressed                      -> KeysVioletLight
                            else                           -> TextPrimary.copy(0.93f)
                        },
                        animationSpec = tween(120), label = "wk_$note"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                            .background(bgColor)
                            .border(
                                width = if (showTarget) 2.dp else 1.dp,
                                color = if (showTarget) KeysViolet else EbonyBorder.copy(0.3f),
                                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                            )
                            .pointerInput(note) {
                                detectTapGestures(onPress = {
                                    if (feedback == null) {
                                        pressedKey = note
                                        feedback = (note == targetNote)
                                        if (note == targetNote) score++
                                    }
                                    tryAwaitRelease()
                                })
                            },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            note,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = if (isPressed) TextPrimary else EbonyDeep,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }

            // Touches noires
            blackKeys.forEach { (wIndex, noteName) ->
                val isPressed  = pressedKey == noteName
                val isTarget   = noteName == targetNote
                val showTarget = feedback == null && isTarget

                val bgColor by animateColorAsState(
                    targetValue = when {
                        feedback == true  && isTarget  -> HarmonyGreen.copy(0.8f)
                        feedback == false && isPressed -> DissonanceRed.copy(0.7f)
                        isPressed                      -> KeysViolet
                        else                           -> EbonyDeep
                    },
                    animationSpec = tween(120), label = "bk_$noteName"
                )
                val leftOffset = whiteWidth * wIndex + whiteWidth * 0.69f + gap * wIndex
                Box(
                    modifier = Modifier
                        .offset(x = leftOffset)
                        .width(blackWidth)
                        .height(blackHeight)
                        .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                        .background(bgColor)
                        .border(
                            width = if (showTarget) 2.dp else 1.dp,
                            color = if (showTarget) KeysVioletLight else EbonyBorder,
                            shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
                        )
                        .pointerInput(noteName) {
                            detectTapGestures(onPress = {
                                if (feedback == null) {
                                    pressedKey = noteName
                                    feedback = (noteName == targetNote)
                                    if (noteName == targetNote) score++
                                }
                                tryAwaitRelease()
                            })
                        }
                )
            }
        }

        // Score en cours
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Score : $score / ${currentRound + 1}",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
        }
    }
}

// ── Barre de progression rounds ───────────────────────────────────────────

@Composable
fun RoundProgressBar(current: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        when {
                            i < current  -> KeysViolet
                            i == current -> KeysVioletLight.copy(0.5f)
                            else         -> EbonySurface
                        }
                    )
            )
        }
    }
}

// ── Écran de résultat ─────────────────────────────────────────────────────

@Composable
fun ExerciseResult(score: Int, total: Int, onContinue: () -> Unit) {
    val pct = (score.toFloat() / total.toFloat() * 100).toInt()
    val emoji = when {
        pct == 100 -> "🏆"
        pct >= 70  -> "⭐"
        pct >= 40  -> "👍"
        else       -> "💪"
    }
    val message = when {
        pct == 100 -> "Parfait !"
        pct >= 70  -> "Très bien !"
        pct >= 40  -> "Pas mal !"
        else       -> "Continue à pratiquer !"
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(visible, enter = scaleIn(tween(400, easing = EaseOutBack)) + fadeIn()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(emoji, style = MaterialTheme.typography.displayLarge)
                Text(message, style = MaterialTheme.typography.displaySmall, color = TextPrimary, textAlign = TextAlign.Center)
                Text(
                    "$score / $total bonnes réponses",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )

                // Cercle de score
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress    = { score.toFloat() / total.toFloat() },
                        modifier    = Modifier.size(100.dp),
                        strokeWidth = 8.dp,
                        color       = when {
                            pct == 100 -> IvoryGold
                            pct >= 70  -> HarmonyGreen
                            else       -> KeysViolet
                        },
                        trackColor  = EbonySurface
                    )
                    Text("$pct%", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                }

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(KeysViolet)
                        .pointerInput(Unit) { detectTapGestures { onContinue() } }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Continuer", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                }
            }
        }
    }
}
