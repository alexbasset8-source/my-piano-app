package com.example.mypianoapp.components.progress

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.ui.theme.*

@Composable
fun ProgressSection() {
    val progress = 0.12f
    var started by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (started) progress else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "progress"
    )

    LaunchedEffect(Unit) { started = true }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progression globale",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = "${(animatedProgress * 100).toInt()} %",
                style = MaterialTheme.typography.labelMedium,
                color = KeysVioletLight
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(EbonySurface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(KeysViolet)
            )
        }
    }
}
