package com.example.mypianoapp.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var logoVisible  by remember { mutableStateOf(false) }
    var textVisible  by remember { mutableStateOf(false) }
    var startFadeOut by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue   = if (logoVisible) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "logo_scale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue   = if (logoVisible) 1f else 0f,
        animationSpec = tween(400),
        label         = "logo_alpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue   = if (textVisible) 1f else 0f,
        animationSpec = tween(500),
        label         = "text_alpha"
    )
    val screenAlpha by animateFloatAsState(
        targetValue   = if (startFadeOut) 0f else 1f,
        animationSpec = tween(400),
        label         = "screen_alpha",
        finishedListener = { if (startFadeOut) onFinished() }
    )

    LaunchedEffect(Unit) {
        delay(100); logoVisible = true
        delay(500); textVisible = true
        delay(1200); startFadeOut = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha)
            .background(EbonyDeep),
        contentAlignment = Alignment.Center
    ) {
        // Glow
        Box(
            modifier = Modifier
                .size(300.dp)
                .blur(120.dp)
                .background(KeysVioletGlow, CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .size(100.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(listOf(KeysViolet, Color(0xFF4C1D95)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🎹", style = MaterialTheme.typography.displayMedium)
            }

            // Texte
            Column(
                modifier = Modifier.alpha(textAlpha),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "My Piano App",
                    style     = MaterialTheme.typography.displaySmall,
                    color     = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Apprends. Joue. Progresse.",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
