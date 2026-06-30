package com.example.mypianoapp.song

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.mypianoapp.audio.PianoSoundEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ━━ Difficulté ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

enum class Difficulty(
    val label: String,
    val bpmMultiplier: Float,   // multiplicateur sur le BPM de base
    val timingWindowMs: Long,   // tolérance de frappe en ms
    val scrollSpeed: Float      // pixels par beat (relatif)
) {
    EASY(   "Facile", 0.65f, 500, 180f),
    NORMAL( "Normal", 1.00f, 280, 260f)
}

// ━━ État ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

enum class PlayerPhase { IDLE, COUNTDOWN, PLAYING, PAUSED, FINISHED }

data class ActiveNote(
    val songNote: SongNote,
    val id: Int,
    var hit: Boolean = false,
    var missed: Boolean = false
)

data class FeedbackEvent(
    val type: FeedbackType,
    val note: String,
    val x: Float = 0f   // position X pour l'animation
)
enum class FeedbackType { PERFECT, GOOD, MISS }

data class SongPlayerState(
    val phase: PlayerPhase = PlayerPhase.IDLE,
    val currentBeat: Float = 0f,       // position courante en beats
    val score: Int = 0,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val perfectCount: Int = 0,
    val goodCount: Int = 0,
    val missCount: Int = 0,
    val countdown: Int = 3,
    val activeNotes: List<ActiveNote> = emptyList(),
    val lastFeedback: FeedbackEvent? = null,
    val difficulty: Difficulty = Difficulty.EASY,
    val leftHandEnabled: Boolean = true  // Main gauche activée par défaut
)

// ━━ Moteur ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

class SongPlayerEngine(
    private val song: Song,
    private val soundEngine: PianoSoundEngine,
    private val scope: CoroutineScope
) {
    var state by mutableStateOf(SongPlayerState())
        private set

    private var gameLoopJob: Job? = null
    private var noteIdCounter = 0

    // Notes déjà programmées pour la MG (évite les doublons)
    private val scheduledLeftNotes = mutableSetOf<Int>()

    // ━━ Actions publiques ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun selectDifficulty(d: Difficulty) {
        state = state.copy(difficulty = d)
    }

    /** Active ou désactive la main gauche */
    fun setLeftHandEnabled(enabled: Boolean) {
        state = state.copy(leftHandEnabled = enabled)
    }

    fun start() {
        state = state.copy(phase = PlayerPhase.COUNTDOWN, countdown = 3)
        scope.launch {
            for (i in 3 downTo 1) {
                state = state.copy(countdown = i)
                delay(900)
            }
            state = state.copy(phase = PlayerPhase.PLAYING, currentBeat = 0f)
            startLoop()
        }
    }

    fun pause() {
        if (state.phase == PlayerPhase.PLAYING) {
            gameLoopJob?.cancel()
            state = state.copy(phase = PlayerPhase.PAUSED)
        } else if (state.phase == PlayerPhase.PAUSED) {
            state = state.copy(phase = PlayerPhase.PLAYING)
            startLoop()
        }
    }

    fun restart() {
        gameLoopJob?.cancel()
        scheduledLeftNotes.clear()
        missedRightNotes.clear()
        noteIdCounter = 0
        state = SongPlayerState(difficulty = state.difficulty, leftHandEnabled = state.leftHandEnabled)
    }

    /** Appelé quand le joueur appuie sur une touche main droite */
    fun onNoteHit(noteName: String) {
        if (state.phase != PlayerPhase.PLAYING) return

        val currentBeat  = state.currentBeat
        val bpm          = song.bpmBase * state.difficulty.bpmMultiplier
        val beatDurationMs = (60_000f / bpm).toLong()
        val windowBeats  = state.difficulty.timingWindowMs.toFloat() / beatDurationMs

        // Cherche la note MD non encore touchée la plus proche en temps
        val target = song.notes
            .filter { it.hand == Hand.RIGHT && it.note == noteName }
            .filter { n ->
                val dist = kotlin.math.abs(n.beatStart - currentBeat)
                dist <= windowBeats
            }
            .minByOrNull { kotlin.math.abs(it.beatStart - currentBeat) }

        if (target == null) {
            // Mauvaise note ou hors timing
            state = state.copy(
                combo        = 0,
                missCount    = state.missCount + 1,
                lastFeedback = FeedbackEvent(FeedbackType.MISS, noteName)
            )
            return
        }

        val dist = kotlin.math.abs(target.beatStart - currentBeat)
        val halfWindow = windowBeats / 2f
        val isPerfect  = dist <= halfWindow

        val gain      = if (isPerfect) 15 + state.combo * 2 else 8 + state.combo
        val newCombo  = state.combo + 1
        val feedback  = if (isPerfect) FeedbackType.PERFECT else FeedbackType.GOOD

        // Marquer la note comme traitée pour qu'elle ne soit pas comptée en miss
        missedRightNotes.add(target.hashCode())

        state = state.copy(
            score        = state.score + gain,
            combo        = newCombo,
            maxCombo     = maxOf(state.maxCombo, newCombo),
            perfectCount = state.perfectCount + (if (isPerfect) 1 else 0),
            goodCount    = state.goodCount + (if (!isPerfect) 1 else 0),
            lastFeedback = FeedbackEvent(feedback, noteName)
        )

        // Jouer le son
        soundEngine.play(noteName)

        // Reset feedback visuel
        scope.launch {
            delay(350)
            state = state.copy(lastFeedback = null)
        }
    }

    // ━━ Boucle principale ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private fun startLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = scope.launch {
            val bpm           = song.bpmBase * state.difficulty.bpmMultiplier
            val tickMs        = 16L   // ~60 FPS
            val beatsPerTick  = (bpm / 60f) * (tickMs / 1000f)

            while (state.phase == PlayerPhase.PLAYING) {
                delay(tickMs)

                val newBeat = state.currentBeat + beatsPerTick

                // Déclencher les notes MG à venir (dans les 0.5 prochains beats)
                scheduleLeftHandNotes(newBeat, bpm)

                // Vérifier les notes MD manquées (dépassées sans être jouées)
                checkMissedNotes(newBeat, bpm)

                // Fin du morceau
                if (newBeat >= song.totalBeats + 4f) {
                    state = state.copy(
                        phase        = PlayerPhase.FINISHED,
                        currentBeat  = newBeat
                    )
                    break
                }

                state = state.copy(currentBeat = newBeat)
            }
        }
    }

    private fun scheduleLeftHandNotes(currentBeat: Float, bpm: Float) {
        // Ne pas jouer les notes de la main gauche si l'option est désactivée
        if (!state.leftHandEnabled) return

        val lookAheadBeats = 0.3f
        song.notes
            .filter { it.hand == Hand.LEFT }
            .filter { it.beatStart in (currentBeat - 0.1f)..(currentBeat + lookAheadBeats) }
            .forEach { note ->
                val id = note.hashCode()
                if (id !in scheduledLeftNotes && note.note != null) {
                    scheduledLeftNotes.add(id)
                    soundEngine.play(note.note)
                }
            }
    }

    // Notes MD déjà comptées comme manquées (évite double comptage)
    private val missedRightNotes = mutableSetOf<Int>()

    private fun checkMissedNotes(currentBeat: Float, bpm: Float) {
        val beatDurationMs = (60_000f / bpm)
        val windowBeats    = state.difficulty.timingWindowMs / beatDurationMs

        val newlyMissed = song.notes
            .filter { it.hand == Hand.RIGHT && it.note != null }
            .filter { note ->
                val id = note.hashCode()
                val passedWindow = currentBeat > note.beatStart + windowBeats
                passedWindow && id !in missedRightNotes
            }

        if (newlyMissed.isNotEmpty()) {
            newlyMissed.forEach { missedRightNotes.add(it.hashCode()) }
            state = state.copy(
                combo     = 0,
                missCount = state.missCount + newlyMissed.size
            )
        }
    }

    // ━━ Accesseur ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /** Notes à afficher dans la fenêtre visible (±visibleBeats autour de currentBeat) */
    fun visibleNotes(visibleBeats: Float = 8f): List<SongNote> {
        val beat = state.currentBeat
        return song.notes.filter { note ->
            note.beatStart >= beat - 1f && note.beatStart <= beat + visibleBeats
        }
    }

    /** Score final en % */
    fun accuracy(): Int {
        val total = state.perfectCount + state.goodCount + state.missCount
        if (total == 0) return 100
        return ((state.perfectCount * 100 + state.goodCount * 60) / total)
            .coerceIn(0, 100)
    }
}
