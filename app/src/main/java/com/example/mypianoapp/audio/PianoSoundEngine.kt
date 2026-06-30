package com.example.mypianoapp.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.AudioAttributes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Moteur audio pour le piano.
 * Charge les samples .mp3 depuis assets/sounds/ et les joue via MediaPlayer.
 * Chaque note a son propre MediaPlayer pour permettre la polyphonie (plusieurs
 * notes en même temps sans couper la précédente).
 */
class PianoSoundEngine(private val context: Context) {

    // Map note → fichier asset
    private val noteToAsset = mapOf(
        "Do"   to "sounds/C4.mp3",
        "Do#"  to "sounds/Cs4.mp3",
        "Ré"   to "sounds/D4.mp3",
        "Ré#"  to "sounds/Ds4.mp3",
        "Mi"   to "sounds/E4.mp3",
        "Fa"   to "sounds/F4.mp3",  // F4 téléchargé
        "Fa#"  to "sounds/Fs4.mp3",
        "Sol"  to "sounds/G4.mp3",
        "Sol#" to "sounds/Gs4.mp3",
        "La"   to "sounds/A4.mp3",
        "La#"  to "sounds/As4.mp3",
        "Si"   to "sounds/B4.mp3",
    )

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    // Pool de MediaPlayer préchargés
    private val players = mutableMapOf<String, MediaPlayer>()

    suspend fun preload() = withContext(Dispatchers.IO) {
        noteToAsset.forEach { (note, asset) ->
            try {
                val afd = context.assets.openFd(asset)
                val player = MediaPlayer().apply {
                    setAudioAttributes(audioAttributes)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    prepare()
                    afd.close()
                }
                players[note] = player
            } catch (_: Exception) {
                // Sample manquant — on ignore silencieusement
            }
        }
    }

    fun play(note: String) {
        players[note]?.let { player ->
            try {
                if (player.isPlaying) {
                    player.seekTo(0)
                } else {
                    player.start()
                }
            } catch (_: Exception) {
                // Réinitialiser si état invalide
            }
        }
    }

    fun release() {
        players.values.forEach { it.release() }
        players.clear()
    }
}
