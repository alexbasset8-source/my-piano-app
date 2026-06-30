package com.example.mypianoapp.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.audio.MetronomeEngine
import com.example.mypianoapp.audio.PianoSoundEngine
import com.example.mypianoapp.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PianoScreen(
    onMinutesPlayed: (Int) -> Unit,
    onPlaySong: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    // ── Audio ─────────────────────────────────────────────────────────
    val soundEngine    = remember { PianoSoundEngine(context) }
    val metronome      = remember { MetronomeEngine() }
    var soundReady     by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { soundEngine.preload(); soundReady = true }
    DisposableEffect(Unit) { onDispose { soundEngine.release(); metronome.release() } }

    // ── Clavier ───────────────────────────────────────────────────────
    val whiteKeys = listOf("Do", "Ré", "Mi", "Fa", "Sol", "La", "Si")
    val blackKeys = listOf(0 to "Do#", 1 to "Ré#", 3 to "Fa#", 4 to "Sol#", 5 to "La#")
    var pressedKey     by remember { mutableStateOf<String?>(null) }
    var sessionSeconds by remember { mutableStateOf(0) }

    // ── Métronome état ────────────────────────────────────────────────
    var metroRunning   by remember { mutableStateOf(false) }
    var metroBpm       by remember { mutableStateOf(80) }
    var metroBeats     by remember { mutableStateOf(4) }
    var currentBeat    by remember { mutableStateOf(-1) }   // -1 = inactif
    var isAccentBeat   by remember { mutableStateOf(false) }

    // Synchronise le moteur avec l'état
    LaunchedEffect(metroBpm) { metronome.bpm = metroBpm }
    LaunchedEffect(metroBeats) { metronome.beatsPerMeasure = metroBeats }

    metronome.onBeat = { beat, accent ->
        currentBeat  = beat
        isAccentBeat = accent
    }

    // Chrono session
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            sessionSeconds++
            if (sessionSeconds % 60 == 0) onMinutesPlayed(1)
        }
    }

    // ── UI ────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EbonyDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // ── Header ────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Piano", style = MaterialTheme.typography.displaySmall, color = TextPrimary)
                Text(
                    if (soundReady) "Appuie sur une touche" else "Chargement des sons...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (soundReady) TextSecondary else IvoryGold
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(EbonyCard)
                    .border(1.dp, EbonyBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    "%02d:%02d".format(sessionSeconds / 60, sessionSeconds % 60),
                    style = MaterialTheme.typography.labelMedium,
                    color = NotesTeal
                )
            }
        }

        // ── Carte Mode Chanson ────────────────────────────────────────
        SongModeCard(onClick = onPlaySong)

        // ── Note affichée ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(EbonyCard)
                .border(
                    1.dp,
                    if (pressedKey != null) KeysViolet.copy(0.5f) else EbonyBorder,
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = pressedKey ?: "🎵",
                style = if (pressedKey != null) MaterialTheme.typography.displayMedium
                        else MaterialTheme.typography.displaySmall,
                color = if (pressedKey != null) KeysVioletLight else TextMuted
            )
        }

        // ── Clavier ───────────────────────────────────────────────────
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            val totalWidth  = this.maxWidth
            val whiteWidth  = totalWidth / whiteKeys.size
            val gap         = 2.dp
            val blackWidth  = whiteWidth * 0.62f
            val blackHeight = 112.dp

            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                whiteKeys.forEach { note ->
                    val isPressed = pressedKey == note
                    val bgColor by animateColorAsState(
                        targetValue   = if (isPressed) KeysVioletLight else TextPrimary.copy(0.93f),
                        animationSpec = tween(80), label = "wk_$note"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f).fillMaxHeight()
                            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                            .background(bgColor)
                            .border(1.dp, if (isPressed) KeysViolet else EbonyBorder.copy(0.3f),
                                RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                            .pointerInput(note) {
                                detectTapGestures(onPress = {
                                    pressedKey = note
                                    if (soundReady) soundEngine.play(note)
                                    scope.launch { delay(400); if (pressedKey == note) pressedKey = null }
                                    tryAwaitRelease()
                                    if (pressedKey == note) pressedKey = null
                                })
                            },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(note, style = MaterialTheme.typography.labelSmall,
                            color = if (isPressed) TextPrimary else EbonyDeep,
                            modifier = Modifier.padding(bottom = 6.dp))
                    }
                }
            }
            blackKeys.forEach { (wIndex, noteName) ->
                val isPressed  = pressedKey == noteName
                val leftOffset = whiteWidth * wIndex + whiteWidth * 0.69f + gap * wIndex
                val bgColor by animateColorAsState(
                    targetValue   = if (isPressed) KeysViolet else EbonyDeep,
                    animationSpec = tween(80), label = "bk_$noteName"
                )
                Box(
                    modifier = Modifier
                        .offset(x = leftOffset).width(blackWidth).height(blackHeight)
                        .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                        .background(bgColor)
                        .border(1.dp, if (isPressed) KeysVioletLight else EbonyBorder,
                            RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                        .pointerInput(noteName) {
                            detectTapGestures(onPress = {
                                pressedKey = noteName
                                if (soundReady) soundEngine.play(noteName)
                                scope.launch { delay(400); if (pressedKey == noteName) pressedKey = null }
                                tryAwaitRelease()
                                if (pressedKey == noteName) pressedKey = null
                            })
                        }
                )
            }
        }

        // ── Métronome ─────────────────────────────────────────────────
        MetronomeWidget(
            bpm        = metroBpm,
            beats      = metroBeats,
            running    = metroRunning,
            currentBeat = currentBeat,
            isAccent   = isAccentBeat,
            onBpmChange   = { metroBpm = it },
            onBeatsChange = { metroBeats = it },
            onToggle = {
                metroRunning = !metroRunning
                if (metroRunning) {
                    currentBeat = -1
                    metronome.bpm            = metroBpm
                    metronome.beatsPerMeasure = metroBeats
                    metronome.start()
                } else {
                    metronome.stop()
                    currentBeat = -1
                }
            }
        )
    }
}

// ── Widget métronome ──────────────────────────────────────────────────────

@Composable
private fun MetronomeWidget(
    bpm: Int,
    beats: Int,
    running: Boolean,
    currentBeat: Int,
    isAccent: Boolean,
    onBpmChange: (Int) -> Unit,
    onBeatsChange: (Int) -> Unit,
    onToggle: () -> Unit
) {
    // Pendule animé — oscille en rythme si actif
    val infiniteTransition = rememberInfiniteTransition(label = "pendule")
    val penduleAngle by infiniteTransition.animateFloat(
        initialValue  = -22f,
        targetValue   = 22f,
        animationSpec = if (running) infiniteRepeatable(
            tween((60_000 / bpm / 2).coerceAtLeast(100), easing = EaseInOut),
            RepeatMode.Reverse
        ) else infiniteRepeatable(tween(1), RepeatMode.Restart),
        label = "angle"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(EbonyCard)
            .border(
                1.dp,
                if (running) NotesTeal.copy(0.4f) else EbonyBorder,
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Titre
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Métronome",
                style = MaterialTheme.typography.titleSmall,
                color = if (running) NotesTeal else TextPrimary
            )
            if (running) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NotesTeal.copy(0.15f))
                        .border(1.dp, NotesTeal.copy(0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("$bpm BPM", style = MaterialTheme.typography.labelSmall, color = NotesTeal)
                }
            }
        }

        // ── Indicateurs de temps + pendule ────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicateurs de battements
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(beats) { i ->
                    val isCurrentBeat = running && currentBeat == i
                    val isAccentBeat  = i == 0
                    val beatColor by animateColorAsState(
                        targetValue = when {
                            isCurrentBeat && isAccentBeat -> IvoryGold
                            isCurrentBeat                 -> NotesTeal
                            isAccentBeat                  -> IvoryGold.copy(0.3f)
                            else                          -> EbonySurface
                        },
                        animationSpec = tween(60),
                        label = "beat_$i"
                    )
                    val beatScale by animateFloatAsState(
                        targetValue   = if (isCurrentBeat) 1.25f else 1f,
                        animationSpec = tween(80),
                        label         = "beat_scale_$i"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .scale(beatScale)
                            .height(if (isAccentBeat) 18.dp else 14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(beatColor)
                    )
                }
            }

            // Pendule
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(EbonySurface)
                    .border(1.dp, EbonyBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Tige du pendule
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .graphicsLayer(rotationZ = if (running) penduleAngle else 0f)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(NotesTeal, NotesTeal.copy(0.3f))
                            )
                        )
                )
                // Pivot central
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (running) NotesTeal else TextMuted)
                )
            }
        }

        // ── Contrôle BPM ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bouton -10
            BpmButton("-10") { onBpmChange((bpm - 10).coerceAtLeast(MetronomeEngine.MIN_BPM)) }
            // Bouton -1
            BpmButton("-1") { onBpmChange((bpm - 1).coerceAtLeast(MetronomeEngine.MIN_BPM)) }

            // Affichage BPM central
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "$bpm",
                    style = MaterialTheme.typography.displaySmall,
                    color = if (running) NotesTeal else TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    tempoLabel(bpm),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }

            // Bouton +1
            BpmButton("+1") { onBpmChange((bpm + 1).coerceAtMost(MetronomeEngine.MAX_BPM)) }
            // Bouton +10
            BpmButton("+10") { onBpmChange((bpm + 10).coerceAtMost(MetronomeEngine.MAX_BPM)) }
        }

        // Slider BPM
        Slider(
            value         = bpm.toFloat(),
            onValueChange = { onBpmChange(it.toInt()) },
            valueRange    = MetronomeEngine.MIN_BPM.toFloat()..MetronomeEngine.MAX_BPM.toFloat(),
            steps         = 0,
            modifier      = Modifier.fillMaxWidth(),
            colors        = SliderDefaults.colors(
                thumbColor       = if (running) NotesTeal else KeysViolet,
                activeTrackColor = if (running) NotesTeal else KeysViolet,
                inactiveTrackColor = EbonySurface
            )
        )

        // ── Mesure (chiffrage) ────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mesure :", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            listOf(2, 3, 4, 6).forEach { b ->
                val selected = beats == b
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) KeysViolet else EbonySurface)
                        .border(1.dp, if (selected) KeysVioletLight.copy(0.4f) else EbonyBorder, RoundedCornerShape(8.dp))
                        .clickable { onBeatsChange(b) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        "$b/4",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) TextPrimary else TextSecondary
                    )
                }
            }
        }

        // ── Bouton play/stop ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (running)
                        Brush.linearGradient(listOf(DissonanceRed.copy(0.8f), DissonanceRed))
                    else
                        Brush.linearGradient(listOf(NotesTeal.copy(0.8f), NotesTeal))
                )
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (running) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint     = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    if (running) "Arrêter" else "Démarrer le métronome",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
            }
        }
    }
}

// ── Bouton BPM petit ─────────────────────────────────────────────────────

@Composable
private fun BpmButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(EbonySurface)
            .border(1.dp, EbonyBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

// ── Label tempo musical ───────────────────────────────────────────────────

private fun tempoLabel(bpm: Int) = when {
    bpm < 60  -> "Largo"
    bpm < 66  -> "Larghetto"
    bpm < 76  -> "Adagio"
    bpm < 108 -> "Andante"
    bpm < 120 -> "Moderato"
    bpm < 156 -> "Allegro"
    bpm < 176 -> "Vivace"
    bpm < 200 -> "Presto"
    else      -> "Prestissimo"
}

@Composable
private fun SongModeCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1A0A2E), Color(0xFF0A1A2E))
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
            Text("🎼", style = MaterialTheme.typography.displaySmall)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "Mode Chanson",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Text(
                    "Joue \"Lettre à Élise\" en suivant les notes",
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
