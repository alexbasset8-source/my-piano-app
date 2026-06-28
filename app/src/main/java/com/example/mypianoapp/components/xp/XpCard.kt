package com.example.mypianoapp.components.xp

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.data.UserProgress
import com.example.mypianoapp.ui.theme.*

@Composable
fun XpCard(progress: UserProgress) {
    var started by remember { mutableStateOf(false) }
    val animatedXp by animateFloatAsState(
        targetValue  = if (started) progress.xpFraction else 0f,
        animationSpec = tween(1400),
        label        = "xp"
    )
    LaunchedEffect(Unit) { started = true }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(EbonyCard)
            .border(1.dp, EbonyBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(KeysVioletGlow)
                .border(1.dp, KeysViolet, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("⭐", style = MaterialTheme.typography.headlineMedium)
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Niveau ${progress.level}",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Text(
                    "${progress.xpProgressInLevel} / ${progress.xpNeededForNextLevel} XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(EbonySurface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedXp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(IvoryGold)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🔥", style = MaterialTheme.typography.titleMedium)
            Text(
                "${progress.currentStreak}j",
                style = MaterialTheme.typography.labelSmall,
                color = IvoryGold
            )
        }
    }
}
