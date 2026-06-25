package com.example.mypianoapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.ui.theme.*

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EbonyDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Profil", style = MaterialTheme.typography.displaySmall, color = TextPrimary)

        // Avatar + nom
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(KeysVioletGlow)
                    .border(2.dp, KeysViolet, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🎹", style = MaterialTheme.typography.displaySmall)
            }
            Column {
                Text("Alex", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                Text("Pianiste débutant", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }

        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileStat("3", "Jours\nconsécutifs", IvoryGold, Modifier.weight(1f))
            ProfileStat("12%", "Progression\nglobale", KeysVioletLight, Modifier.weight(1f))
            ProfileStat("25 XP", "Points\nobtenus", NotesTeal, Modifier.weight(1f))
        }

        // Accomplissements
        Text("ACCOMPLISSEMENTS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        listOf(
            "🏅" to "Première leçon complète",
            "🔥" to "3 jours d'affilée",
            "🎵" to "10 notes jouées",
        ).forEach { (emoji, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(EbonyCard)
                    .border(1.dp, EbonyBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emoji, style = MaterialTheme.typography.titleLarge)
                Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            }
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String, accent: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(EbonyCard)
            .border(1.dp, EbonyBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = accent)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
