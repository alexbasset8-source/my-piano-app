package com.example.mypianoapp.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.data.Badge
import com.example.mypianoapp.data.BadgeRarity
import com.example.mypianoapp.data.BadgeSystem
import com.example.mypianoapp.data.LessonCatalog
import com.example.mypianoapp.data.UserProgress
import com.example.mypianoapp.ui.theme.*

@Composable
fun ProfileScreen(
    progress: UserProgress,
    onResetProgress: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    val unlockedBadgeIds = remember(progress) { BadgeSystem.unlockedBadgeIds(progress) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor   = EbonyCard,
            shape            = RoundedCornerShape(20.dp),
            title  = { Text("Réinitialiser ?", color = TextPrimary) },
            text   = { Text("Tout ton XP, tes leçons, ton streak et tes badges seront effacés.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { onResetProgress(); showResetDialog = false }) {
                    Text("Réinitialiser", color = DissonanceRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Annuler", color = TextSecondary)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EbonyDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Profil", style = MaterialTheme.typography.displaySmall, color = TextPrimary)

        // ── Carte hero profil ─────────────────────────────────────────
        ProfileHeroCard(progress = progress)

        // ── Stats en 3 colonnes ───────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("🔥", "${progress.currentStreak}", "Streak",      IvoryGold,       Modifier.weight(1f))
            StatTile("⭐", "${progress.totalXp}",        "XP Total",   KeysVioletLight, Modifier.weight(1f))
            StatTile("📚", "${progress.completedLessonIds.size}/${LessonCatalog.lessons.size}", "Leçons", NotesTeal, Modifier.weight(1f))
        }

        // ── Barre XP niveau ───────────────────────────────────────────
        XpLevelBar(progress = progress)

        // ── Badges ────────────────────────────────────────────────────
        SectionTitle("BADGES", "${unlockedBadgeIds.size} / ${BadgeSystem.allBadges.size}")
        BadgesGrid(allBadges = BadgeSystem.allBadges, unlockedIds = unlockedBadgeIds)

        // ── Reset ─────────────────────────────────────────────────────
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(EbonyCard)
                .border(1.dp, DissonanceRed.copy(0.25f), RoundedCornerShape(12.dp))
                .clickable { showResetDialog = true }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Réinitialiser la progression",
                color = DissonanceRed,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

// ── Carte hero ────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeroCard(progress: UserProgress) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(listOf(EbonyCard, EbonySurface))
            )
            .border(1.dp, EbonyBorder, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar animé
            val infiniteTransition = rememberInfiniteTransition(label = "avatar")
            val borderAlpha by infiniteTransition.animateFloat(
                initialValue  = 0.5f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse),
                label = "border_alpha"
            )
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(KeysVioletGlow)
                    .border(2.dp, KeysViolet.copy(borderAlpha), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🎹", style = MaterialTheme.typography.displaySmall)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(progress.userName, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                Text(levelTitle(progress.level), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(KeysVioletGlow)
                            .border(1.dp, KeysViolet.copy(0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Niveau ${progress.level}",
                            style = MaterialTheme.typography.labelSmall,
                            color = KeysVioletLight
                        )
                    }
                    if (progress.currentStreak >= 3) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(IvoryGoldGlow)
                                .border(1.dp, IvoryGold.copy(0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "🔥 ${progress.currentStreak}j",
                                style = MaterialTheme.typography.labelSmall,
                                color = IvoryGold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Barre XP ──────────────────────────────────────────────────────────────

@Composable
private fun XpLevelBar(progress: UserProgress) {
    var started by remember { mutableStateOf(false) }
    val animFrac by animateFloatAsState(
        targetValue   = if (started) progress.xpFraction else 0f,
        animationSpec = tween(1000),
        label         = "xp_bar"
    )
    LaunchedEffect(Unit) { started = true }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(EbonyCard)
            .border(1.dp, EbonyBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Niveau ${progress.level}", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Text(
                "${progress.xpProgressInLevel} / ${progress.xpNeededForNextLevel} XP",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(EbonySurface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animFrac)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(listOf(KeysViolet, IvoryGold))
                    )
            )
        }
        Text(
            "Prochain niveau : ${levelTitle(progress.level + 1)}",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

// ── Grille de badges ──────────────────────────────────────────────────────

@Composable
private fun BadgesGrid(allBadges: List<Badge>, unlockedIds: Set<String>) {
    // Grouper par rareté
    val grouped = BadgeRarity.entries.reversed().map { rarity ->
        rarity to allBadges.filter { it.rarity == rarity }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        grouped.forEach { (rarity, badges) ->
            val rarityColor = Color(BadgeSystem.getRarityColor(rarity))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Label rareté
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(rarityColor)
                    )
                    Text(
                        rarity.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = rarityColor
                    )
                }
                // Grille 3 colonnes
                val rows = badges.chunked(3)
                rows.forEach { rowBadges ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowBadges.forEach { badge ->
                            BadgeTile(
                                badge    = badge,
                                unlocked = badge.id in unlockedIds,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Remplir les cases vides
                        repeat(3 - rowBadges.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeTile(badge: Badge, unlocked: Boolean, modifier: Modifier = Modifier) {
    val accentColor = Color(BadgeSystem.getRarityColor(badge.rarity))
    val alpha = if (unlocked) 1f else 0.3f

    val infiniteTransition = rememberInfiniteTransition(label = "badge_${badge.id}")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.15f,
        targetValue   = if (unlocked) 0.35f else 0.15f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOut), RepeatMode.Reverse),
        label         = "badge_glow"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(EbonyCard)
            .border(
                1.dp,
                if (unlocked) accentColor.copy(0.4f) else EbonyBorder,
                RoundedCornerShape(14.dp)
            )
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (unlocked) accentColor.copy(glowAlpha) else EbonySurface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (unlocked) badge.emoji else "🔒",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary.copy(alpha = alpha)
            )
        }
        Text(
            badge.title,
            style     = MaterialTheme.typography.labelSmall,
            color     = TextPrimary.copy(alpha = alpha),
            textAlign = TextAlign.Center,
            maxLines  = 2
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(title: String, subtitle: String = "") {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        if (subtitle.isNotEmpty())
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}

@Composable
private fun StatTile(emoji: String, value: String, label: String, accent: Color, modifier: Modifier) {
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
        Text(value, style = MaterialTheme.typography.titleMedium, color = accent)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
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
