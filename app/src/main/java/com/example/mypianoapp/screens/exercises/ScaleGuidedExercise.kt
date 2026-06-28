package com.example.mypianoapp.screens.exercises

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ScaleGuidedExercise(
    targetNotes: List<String>,
    onComplete: (score: Int, total: Int) -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var errors       by remember { mutableStateOf(0) }
    var feedback     by remember { mutableStateOf<Boolean?>(null) }
    var pressedKey   by remember { mutableStateOf<String?>(null) }
    var finished     by remember { mutableStateOf(false) }

    val allWhiteKeys = listOf("Do", "Ré", "Mi", "Fa", "Sol", "La", "Si")
    val blackKeys    = listOf(0 to "Do#", 1 to "Ré#", 3 to "Fa#", 4 to "Sol#", 5 to "La#")

    // Reset feedback après un instant
    LaunchedEffect(feedback) {
        if (feedback != null) {
            delay(500)
            pressedKey = null
            if (feedback == true) {
                if (currentIndex + 1 >= targetNotes.size) {
                    finished = true
                } else {
                    currentIndex++
                }
            }
            feedback = null
        }
    }

    if (finished) {
        val score = targetNotes.size - errors
        ExerciseResult(
            score      = maxOf(score, 0),
            total      = targetNotes.size,
            onContinue = { onComplete(maxOf(score, 0), targetNotes.size) }
        )
        return
    }

    val targetNote = targetNotes[currentIndex]

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Barre de progression
        RoundProgressBar(current = currentIndex, total = targetNotes.size)

        Text(
            "Joue les notes dans l'ordre",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Séquence de notes avec progression visuelle
        NoteSequenceDisplay(
            notes        = targetNotes,
            currentIndex = currentIndex,
            feedback     = feedback
        )

        // Note actuelle à jouer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    when (feedback) {
                        true  -> HarmonyGreen.copy(0.15f)
                        false -> DissonanceRed.copy(0.12f)
                        null  -> EbonyCard
                    }
                )
                .border(
                    1.dp,
                    when (feedback) {
                        true  -> HarmonyGreen.copy(0.5f)
                        false -> DissonanceRed.copy(0.4f)
                        null  -> EbonyBorder
                    },
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    when (feedback) { true -> "✓" ; false -> "✗" ; else -> "▶" },
                    style = MaterialTheme.typography.titleLarge,
                    color = when (feedback) {
                        true  -> HarmonyGreen
                        false -> DissonanceRed
                        null  -> KeysVioletLight
                    }
                )
                Column {
                    Text(
                        "Joue maintenant :",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        targetNote,
                        style = MaterialTheme.typography.headlineLarge,
                        color = when (feedback) {
                            true  -> HarmonyGreen
                            false -> DissonanceRed
                            null  -> TextPrimary
                        }
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "${currentIndex + 1}/${targetNotes.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
            }
        }

        // Clavier
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            val totalWidth  = maxWidth
            val whiteCount  = allWhiteKeys.size
            val whiteWidth  = totalWidth / whiteCount
            val gap         = 2.dp
            val blackWidth  = whiteWidth * 0.62f
            val blackHeight = 98.dp

            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                allWhiteKeys.forEach { note ->
                    val isPressed = pressedKey == note
                    val isTarget  = note == targetNote
                    val bgColor by animateColorAsState(
                        targetValue = when {
                            feedback == true  && isTarget  -> HarmonyGreen.copy(0.7f)
                            feedback == false && isPressed -> DissonanceRed.copy(0.6f)
                            isPressed                      -> KeysVioletLight
                            else                           -> TextPrimary.copy(0.93f)
                        },
                        animationSpec = tween(100), label = "sg_wk_$note"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                            .background(bgColor)
                            .border(
                                width = if (isTarget && feedback == null) 2.dp else 1.dp,
                                color = if (isTarget && feedback == null) KeysViolet else EbonyBorder.copy(0.3f),
                                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                            )
                            .pointerInput(note) {
                                detectTapGestures(onPress = {
                                    if (feedback == null) {
                                        pressedKey = note
                                        val correct = (note == targetNote)
                                        feedback = correct
                                        if (!correct) errors++
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

            blackKeys.forEach { (wIndex, noteName) ->
                val isPressed = pressedKey == noteName
                val isTarget  = noteName == targetNote
                val bgColor by animateColorAsState(
                    targetValue = when {
                        feedback == true  && isTarget  -> HarmonyGreen.copy(0.8f)
                        feedback == false && isPressed -> DissonanceRed.copy(0.7f)
                        isPressed                      -> KeysViolet
                        else                           -> EbonyDeep
                    },
                    animationSpec = tween(100), label = "sg_bk_$noteName"
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
                            width = if (isTarget && feedback == null) 2.dp else 1.dp,
                            color = if (isTarget && feedback == null) KeysVioletLight else EbonyBorder,
                            shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
                        )
                        .pointerInput(noteName) {
                            detectTapGestures(onPress = {
                                if (feedback == null) {
                                    pressedKey = noteName
                                    val correct = (noteName == targetNote)
                                    feedback = correct
                                    if (!correct) errors++
                                }
                                tryAwaitRelease()
                            })
                        }
                )
            }
        }
    }
}

// ── Affichage séquence de notes ──────────────────────────────────────────

@Composable
private fun NoteSequenceDisplay(
    notes: List<String>,
    currentIndex: Int,
    feedback: Boolean?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(EbonyCard)
            .border(1.dp, EbonyBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        notes.forEachIndexed { i, note ->
            val isDone    = i < currentIndex
            val isCurrent = i == currentIndex

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            isDone    -> KeysViolet.copy(0.3f)
                            isCurrent -> when (feedback) {
                                true  -> HarmonyGreen.copy(0.3f)
                                false -> DissonanceRed.copy(0.25f)
                                null  -> KeysVioletGlow
                            }
                            else      -> EbonySurface
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            isDone    -> KeysViolet.copy(0.4f)
                            isCurrent -> when (feedback) {
                                true  -> HarmonyGreen.copy(0.6f)
                                false -> DissonanceRed.copy(0.5f)
                                null  -> KeysViolet.copy(0.6f)
                            }
                            else      -> EbonyBorder
                        },
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = if (isDone) "✓" else note,
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isDone    -> KeysVioletLight
                        isCurrent -> when (feedback) {
                            true  -> HarmonyGreen
                            false -> DissonanceRed
                            null  -> TextPrimary
                        }
                        else      -> TextMuted
                    }
                )
            }
        }
    }
}
