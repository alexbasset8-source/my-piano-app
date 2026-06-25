package com.example.mypianoapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.ui.theme.*

@Composable
fun PianoScreen() {
    val whiteKeys = listOf("Do", "Ré", "Mi", "Fa", "Sol", "La", "Si")
    val blackKeys = listOf(0 to "C#", 1 to "D#", 3 to "F#", 4 to "G#", 5 to "A#")
    var pressedKey by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EbonyDeep)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text("Piano", style = MaterialTheme.typography.displaySmall, color = TextPrimary)
            Text("Appuyez sur une touche", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }

        // Note affichée
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(EbonyCard)
                .border(1.dp, EbonyBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = pressedKey ?: "🎵",
                style = if (pressedKey != null) MaterialTheme.typography.displayMedium
                        else MaterialTheme.typography.displaySmall,
                color = if (pressedKey != null) KeysVioletLight else TextMuted
            )
        }

        // Clavier
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            // Touches blanches
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                whiteKeys.forEach { note ->
                    val isPressed = pressedKey == note
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                            .background(
                                if (isPressed) KeysVioletLight
                                else TextPrimary.copy(alpha = 0.92f)
                            )
                            .clickable { pressedKey = if (pressedKey == note) null else note },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            text = note,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPressed) TextPrimary else EbonyDeep,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
            // Touches noires (superposées)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                val keyWidth = 1f / whiteKeys.size
                blackKeys.forEachIndexed { bi, (wIndex, noteName) ->
                    val isPressed = pressedKey == noteName
                    // Décalage approximatif
                    val offset = (wIndex + 0.6f) * keyWidth
                    Spacer(Modifier.weight(offset - if (bi == 0) 0f else
                        (blackKeys[bi - 1].first + 0.6f) * keyWidth))
                    Box(
                        modifier = Modifier
                            .weight(keyWidth * 0.65f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                            .background(if (isPressed) KeysViolet else EbonyDeep)
                            .border(1.dp, EbonyBorder,
                                RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                            .clickable { pressedKey = if (pressedKey == noteName) null else noteName }
                    )
                }
                Spacer(Modifier.weight(1f - (blackKeys.last().first + 0.6f) * keyWidth))
            }
        }

        // Octave info
        Text(
            "Octave 4 — Clavier démo interactif",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}
