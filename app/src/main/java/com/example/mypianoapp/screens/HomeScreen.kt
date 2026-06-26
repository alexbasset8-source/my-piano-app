package com.example.mypianoapp.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.components.progress.ProgressSection
import com.example.mypianoapp.components.xp.XpCard
import com.example.mypianoapp.ui.theme.*

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EbonyDeep)
    ) {
        // Glow ambiant en arrière-plan
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-60).dp, y = (-40).dp)
                .blur(120.dp)
                .background(KeysVioletGlow, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = 80.dp)
                .blur(100.dp)
                .background(IvoryGoldGlow, CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 56.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── En-tête ──────────────────────────────────────────
            HomeHeader()

            // ── Carte héro : session du jour ─────────────────────
            HeroSessionCard()

            // ── XP & Streak ──────────────────────────────────────
            XpCard()

            // ── Progression globale ───────────────────────────────
            SectionLabel("Progression")
            ProgressSection()

            // ── Raccourcis rapides ────────────────────────────────
            SectionLabel("Continuer")
            QuickActionsGrid()

            // ── Stats du jour ─────────────────────────────────────
            SectionLabel("Aujourd'hui")
            DailyStatsRow()
        }
    }
}

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Bonjour 👋",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "My Piano App",
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(EbonySurface)
                .border(1.dp, EbonyBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🎹", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun HeroSessionCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        KeysViolet,
                        Color(0xFF4C1D95)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(listOf(KeysVioletLight.copy(alpha = 0.4f), Color.Transparent)),
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Session du jour",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "10 min",
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "par jour",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary.copy(alpha = 0.6f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(TextPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎵", style = MaterialTheme.typography.displaySmall)
                }
            }

            // CTA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TextPrimary.copy(alpha = 0.15f))
                    .clickable {}
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Continuer ma progression",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary
    )
}

@Composable
private fun QuickActionsGrid() {
    val actions = listOf(
        Triple("Leçons", Icons.Default.LibraryMusic, KeysViolet),
        Triple("Piano", Icons.Default.MusicNote, NotesTeal),
        Triple("Exercices", Icons.Default.SportsEsports, IvoryGold),
        Triple("Trophées", Icons.Default.EmojiEvents, DissonanceRed)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        actions.take(2).forEach { (label, icon, color) ->
            QuickActionCard(
                label = label,
                icon = icon,
                accent = color,
                modifier = Modifier.weight(1f)
            )
        }
    }
    Spacer(Modifier.height(0.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        actions.drop(2).forEach { (label, icon, color) ->
            QuickActionCard(
                label = label,
                icon = icon,
                accent = color,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(EbonyCard)
            .border(1.dp, EbonyBorder, RoundedCornerShape(16.dp))
            .clickable {}
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary
        )
    }
}

@Composable
private fun DailyStatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard("⏱", "8 min", "pratiqués", Modifier.weight(1f))
        StatCard("🎯", "4", "exercices", Modifier.weight(1f))
        StatCard("✅", "2", "leçons", Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(EbonyCard)
            .border(1.dp, EbonyBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(emoji, style = MaterialTheme.typography.titleLarge)
        Text(value, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
