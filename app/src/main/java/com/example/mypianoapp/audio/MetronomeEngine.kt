package com.example.mypianoapp.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Moteur métronome — génère les clics via AudioTrack (synthèse PCM).
 *
 * Deux sons différents :
 *   - Temps fort (temps 1) : fréquence plus haute (1200 Hz) → accent
 *   - Temps faibles        : fréquence normale   (800 Hz)
 *
 * L'enveloppe exponentielle donne un "clic" court et naturel.
 */
class MetronomeEngine {

    private val sampleRate = 44100
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    var bpm: Int = 80
        set(value) { field = value.coerceIn(MIN_BPM, MAX_BPM) }

    var beatsPerMeasure: Int = 4    // numérateur de la mesure (2, 3, 4, 6)
    var isRunning: Boolean = false
        private set

    // Callback appelé sur chaque battement : (beatIndex, isAccent)
    var onBeat: ((Int, Boolean) -> Unit)? = null

    fun start() {
        if (isRunning) return
        isRunning = true
        var beatIndex = 0

        job = scope.launch {
            while (isActive) {
                val isAccent = (beatIndex % beatsPerMeasure) == 0
                playClick(isAccent)
                onBeat?.invoke(beatIndex % beatsPerMeasure, isAccent)
                beatIndex++

                val intervalMs = (60_000.0 / bpm).toLong()
                delay(intervalMs)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        isRunning = false
    }

    fun toggle() {
        if (isRunning) stop() else start()
    }

    private fun playClick(accent: Boolean) {
        val freq      = if (accent) 1200.0 else 800.0
        val durationMs = 40
        val numSamples = (sampleRate * durationMs) / 1000
        val samples   = ShortArray(numSamples)
        val decay     = 80.0   // vitesse de décroissance exponentielle

        for (i in 0 until numSamples) {
            val t         = i.toDouble() / sampleRate
            val envelope  = exp(-decay * t)
            val amplitude = if (accent) 0.9 else 0.65
            val sample    = (amplitude * envelope * sin(2 * PI * freq * t) * Short.MAX_VALUE).toInt()
            samples[i]    = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(samples, 0, samples.size)
            track.play()
            // Libère après lecture (~50ms)
            scope.launch {
                delay(60)
                track.stop()
                track.release()
            }
        } catch (_: Exception) {
            // Ignore si l'audio n'est pas disponible
        }
    }

    fun release() {
        stop()
    }

    companion object {
        const val MIN_BPM = 30
        const val MAX_BPM = 240
    }
}
