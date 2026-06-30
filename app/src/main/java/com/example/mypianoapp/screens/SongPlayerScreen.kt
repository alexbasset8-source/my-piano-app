package com.example.mypianoapp.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mypianoapp.audio.PianoSoundEngine
import com.example.mypianoapp.song.*
import com.example.mypianoapp.ui.theme.*

// ── Couleurs par note ─────────────────────────────────────────────────────
private val noteColorMap = mapOf(
    "Do"  to Color(0xFF7C3AED), "Do#" to Color(0xFF9D5FF5),
    "Ré"  to Color(0xFF06B6D4), "Ré#" to Color(0xFF0EA5E9),
    "Mi"  to Color(0xFF22C55E),
    "Fa"  to Color(0xFFF59E0B), "Fa#" to Color(0xFFF97316),
    "Sol" to Color(0xFFEF4444), "Sol#" to Color(0xFFEC4899),
    "La"  to Color(0xFF8B5CF6), "La#" to Color(0xFFA78BFA),
    "Si"  to Color(0xFF14B8A6)
)
private fun noteColor(note: String?) = noteColorMap[note] ?: Color(0xFF7C3AED)

// ── Mapping note → index colonne clavier (blanches 0-6) ──────────────────
private val noteToWhiteIndex = mapOf(
    "Do" to 0, "Ré" to 1, "Mi" to 2, "Fa" to 3,
    "Sol" to 4, "La" to 5, "Si" to 6
)
private val noteToBlackIndex = mapOf(
    "Do#" to 0, "Ré#" to 1, "Fa#" to 3, "Sol#" to 4, "La#" to 5
)

@Composable
fun SongPlayerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    val soundEngine = remember { PianoSoundEngine(context) }
    var soundReady  by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { soundEngine.preload(); soundReady = true }
    DisposableEffect(Unit) { onDispose { soundEngine.release() } }

    val engine = remember { SongPlayerEngine(lettrAElise, soundEngine, scope) }
    val state  = engine.state

    when (state.phase) {
        PlayerPhase.IDLE     -> SongMenuScreen(
            song       = lettrAElise,
            difficulty = state.difficulty,
            onSelect   = { engine.selectDifficulty(it) },
            onStart    = { engine.start() },
            onBack     = onBack
        )
        PlayerPhase.FINISHED -> SongResultScreen(
            song      = lettrAElise,
            state     = state,
            accuracy  = engine.accuracy(),
            onRestart = { engine.restart() },
            onBack    = onBack
        )
        else -> SongPlayView(
            engine  = engine,
            state   = state,
            onBack  = onBack
        )
    }
}

// ── Vue de jeu principale ─────────────────────────────────────────────────

@Composable
private fun SongPlayView(
    engine: SongPlayerEngine,
    state: SongPlayerState,
    onBack: () -> Unit
) {
    val visibleBeats = when (state.difficulty) {
        Difficulty.EASY   -> 7f
        Difficulty.NORMAL -> 5f
    }
    val visibleNotes = remember(state.currentBeat) {
        engine.visibleNotes(visibleBeats)
    }

    Box(modifier = Modifier.fillMaxSize().background(EbonyDeep)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── HUD ───────────────────────────────────────────────────
            SongHud(state = state, onPause = { engine.pause() }, onBack = onBack)

            // ── Zone de défilement ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                NoteHighway(
                    currentBeat  = state.currentBeat,
                    visibleBeats = visibleBeats,
                    notes        = visibleNotes,
                    difficulty   = state.difficulty,
                    modifier     = Modifier.fillMaxSize()
                )

                // Feedback "Perfect / Good / Miss"
                state.lastFeedback?.let { fb ->
                    FeedbackBadge(feedback = fb, modifier = Modifier.align(Alignment.Center))
                }

                // Overlay pause / countdown
                when (state.phase) {
                    PlayerPhase.PAUSED -> PauseOverlay(onResume = { engine.pause() }, onRestart = { engine.restart() })
                    PlayerPhase.COUNTDOWN -> CountdownOverlay(count = state.countdown)
                    else -> {}
                }
            }

            // ── Clavier ───────────────────────────────────────────────
            SongKeyboard(
                currentBeat  = state.currentBeat,
                activeNotes  = visibleNotes.filter {
                    it.hand == Hand.RIGHT &&
                    kotlin.math.abs(it.beatStart - state.currentBeat) < 0.4f
                }.map { it.note ?: "" },
                onNotePlay   = { note -> engine.onNoteHit(note) },
                modifier     = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
            )
        }
    }
}

// ── Autoroute de notes (Guitar Hero highway) ──────────────────────────────

@Composable
private fun NoteHighway(
    currentBeat: Float,
    visibleBeats: Float,
    notes: List<SongNote>,
    difficulty: Difficulty,
    modifier: Modifier = Modifier
) {
    val whiteCount = 7
    val scrollSpeed = difficulty.scrollSpeed

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val colW = w / whiteCount  // largeur d'une colonne blanche

        // ── Fond ────────────────────────────────────────────────────
        drawRect(Color(0xFF0A0A18))

        // Lignes de colonnes verticales
        for (i in 0..whiteCount) {
            drawLine(
                color       = Color(0xFF1E1E3A),
                start       = Offset(i * colW, 0f),
                end         = Offset(i * colW, h),
                strokeWidth = 1f
            )
        }

        // Lignes horizontales de mesure (grille rythmique)
        val beatsVisible = visibleBeats + 1f
        val pixPerBeat   = h / beatsVisible
        for (b in 0..beatsVisible.toInt()) {
            val isMeasure = (b + currentBeat.toInt()) % 3 == 0
            val lineY     = h - b * pixPerBeat + (currentBeat % 1f) * pixPerBeat
            if (lineY in 0f..h) {
                drawLine(
                    color       = if (isMeasure) Color(0xFF2A2A4A) else Color(0xFF141428),
                    start       = Offset(0f, lineY),
                    end         = Offset(w, lineY),
                    strokeWidth = if (isMeasure) 2f else 1f
                )
            }
        }

        // Ligne de frappe (hit line) à 15% du bas
        val hitY = h * 0.88f
        drawLine(
            color       = Color(0xFF4040AA),
            start       = Offset(0f, hitY),
            end         = Offset(w, hitY),
            strokeWidth = 2f
        )
        // Glow ligne de frappe
        drawLine(
            brush       = Brush.horizontalGradient(
                listOf(Color.Transparent, Color(0x607C3AED), Color.Transparent)
            ),
            start       = Offset(0f, hitY),
            end         = Offset(w, hitY),
            strokeWidth = 8f
        )

        // ── Notes ────────────────────────────────────────────────────
        notes.forEach { note ->
            if (note.note == null) return@forEach

            val beatsFromNow = note.beatStart - currentBeat
            // Y : notes futures arrivent du haut, hit line en bas
            val noteY  = hitY - beatsFromNow * pixPerBeat
            val noteH  = (note.duration * pixPerBeat).coerceAtLeast(14f)
            val color  = noteColor(note.note)

            if (note.hand == Hand.LEFT) {
                // MG : affichage en gris transparent (info uniquement)
                drawLeftNote(note.note, noteY, noteH, colW, color.copy(alpha = 0.35f))
            } else {
                // MD : bloc coloré plein
                drawRightNote(note.note, noteY, noteH, colW, color, hitY)
            }
        }

        // ── Indicateurs de colonne en bas ────────────────────────────
        for (i in 0 until whiteCount) {
            val cx = i * colW + colW / 2
            drawCircle(
                color  = Color(0xFF2A2A5A),
                radius = colW * 0.3f,
                center = Offset(cx, hitY)
            )
        }
    }
}

private fun DrawScope.drawRightNote(
    note: String, yTop: Float, height: Float, colW: Float, color: Color, hitY: Float
) {
    val isBlack = note.endsWith("#")
    val x: Float
    val w: Float

    if (!isBlack) {
        val idx = noteToWhiteIndex[note] ?: return
        x = idx * colW + 4f
        w = colW - 8f
    } else {
        val idx = noteToBlackIndex[note] ?: return
        x = idx * colW + colW * 0.6f
        w = colW * 0.5f
    }

    val isAtHit = yTop in (hitY - 20f)..(hitY + 20f)

    // Corps de la note
    drawRoundRect(
        brush       = if (isAtHit)
            Brush.verticalGradient(listOf(color, color.copy(0.6f)), startY = yTop, endY = yTop + height)
        else
            Brush.verticalGradient(listOf(color.copy(0.9f), color.copy(0.5f)), startY = yTop, endY = yTop + height),
        topLeft     = Offset(x, yTop - height),
        size        = Size(w, height),
        cornerRadius = CornerRadius(6f)
    )

    // Contour lumineux
    drawRoundRect(
        color        = if (isAtHit) Color.White.copy(0.7f) else color.copy(0.8f),
        topLeft      = Offset(x, yTop - height),
        size         = Size(w, height),
        cornerRadius = CornerRadius(6f),
        style        = Stroke(2f)
    )

    // Glow si à la ligne de frappe
    if (isAtHit) {
        drawRoundRect(
            color        = color.copy(0.3f),
            topLeft      = Offset(x - 4f, yTop - height - 4f),
            size         = Size(w + 8f, height + 8f),
            cornerRadius = CornerRadius(10f)
        )
    }
}

private fun DrawScope.drawLeftNote(
    note: String, yTop: Float, height: Float, colW: Float, color: Color
) {
    val isBlack = note.endsWith("#")
    val x: Float
    val w: Float

    if (!isBlack) {
        val idx = noteToWhiteIndex[note] ?: return
        x = idx * colW + 4f
        w = colW - 8f
    } else {
        val idx = noteToBlackIndex[note] ?: return
        x = idx * colW + colW * 0.6f
        w = colW * 0.5f
    }

    // MG : rectangle semi-transparent avec tirets
    drawRoundRect(
        color        = color,
        topLeft      = Offset(x + 2f, yTop - height),
        size         = Size(w - 4f, height),
        cornerRadius = CornerRadius(4f),
        style        = Stroke(1.5f)
    )
}

// ── HUD ───────────────────────────────────────────────────────────────────

@Composable
private fun SongHud(
    state: SongPlayerState,
    onPause: () -> Unit,
    onBack: () -> Unit
) {
    val totalNotes = state.perfectCount + state.goodCount + state.missCount
    val pct = if (totalNotes == 0) 100
    else ((state.perfectCount * 100 + state.goodCount * 60) / totalNotes).coerceIn(0, 100)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A18))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            Icon(Icons.Default.ArrowBack, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }

        // Score
        HudPill("⭐ ${state.score}", KeysViolet)

        // Combo
        if (state.combo > 1) HudPill("🔥 ×${state.combo}", NotesTeal)

        // Précision
        HudPill("$pct%", when {
            pct >= 90 -> HarmonyGreen
            pct >= 70 -> IvoryGold
            else      -> DissonanceRed
        })

        Spacer(Modifier.weight(1f))

        // Difficulté
        HudPill(state.difficulty.label, TextSecondary)

        // Pause
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(EbonySurface)
                .pointerInput(Unit) { detectTapGestures { onPause() } },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (state.phase == PlayerPhase.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                null, tint = TextSecondary, modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun HudPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(0.15f))
            .border(1.dp, color.copy(0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

// ── Clavier ───────────────────────────────────────────────────────────────

@Composable
private fun SongKeyboard(
    currentBeat: Float,
    activeNotes: List<String>,      // notes arrivant à la hit line
    onNotePlay: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val whiteKeys = listOf("Do", "Ré", "Mi", "Fa", "Sol", "La", "Si")
    val blackKeys = listOf(0 to "Do#", 1 to "Ré#", 3 to "Fa#", 4 to "Sol#", 5 to "La#")

    Box(
        modifier = modifier
            .background(Color(0xFF07070F))
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val totalWidth  = this.maxWidth
            val whiteWidth  = totalWidth / whiteKeys.size
            val gap         = 2.dp
            val blackWidth  = whiteWidth * 0.62f
            val blackHeight = this.maxHeight * 0.58f

            // Touches blanches
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                whiteKeys.forEach { note ->
                    val isActive  = activeNotes.contains(note)
                    val col       = noteColor(note)
                    val bgColor by animateColorAsState(
                        targetValue   = if (isActive) col.copy(0.85f) else Color.White.copy(0.92f),
                        animationSpec = tween(80), label = "wk_$note"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                            .background(bgColor)
                            .border(
                                width = if (isActive) 2.dp else 1.dp,
                                color = if (isActive) col else Color.Gray.copy(0.3f),
                                shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
                            )
                            .pointerInput(note) {
                                detectTapGestures(onPress = {
                                    onNotePlay(note)
                                    tryAwaitRelease()
                                })
                            },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (isActive) {
                            // Pastille colorée en haut de la touche active
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .align(Alignment.TopCenter)
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(col)
                            )
                        }
                        Text(
                            note,
                            fontSize = 9.sp,
                            color    = if (isActive) Color.White else Color.Black.copy(0.6f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
            }

            // Touches noires
            blackKeys.forEach { (wIndex, noteName) ->
                val isActive   = activeNotes.contains(noteName)
                val col        = noteColor(noteName)
                val leftOffset = whiteWidth * wIndex + whiteWidth * 0.69f + gap * wIndex
                val bgColor by animateColorAsState(
                    targetValue   = if (isActive) col else Color(0xFF1A1A1A),
                    animationSpec = tween(80), label = "bk_$noteName"
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
                            if (isActive) col.copy(0.8f) else Color.DarkGray,
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

// ── Badge de feedback ─────────────────────────────────────────────────────

@Composable
private fun FeedbackBadge(feedback: FeedbackEvent, modifier: Modifier = Modifier) {
    val (text, color) = when (feedback.type) {
        FeedbackType.PERFECT -> "PERFECT ✨" to IvoryGold
        FeedbackType.GOOD    -> "GOOD 👍"    to HarmonyGreen
        FeedbackType.MISS    -> "MISS ✗"     to DissonanceRed
    }
    AnimatedVisibility(
        visible = true,
        enter   = scaleIn(tween(120, easing = EaseOutBack)) + fadeIn(tween(100)),
        exit    = fadeOut(tween(200)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(0.2f))
                .border(2.dp, color.copy(0.6f), RoundedCornerShape(12.dp))
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(text, style = MaterialTheme.typography.titleLarge, color = color)
        }
    }
}

// ── Overlay Pause ─────────────────────────────────────────────────────────

@Composable
private fun PauseOverlay(onResume: () -> Unit, onRestart: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("⏸ En pause", style = MaterialTheme.typography.displaySmall, color = TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GamePillBtn("Reprendre", KeysViolet, onResume)
                GamePillBtn("Recommencer", EbonySurface, onRestart)
            }
        }
    }
}

@Composable
private fun CountdownOverlay(count: Int) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.55f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$count",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp),
            color = IvoryGold
        )
    }
}

@Composable
private fun GamePillBtn(label: String, bg: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, bg.copy(0.5f), RoundedCornerShape(12.dp))
            .pointerInput(Unit) { detectTapGestures { onClick() } }
            .padding(horizontal = 22.dp, vertical = 12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
    }
}

// ── Menu sélection ────────────────────────────────────────────────────────

@Composable
private fun SongMenuScreen(
    song: Song,
    difficulty: Difficulty,
    onSelect: (Difficulty) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EbonyDeep)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(EbonySurface)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        Text("🎹", fontSize = 56.sp)
        Text(song.title, style = MaterialTheme.typography.displaySmall, color = TextPrimary, textAlign = TextAlign.Center)
        Text(song.composer, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(EbonyCard)
                .border(1.dp, EbonyBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Comment jouer :", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            listOf(
                "🎵 Les blocs colorés tombent vers le bas",
                "⌨️ Joue la note quand le bloc arrive en bas",
                "🤖 La main gauche est jouée automatiquement",
                "👻 Les blocs transparents = main gauche (info)"
            ).forEach {
                Text(it, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        // Difficulté
        Text("DIFFICULTÉ", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Difficulty.entries.forEach { d ->
                val selected = d == difficulty
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) KeysViolet else EbonyCard)
                        .border(
                            2.dp,
                            if (selected) KeysVioletLight.copy(0.5f) else EbonyBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelect(d) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(d.label, style = MaterialTheme.typography.titleSmall, color = if (selected) TextPrimary else TextSecondary)
                        Text(
                            "${(song.bpmBase * d.bpmMultiplier).toInt()} BPM",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) TextPrimary.copy(0.7f) else TextMuted
                        )
                        Text(
                            "Tolérance ${d.timingWindowMs}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) TextPrimary.copy(0.6f) else TextMuted
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(KeysViolet, Color(0xFF4C1D95))))
                .clickable(onClick = onStart),
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlayArrow, null, tint = TextPrimary, modifier = Modifier.size(22.dp))
                Text("Jouer", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
            }
        }
    }
}

// ── Écran de résultats ────────────────────────────────────────────────────

@Composable
private fun SongResultScreen(
    song: Song,
    state: SongPlayerState,
    accuracy: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    val grade = when {
        accuracy >= 95 -> "S" to IvoryGold
        accuracy >= 80 -> "A" to HarmonyGreen
        accuracy >= 65 -> "B" to NotesTeal
        accuracy >= 50 -> "C" to KeysVioletLight
        else           -> "D" to DissonanceRed
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EbonyDeep)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Bravo !", style = MaterialTheme.typography.displaySmall, color = TextPrimary)
        Text(song.title, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

        // Grade
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(grade.second.copy(0.15f))
                .border(2.dp, grade.second.copy(0.5f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(grade.first, style = MaterialTheme.typography.displayLarge, color = grade.second)
        }

        // Stats
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatPill("⭐",  "${state.score}",        "Score",   KeysVioletLight, Modifier.weight(1f))
            StatPill("🎯",  "$accuracy%",            "Précision", IvoryGold,    Modifier.weight(1f))
            StatPill("🔥",  "×${state.maxCombo}",    "Combo max", NotesTeal,    Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatPill("✨", "${state.perfectCount}", "Perfect",  IvoryGold,      Modifier.weight(1f))
            StatPill("👍", "${state.goodCount}",    "Good",     HarmonyGreen,   Modifier.weight(1f))
            StatPill("✗",  "${state.missCount}",    "Miss",     DissonanceRed,  Modifier.weight(1f))
        }

        Spacer(Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(14.dp))
                    .background(EbonySurface).border(1.dp, EbonyBorder, RoundedCornerShape(14.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) { Text("Retour", style = MaterialTheme.typography.labelLarge, color = TextSecondary) }
            Box(
                modifier = Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(KeysViolet, Color(0xFF4C1D95))))
                    .clickable(onClick = onRestart),
                contentAlignment = Alignment.Center
            ) { Text("Rejouer", style = MaterialTheme.typography.labelLarge, color = TextPrimary) }
        }
    }
}

@Composable
private fun StatPill(emoji: String, value: String, label: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(EbonyCard)
            .border(1.dp, EbonyBorder, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(emoji, fontSize = 16.sp)
        Text(value, style = MaterialTheme.typography.titleMedium, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
