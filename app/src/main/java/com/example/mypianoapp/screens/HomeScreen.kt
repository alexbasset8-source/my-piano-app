package com.example.mypianoapp.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import com.example.mypianoapp.data.UserProgress
import com.example.mypianoapp.navigation.Screen
import com.example.mypianoapp.ui.theme.*

@Composable
fun HomeScreen(progress: UserProgress, onNavigate: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(EbonyDeep)) {
        Box(modifier = Modifier.size(320.dp).offset((-60).dp, (-40).dp).blur(120.dp).background(KeysVioletGlow, CircleShape))
        Box(modifier = Modifier.size(200.dp).align(Alignment.TopEnd).offset(60.dp, 80.dp).blur(100.dp).background(IvoryGoldGlow, CircleShape))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 56.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            HomeHeader(userName = progress.userName)
            HeroSessionCard(progress = progress, onContinue = { onNavigate(Screen.Lessons.route) })
            XpCard(progress = progress)

            SectionLabel("Progression")
            ProgressSection(progress = progress)

            SectionLabel("Continuer")
            QuickActionsGrid(onNavigate = onNavigate)

            SectionLabel("Mini-jeu")
            GoblinGameCard(onClick = { onNavigate("goblin_game") })

            SectionLabel("Aujourd'hui")
            DailyStatsRow(progress = progress)
        }
    }
}

@Composable
private fun HomeHeader(userName: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("Bonjour 👋", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Text(userName, style = MaterialTheme.typography.displaySmall, color = TextPrimary)
        }
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(EbonySurface).border(1.dp, EbonyBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text("🎹", style = MaterialTheme.typography.titleLarge) }
    }
}

@Composable
private fun HeroSessionCard(progress: UserProgress, onContinue: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    val goalAnim by animateFloatAsState(
        targetValue   = if (started) progress.dailyGoalFraction else 0f,
        animationSpec = tween(1000),
        label         = "goal"
    )
    LaunchedEffect(Unit) { started = true }

    val goalPct = (progress.dailyGoalFraction * 100).toInt().coerceAtMost(100)
    val goalDone = progress.dailyGoalFraction >= 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(KeysViolet, Color(0xFF4C1D95))))
            .border(1.dp, Brush.linearGradient(listOf(KeysVioletLight.copy(0.4f), Color.Transparent)), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text("Objectif du jour", style = MaterialTheme.typography.labelMedium, color = TextPrimary.copy(0.7f))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (goalDone) "✓ Objectif atteint !" else "${progress.todayMinutes} / ${progress.dailyGoalMinutes} min",
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary
                    )
                }
                Box(
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(TextPrimary.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text(if (goalDone) "🏆" else "🎵", style = MaterialTheme.typography.displaySmall) }
            }

            // Barre de progression objectif
            Box(modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(TextPrimary.copy(0.15f))) {
                Box(modifier = Modifier.fillMaxWidth(goalAnim).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(
                    if (goalDone) IvoryGold else TextPrimary.copy(0.7f)
                ))
            }

            // CTA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TextPrimary.copy(0.15f))
                    .clickable(onClick = onContinue)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Continuer ma progression", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
}

@Composable
private fun QuickActionsGrid(onNavigate: (String) -> Unit) {
    val actions = listOf(
        Triple("Leçons",    Icons.Default.LibraryMusic,  KeysViolet)    to Screen.Lessons.route,
        Triple("Piano",     Icons.Default.MusicNote,     NotesTeal)     to Screen.Piano.route,
        Triple("Exercices", Icons.Default.SportsEsports, IvoryGold)     to Screen.Exercises.route,
        Triple("Trophées",  Icons.Default.EmojiEvents,   DissonanceRed) to Screen.Profile.route
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(actions.take(2), actions.drop(2)).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (triple, route) ->
                    QuickActionCard(triple.first, triple.second, triple.third, Modifier.weight(1f)) { onNavigate(route) }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(label: String, icon: ImageVector, accent: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(EbonyCard)
            .border(1.dp, EbonyBorder, RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(accent.copy(0.15f)), contentAlignment = Alignment.Center) {
            Icon(icon, label, tint = accent, modifier = Modifier.size(20.dp))
        }
        Text(label, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
    }
}

@Composable
private fun DailyStatsRow(progress: UserProgress) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard("⏱", "${progress.todayMinutes}", "min pratiqués",          Modifier.weight(1f))
        StatCard("🎯", "${progress.todayExercises}", "exercices",            Modifier.weight(1f))
        StatCard("✅", "${progress.todayLessonsCompleted}", "leçons",        Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(emoji: String, value: String, label: String, modifier: Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).background(EbonyCard)
            .border(1.dp, EbonyBorder, RoundedCornerShape(14.dp)).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(emoji, style = MaterialTheme.typography.titleLarge)
        Text(value, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun GoblinGameCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1A0A2E), Color(0xFF0D1A2E))
                )
            )
            .border(1.dp, Color(0xFF3D2060), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("👺", style = MaterialTheme.typography.displaySmall)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "Goblin Attack !",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Text(
                    "Joue les notes pour repousser les gobelins",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF7C3AED).copy(0.2f))
                    .border(1.dp, Color(0xFF7C3AED).copy(0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Jouer", style = MaterialTheme.typography.labelMedium, color = KeysVioletLight)
            }
        }
    }
}
