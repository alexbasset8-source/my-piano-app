package com.example.mypianoapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mypianoapp.audio.PianoSoundEngine
import com.example.mypianoapp.game.GamePhase
import com.example.mypianoapp.game.GoblinGameEngine
import com.example.mypianoapp.game.WHITE_NOTES
import com.example.mypianoapp.ui.theme.*

@Composable
fun GoblinGameScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    val engine      = remember { GoblinGameEngine(scope) }
    val state       = engine.state
    val soundEngine = remember { PianoSoundEngine(context) }
    var soundReady  by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { soundEngine.preload(); soundReady = true }
    DisposableEffect(Unit) { onDispose { soundEngine.release() } }

    // Détection portrait — inviter à tourner le téléphone
    val configuration = LocalConfiguration.current
    val isLandscape   = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D1A))
        ) {

            when (state.phase) {
                GamePhase.MENU      -> GameMenuOverlay(onStart = { engine.startGame() }, onBack = onBack)
                GamePhase.GAME_OVER -> GameOverOverlay(
                    score    = state.score,
                    wave     = state.wave,
                    maxCombo = state.maxCombo,
                    onRestart = { engine.restartGame() },
                    onBack   = onBack
                )
                GamePhase.VICTORY   -> VictoryOverlay(
                    score    = state.score,
                    maxCombo = state.maxCombo,
                    onRestart = { engine.restartGame() },
                    onBack   = onBack
                )
                else -> {
                    // ── Zone de jeu ───────────────────────────────────────────────
                    Column(modifier = Modifier.fillMaxSize()) {

                        // HUD
                        GameHud(
                            wave   = state.wave,
                            score  = state.score,
                            lives  = state.lives,
                            maxLives = state.maxLives,
                            combo  = state.combo,
                            onBack = onBack
                        )

                        // Champ de bataille
                        BattleField(
                            goblins = state.goblins,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )

                        // Clavier 1 octave
                        GameKeyboard(
                            flashNote    = state.flashNote,
                            flashCorrect = state.flashCorrect,
                            onNotePlay   = { note: String ->
                                if (soundReady) soundEngine.play(note)
                                engine.playNote(note)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                        )
                    }

                    // Overlays de phase
                    AnimatedVisibility(
                        visible = state.phase == GamePhase.COUNTDOWN,
                        enter   = scaleIn() + fadeIn(),
                        exit    = scaleOut() + fadeOut()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${state.countdown}",
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp),
                                color = IvoryGold
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = state.phase == GamePhase.WAVE_CLEAR,
                        enter   = scaleIn(tween(300, easing = EaseOutBack)) + fadeIn(),
                        exit    = fadeOut(tween(400))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⚔️", fontSize = 56.sp)
                                Text(
                                    "Vague ${state.wave} survivée !",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = HarmonyGreen
                                )
                                Text(
                                    "Prépare-toi...",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        RotatePrompt(onBack = onBack)
    }
}

// ── HUD ───────────────────────────────────────────────────────────────────

@Composable
private fun GameHud(
    wave: Int, score: Int, lives: Int, maxLives: Int, combo: Int, onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A14))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Retour
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(EbonySurface)
                .pointerInput(Unit) { detectTapGestures { onBack() } },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }

        // Vague
        HudChip("Vague $wave / 5", KeysViolet)

        // Score
        HudChip("⭐ $score", IvoryGold)

        // Combo
        if (combo > 1) HudChip("🔥 ×$combo", NotesTeal)

        Spacer(Modifier.weight(1f))

        // Vies
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(maxLives) { i ->
                Icon(
                    imageVector = if (i < lives) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint     = if (i < lives) DissonanceRed else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun HudChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(0.15f))
            .border(1.dp, color.copy(0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

// ── Champ de bataille ─────────────────────────────────────────────────────

@Composable
private fun BattleField(
    goblins: List<com.example.mypianoapp.game.Goblin>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    // Palette gobelin par note
    val noteColors = mapOf(
        "Do" to Color(0xFF7C3AED), "Do#" to Color(0xFF9D5FF5),
        "Ré" to Color(0xFF06B6D4), "Ré#" to Color(0xFF0EA5E9),
        "Mi" to Color(0xFF22C55E),
        "Fa" to Color(0xFFF59E0B), "Fa#" to Color(0xFFF97316),
        "Sol" to Color(0xFFEF4444), "Sol#" to Color(0xFFEC4899),
        "La" to Color(0xFF8B5CF6), "La#" to Color(0xFFA78BFA),
        "Si" to Color(0xFF14B8A6)
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Sol
        drawRect(
            color  = Color(0xFF1A1A2E),
            topLeft = Offset(0f, h * 0.82f),
            size   = Size(w, h * 0.18f)
        )
        // Ligne de sol
        drawLine(
            color       = Color(0xFF3A3A5C),
            start       = Offset(0f, h * 0.82f),
            end         = Offset(w, h * 0.82f),
            strokeWidth = 2f
        )

        // Barricade à gauche (côté clavier)
        drawBarricade(w, h)

        // Gobelins vivants
        goblins.filter { it.alive }.forEach { goblin ->
            val gx  = w * (1f - goblin.x)   // 0=droite, grandit vers la gauche
            val gy  = h * 0.65f              // position verticale fixe sur le sol
            val col = noteColors[goblin.note] ?: KeysViolet

            drawGoblin(
                cx        = gx,
                cy        = gy,
                color     = col,
                note      = goblin.note,
                textMeasurer = textMeasurer
            )
        }
    }
}

private fun DrawScope.drawBarricade(w: Float, h: Float) {
    val bx = w * 0.06f
    val by = h * 0.30f
    val bh = h * 0.52f
    val bw = 16f
    // Planches
    repeat(4) { i ->
        val py = by + i * (bh / 4)
        drawRect(
            color    = Color(0xFF8B6914),
            topLeft  = Offset(bx - bw / 2, py),
            size     = Size(bw, bh / 4 - 4f)
        )
    }
    // Clous
    repeat(4) { i ->
        drawCircle(
            color  = Color(0xFFCCCCCC),
            radius = 3f,
            center = Offset(bx, by + i * (bh / 4) + 8f)
        )
    }
}

private fun DrawScope.drawGoblin(
    cx: Float, cy: Float, color: Color, note: String, textMeasurer: TextMeasurer
) {
    val r = 18f  // rayon corps

    // Ombre
    drawCircle(Color.Black.copy(0.3f), r + 2f, Offset(cx + 3, cy + 3))

    // Corps
    drawCircle(color, r, Offset(cx, cy))
    drawCircle(color.copy(0.3f), r, Offset(cx, cy), style = Stroke(2f))

    // Yeux
    drawCircle(Color.White, 4f, Offset(cx - 6f, cy - 4f))
    drawCircle(Color.White, 4f, Offset(cx + 6f, cy - 4f))
    drawCircle(Color.Black, 2f, Offset(cx - 6f, cy - 4f))
    drawCircle(Color.Black, 2f, Offset(cx + 6f, cy - 4f))

    // Oreilles pointues
    val earPath = Path().apply {
        moveTo(cx - r + 2f, cy - r + 6f)
        lineTo(cx - r - 6f, cy - r - 10f)
        lineTo(cx - r + 10f, cy - r + 2f)
        close()
    }
    val earPath2 = Path().apply {
        moveTo(cx + r - 2f, cy - r + 6f)
        lineTo(cx + r + 6f, cy - r - 10f)
        lineTo(cx + r - 10f, cy - r + 2f)
        close()
    }
    drawPath(earPath, color)
    drawPath(earPath2, color)

    // Jambes
    drawLine(color.copy(0.8f), Offset(cx - 6f, cy + r), Offset(cx - 8f, cy + r + 14f), 4f)
    drawLine(color.copy(0.8f), Offset(cx + 6f, cy + r), Offset(cx + 8f, cy + r + 14f), 4f)

    // Arme (épée pixelisée)
    drawLine(Color(0xFFCCCCCC), Offset(cx + r, cy - 4f), Offset(cx + r + 16f, cy - 20f), 3f)
    drawLine(Color(0xFFCCCCCC), Offset(cx + r + 6f, cy - 8f), Offset(cx + r + 14f, cy), 3f)

    // Badge note au-dessus
    val measured = textMeasurer.measure(
        text  = AnnotatedString(note),
        style = TextStyle(
            fontSize   = 10.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.White
        )
    )
    val badgeW = measured.size.width + 12f
    val badgeH = measured.size.height + 8f
    val bx     = cx - badgeW / 2
    val by     = cy - r - badgeH - 8f

    drawRoundRect(
        color       = color,
        topLeft     = Offset(bx, by),
        size        = Size(badgeW, badgeH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
    )
    drawText(
        textMeasurer = textMeasurer,
        text         = note,
        topLeft      = Offset(bx + 6f, by + 4f),
        style        = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
    )
}

// ── Clavier de jeu ────────────────────────────────────────────────────────

@Composable
private fun GameKeyboard(
    flashNote: String?,
    flashCorrect: Boolean,
    onNotePlay: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val whiteKeys = WHITE_NOTES
    val blackKeys = listOf(0 to "Do#", 1 to "Ré#", 3 to "Fa#", 4 to "Sol#", 5 to "La#")

    Box(
        modifier = modifier
            .background(Color(0xFF0A0A14))
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val totalWidth  = this.maxWidth
            val whiteCount  = whiteKeys.size
            val whiteWidth  = totalWidth / whiteCount
            val gap         = 2.dp
            val blackWidth  = whiteWidth * 0.62f
            val blackHeight = this.maxHeight * 0.58f

            // Touches blanches
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                whiteKeys.forEach { note ->
                    val isFlash   = flashNote == note
                    val flashColor = if (flashCorrect) HarmonyGreen else DissonanceRed
                    val bgColor by animateColorAsState(
                        targetValue = if (isFlash) flashColor else Color.White.copy(0.92f),
                        animationSpec = tween(100), label = "wk_$note"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                            .background(bgColor)
                            .border(
                                1.dp,
                                if (isFlash) flashColor.copy(0.6f) else Color.Gray.copy(0.3f),
                                RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
                            )
                            .pointerInput(note) {
                                detectTapGestures(onPress = {
                                    onNotePlay(note)
                                    tryAwaitRelease()
                                })
                            },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            note,
                            fontSize = 9.sp,
                            color    = Color.Black.copy(0.7f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }

            // Touches noires
            blackKeys.forEach { (wIndex, noteName) ->
                val isFlash    = flashNote == noteName
                val flashColor = if (flashCorrect) HarmonyGreen else DissonanceRed
                val leftOffset = whiteWidth * wIndex + whiteWidth * 0.69f + gap * wIndex
                val bgColor by animateColorAsState(
                    targetValue   = if (isFlash) flashColor else Color(0xFF1A1A1A),
                    animationSpec = tween(100), label = "bk_$noteName"
                )
                Box(
                    modifier = Modifier
                        .offset(x = leftOffset)
                        .width(blackWidth)
                        .height(blackHeight)
                        .clip(RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
                        .background(bgColor)
                        .border(
                            1.dp,
                            if (isFlash) flashColor else Color.DarkGray,
                            RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp)
                        )
                        .pointerInput(noteName) {
                            detectTapGestures(onPress = {
                                onNotePlay(noteName)
                                tryAwaitRelease()
                            })
                        }
                )
            }
        }
    }
}

// ── Overlays de menu / game over / victoire ───────────────────────────────

@Composable
private fun GameMenuOverlay(onStart: () -> Unit, onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D1A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("👺", fontSize = 64.sp)
            Text(
                "Goblin Attack !",
                style     = MaterialTheme.typography.displaySmall,
                color     = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                "Joue la bonne note pour tuer les gobelins\navant qu'ils atteignent ta barricade !",
                style     = MaterialTheme.typography.bodyMedium,
                color     = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))

            // Règles rapides
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(EbonyCard)
                    .border(1.dp, EbonyBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "❤️ 3 vies — un gobelin qui passe = -1 vie",
                    "🎵 Chaque gobelin porte une note à jouer",
                    "🔥 Combo = points bonus (max ×5)",
                    "⚔️ 5 vagues — les noires arrivent à la vague 3"
                ).forEach { rule ->
                    Text(rule, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(EbonySurface)
                        .border(1.dp, EbonyBorder, RoundedCornerShape(12.dp))
                        .pointerInput(Unit) { detectTapGestures { onBack() } }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text("Retour", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(KeysViolet, Color(0xFF4C1D95)))
                        )
                        .pointerInput(Unit) { detectTapGestures { onStart() } }
                        .padding(horizontal = 32.dp, vertical = 12.dp)
                ) {
                    Text("Jouer !", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun GameOverOverlay(
    score: Int, wave: Int, maxCombo: Int,
    onRestart: () -> Unit, onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("💀", fontSize = 56.sp)
            Text("Défaite !", style = MaterialTheme.typography.displaySmall, color = DissonanceRed)
            Text(
                "Tu as survécu jusqu'à la vague $wave",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            ScoreSummary(score = score, maxCombo = maxCombo)
            GameEndButtons(onRestart = onRestart, onBack = onBack)
        }
    }
}

@Composable
private fun VictoryOverlay(
    score: Int, maxCombo: Int, onRestart: () -> Unit, onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("🏆", fontSize = 56.sp)
            Text("Victoire !", style = MaterialTheme.typography.displaySmall, color = IvoryGold)
            Text(
                "Toutes les vagues repoussées !",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            ScoreSummary(score = score, maxCombo = maxCombo)
            GameEndButtons(onRestart = onRestart, onBack = onBack)
        }
    }
}

@Composable
private fun ScoreSummary(score: Int, maxCombo: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(EbonyCard)
            .border(1.dp, EbonyBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⭐ $score", style = MaterialTheme.typography.headlineMedium, color = IvoryGold)
            Text("Score", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🔥 ×$maxCombo", style = MaterialTheme.typography.headlineMedium, color = NotesTeal)
            Text("Combo max", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun GameEndButtons(onRestart: () -> Unit, onBack: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(EbonySurface)
                .border(1.dp, EbonyBorder, RoundedCornerShape(12.dp))
                .pointerInput(Unit) { detectTapGestures { onBack() } }
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text("Quitter", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(KeysViolet, Color(0xFF4C1D95))))
                .pointerInput(Unit) { detectTapGestures { onRestart() } }
                .padding(horizontal = 28.dp, vertical = 12.dp)
        ) {
            Text("Rejouer", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
        }
    }
}

@Composable
private fun RotatePrompt(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text("🔄", fontSize = 64.sp)
            Text(
                "Rotation requise",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                "Ce jeu se joue en mode paysage pour un meilleur confort.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(EbonySurface)
                    .border(1.dp, EbonyBorder, RoundedCornerShape(12.dp))
                    .pointerInput(Unit) { detectTapGestures { onBack() } }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text("Retour", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            }
        }
    }
}
