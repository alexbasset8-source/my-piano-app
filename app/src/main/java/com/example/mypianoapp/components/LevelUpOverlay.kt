package com.example.mypianoapp.components

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LevelUpOverlay(
    newLevel: Int,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
        delay(3000)
        onDismiss()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "levelup")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.3f,
        targetValue   = 0.7f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOut), RepeatMode.Reverse),
        label         = "glow"
    )
    val starScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.15f,
        animationSpec = infiniteRepeatable(tween(600, easing = EaseInOut), RepeatMode.Reverse),
        label         = "star"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EbonyDeep.copy(alpha = 0.88f))
            .pointerInput(Unit) { detectTapGestures { onDismiss() } },
        contentAlignment = Alignment.Center
    ) {
        // Glow de fond
        Box(
            modifier = Modifier
                .size(320.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(KeysViolet.copy(glowAlpha), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter   = scaleIn(tween(500, easing = EaseOutBack)) + fadeIn(tween(300))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(40.dp)
            ) {
                // Badge niveau
                Box(
                    modifier = Modifier
                        .scale(starScale)
                        .size(110.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(listOf(KeysViolet, Color(0xFF4C1D95)))
                        )
                        .border(2.dp, IvoryGold.copy(0.6f), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⭐", style = MaterialTheme.typography.displaySmall)
                        Text(
                            "$newLevel",
                            style = MaterialTheme.typography.displayMedium,
                            color = IvoryGold
                        )
                    }
                }

                // Texte
                Text(
                    "NIVEAU SUPÉRIEUR !",
                    style     = MaterialTheme.typography.labelLarge,
                    color     = IvoryGold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Tu as atteint\nle niveau $newLevel",
                    style     = MaterialTheme.typography.displaySmall,
                    color     = TextPrimary,
                    textAlign = TextAlign.Center
                )

                // Titre de niveau
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(IvoryGoldGlow)
                        .border(1.dp, IvoryGold.copy(0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = levelTitle(newLevel),
                        style = MaterialTheme.typography.titleMedium,
                        color = IvoryGold
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Appuie pour continuer",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

private fun levelTitle(level: Int) = when (level) {
    1    -> "Débutant"
    2    -> "Apprenti Pianiste"
    3    -> "Pianiste Confirmé"
    4    -> "Musicien Avancé"
    5    -> "Expert du Clavier"
    else -> "Maître Pianiste"
}
