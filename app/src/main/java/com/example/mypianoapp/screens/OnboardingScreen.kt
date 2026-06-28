package com.example.mypianoapp.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: (name: String, dailyGoal: Int) -> Unit) {
    val pagerState    = rememberPagerState(pageCount = { 4 })
    val scope         = rememberCoroutineScope()
    var userName      by remember { mutableStateOf("") }
    var dailyGoal     by remember { mutableIntStateOf(10) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EbonyDeep)
    ) {
        // Glow de fond
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .blur(140.dp)
                .background(KeysVioletGlow, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .blur(120.dp)
                .background(IvoryGoldGlow, CircleShape)
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Pages ─────────────────────────────────────────────────
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> FeaturesPage()
                    2 -> NamePage(
                        userName  = userName,
                        onChange  = { userName = it }
                    )
                    3 -> GoalPage(
                        dailyGoal = dailyGoal,
                        onChange  = { dailyGoal = it }
                    )
                }
            }

            // ── Indicateurs de page ───────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { i ->
                    val isSelected = pagerState.currentPage == i
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(if (isSelected) 24.dp else 8.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) KeysViolet else EbonySurface)
                    )
                }
            }

            // ── Bouton principal ──────────────────────────────────────
            val keyboard = LocalSoftwareKeyboardController.current
            val isLastPage = pagerState.currentPage == 3

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(KeysViolet, Color(0xFF4C1D95)))
                    )
                    .clickable {
                        keyboard?.hide()
                        if (isLastPage) {
                            onComplete(
                                userName.trim().ifEmpty { "Pianiste" },
                                dailyGoal
                            )
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (pagerState.currentPage) {
                        0    -> "Commencer"
                        1    -> "Suivant"
                        2    -> if (userName.isBlank()) "Continuer sans nom" else "Suivant"
                        else -> "C'est parti ! 🎹"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Page 1 : Bienvenue ────────────────────────────────────────────────────

@Composable
private fun WelcomePage() {
    val infiniteTransition = rememberInfiniteTransition(label = "piano")
    val pianoScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.06f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOut), RepeatMode.Reverse),
        label         = "piano_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icône géante animée
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(KeysVioletGlow)
                .border(2.dp, KeysViolet.copy(0.5f), RoundedCornerShape(36.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🎹", style = MaterialTheme.typography.displayLarge)
        }

        Spacer(Modifier.height(36.dp))

        Text(
            "My Piano App",
            style     = MaterialTheme.typography.displaySmall,
            color     = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Apprends le piano à ton rythme,\nchaque jour un peu plus loin.",
            style     = MaterialTheme.typography.bodyLarge,
            color     = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        // 3 arguments clés
        listOf(
            "🎯" to "10 leçons progressives",
            "🎵" to "Exercices interactifs",
            "🏆" to "Badges & progression"
        ).forEach { (emoji, text) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(EbonyCard)
                        .border(1.dp, EbonyBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, style = MaterialTheme.typography.titleSmall)
                }
                Text(text, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }
}

// ── Page 2 : Fonctionnalités ──────────────────────────────────────────────

@Composable
private fun FeaturesPage() {
    val features = listOf(
        Triple("📚", "Parcours structuré", "10 leçons de zéro à tes premiers accords"),
        Triple("🎹", "Piano interactif", "Joue avec de vrais sons de piano acoustique"),
        Triple("🥁", "Exercices variés", "Reconnaissance de notes, gammes, rythme"),
        Triple("🔥", "Streak quotidien", "Pratique chaque jour pour garder ta flamme"),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Tout ce qu'il te faut",
            style     = MaterialTheme.typography.displaySmall,
            color     = TextPrimary,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Une app complète pour débutants",
            style     = MaterialTheme.typography.bodyMedium,
            color     = TextSecondary,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(32.dp))

        features.forEach { (emoji, title, desc) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(EbonyCard)
                    .border(1.dp, EbonyBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KeysVioletGlow)
                        .border(1.dp, KeysViolet.copy(0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, style = MaterialTheme.typography.titleLarge)
                }
                Column {
                    Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text(desc,  style = MaterialTheme.typography.bodySmall,  color = TextSecondary)
                }
            }
        }
    }
}

// ── Page 3 : Nom ──────────────────────────────────────────────────────────

@Composable
private fun NamePage(userName: String, onChange: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("👤", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(20.dp))
        Text(
            "Comment tu t'appelles ?",
            style     = MaterialTheme.typography.displaySmall,
            color     = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Ton prénom apparaîtra sur ton profil\net dans l'accueil.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value         = userName,
            onValueChange = { if (it.length <= 20) onChange(it) },
            modifier      = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder   = { Text("Ton prénom…", color = TextMuted) },
            singleLine    = true,
            shape         = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction      = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusRequester.freeFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = KeysViolet,
                unfocusedBorderColor = EbonyBorder,
                focusedTextColor     = TextPrimary,
                unfocusedTextColor   = TextPrimary,
                cursorColor          = KeysViolet,
                focusedContainerColor   = EbonyCard,
                unfocusedContainerColor = EbonyCard
            ),
            textStyle = MaterialTheme.typography.titleMedium
        )

        if (userName.isNotBlank()) {
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(KeysVioletGlow)
                    .border(1.dp, KeysViolet.copy(0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    "Bonjour, $userName ! 👋",
                    style = MaterialTheme.typography.titleSmall,
                    color = KeysVioletLight
                )
            }
        }
    }
}

// ── Page 4 : Objectif quotidien ───────────────────────────────────────────

@Composable
private fun GoalPage(dailyGoal: Int, onChange: (Int) -> Unit) {
    val goals = listOf(5 to "Rapide", 10 to "Régulier", 15 to "Sérieux", 20 to "Intense")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⏱", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(20.dp))
        Text(
            "Ton objectif quotidien",
            style     = MaterialTheme.typography.displaySmall,
            color     = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Combien de minutes par jour\nveux-tu consacrer au piano ?",
            style     = MaterialTheme.typography.bodyMedium,
            color     = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        // Grille 2×2
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            goals.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { (minutes, label) ->
                        val selected = dailyGoal == minutes
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selected) KeysViolet else EbonyCard)
                                .border(
                                    2.dp,
                                    if (selected) KeysVioletLight.copy(0.5f) else EbonyBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onChange(minutes) }
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "$minutes min",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = if (selected) TextPrimary else TextSecondary
                                )
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) TextPrimary.copy(0.7f) else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Tu pourras modifier cet objectif\nplus tard dans ton profil.",
            style     = MaterialTheme.typography.bodySmall,
            color     = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}
