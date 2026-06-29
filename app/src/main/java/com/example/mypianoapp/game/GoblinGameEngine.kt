package com.example.mypianoapp.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

import kotlin.time.Duration.Companion.milliseconds

// ── Notes disponibles (1 octave, touches blanches uniquement au début) ────

val WHITE_NOTES = listOf("Do", "Ré", "Mi", "Fa", "Sol", "La", "Si")
val ALL_NOTES   = listOf("Do", "Do#", "Ré", "Ré#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")

// ── Modèles ───────────────────────────────────────────────────────────────

data class Goblin(
    val id: Int,
    val note: String,
    val lane: Int,          // colonne de départ (0..6 pour blanches)
    var x: Float,           // position horizontale normalisée 0..1 (0=droite, 1=clavier)
    var hp: Int = 1,
    var alive: Boolean = true,
    val speed: Float,        // vitesse de déplacement par tick
)

enum class GamePhase { MENU, COUNTDOWN, PLAYING, WAVE_CLEAR, GAME_OVER, VICTORY }

data class GoblinGameState(
    val phase: GamePhase = GamePhase.MENU,
    val wave: Int = 1,
    val score: Int = 0,
    val lives: Int = 3,
    val maxLives: Int = 3,
    val goblins: List<Goblin> = emptyList(),
    val countdown: Int = 3,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val flashNote: String? = null,       // note jouée (feedback visuel)
    val flashCorrect: Boolean = true     // vrai = hit, faux = miss
)

// ── Moteur ────────────────────────────────────────────────────────────────

class GoblinGameEngine(private val scope: CoroutineScope) {

    var state by mutableStateOf(GoblinGameState())
        private set

    private var gameLoopJob: Job? = null
    private var goblinIdCounter = 0
    private val maxWave = 5

    // ── Actions publiques ──────────────────────────────────────────────

    fun startGame() {
        state = GoblinGameState(phase = GamePhase.COUNTDOWN, countdown = 3)
        scope.launch {
            repeat(3) { i ->
                state = state.copy(countdown = 3 - i)
                delay(900.milliseconds)
            }
            startWave(1)
        }
    }

    fun playNote(note: String) {
        if (state.phase != GamePhase.PLAYING) return

        // Cherche le gobelin le plus proche portant cette note
        val target = state.goblins
            .asSequence()
            .filter { (it.alive && it.note == note) }
            .minByOrNull { it.x }  // le plus avancé en priorité

        if (target != null) {
            // Hit !
            val newCombo  = state.combo + 1
            val scoreGain = 10 * minOf(newCombo, 5)  // max ×5 combo
            val updated   = state.goblins.map {
                if (it.id == target.id) it.copy(alive = false) else it
            }
            state = state.copy(
                goblins      = updated,
                score        = state.score + scoreGain,
                combo        = newCombo,
                maxCombo     = maxOf(state.maxCombo, newCombo),
                flashNote    = note,
                flashCorrect = true
            )
        } else {
            // Miss — casse le combo mais pas de perte de vie
            state = state.copy(
                combo        = 0,
                flashNote    = note,
                flashCorrect = false
            )
        }

        // Reset flash après 200ms
        scope.launch {
            delay(200)
            state = state.copy(flashNote = null)
        }
    }

    fun restartGame() {
        gameLoopJob?.cancel()
        state = GoblinGameState()
    }

    // ── Vague ─────────────────────────────────────────────────────────

    private fun startWave(wave: Int) {
        state = state.copy(
            phase   = GamePhase.PLAYING,
            wave    = wave,
            goblins = spawnWave(wave),
            combo   = 0
        )
        startGameLoop()
    }

    private fun spawnWave(wave: Int): List<Goblin> {
        // Plus de vague = plus de gobelins, plus rapides, notes noires aussi
        val count    = 3 + wave * 2          // 5, 7, 9, 11, 13
        val speed    = 0.0012f + wave * 0.0003f
        val useBlack = wave >= 3
        val pool     = if (useBlack) ALL_NOTES else WHITE_NOTES

        return (0 until count).map { i ->
            val note = pool[Random.nextInt(pool.size)]
            // Répartition en quinconce sur l'axe X de départ
            val startX = 0.05f + (i % 4) * 0.05f  // décalés pour ne pas arriver tous ensemble
            Goblin(
                id    = goblinIdCounter++,
                note  = note,
                lane  = Random.nextInt(7),
                x     = startX,
                speed = speed + Random.nextFloat() * 0.0002f
            )
        }
    }

    // ── Boucle de jeu ─────────────────────────────────────────────────

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = scope.launch {
            while (true) {
                delay(16)  // ~60 FPS
                if (state.phase != GamePhase.PLAYING) break
                tick()
            }
        }
    }

    private fun tick() {
        val current = state
        if (current.phase != GamePhase.PLAYING) return

        // Déplacer les gobelins vers la gauche (vers le clavier)
        var livesLost = 0
        val moved = current.goblins.map { g ->
            if (!g.alive) return@map g
            val newX = g.x + g.speed
            if (newX >= 1f) {
                // Gobelin atteint le clavier → perd une vie
                livesLost++
                g.copy(alive = false, x = 1f)
            } else {
                g.copy(x = newX)
            }
        }

        val newLives = current.lives - livesLost
        val combo    = if (livesLost > 0) 0 else current.combo

        if (newLives <= 0) {
            // Game Over
            gameLoopJob?.cancel()
            state = current.copy(
                goblins = moved,
                lives   = 0,
                combo   = 0,
                phase   = GamePhase.GAME_OVER
            )
            return
        }

        // Vérifier si la vague est terminée
        val allDead = moved.all { !it.alive }

        state = current.copy(
            goblins = moved,
            lives   = newLives,
            combo   = combo
        )

        if (allDead) {
            gameLoopJob?.cancel()
            scope.launch {
                state = state.copy(phase = GamePhase.WAVE_CLEAR)
                delay(1800)
                if (current.wave >= maxWave) {
                    state = state.copy(phase = GamePhase.VICTORY)
                } else {
                    startWave(current.wave + 1)
                }
            }
        }
    }
}
