package com.example.mypianoapp.song

/**
 * Une note dans la partition.
 * @param note      nom de la note (ex: "Mi", "Ré#") ou null = silence
 * @param hand      MAIN_DROITE ou MAIN_GAUCHE
 * @param beatStart position en beats depuis le début (float pour les syncopes)
 * @param duration  durée en beats (0.5 = croche, 1 = noire, 2 = blanche…)
 * @param octave    0 = octave du clavier affiché, 1 = octave au-dessus, -1 = en dessous
 */
data class SongNote(
    val note: String?,
    val hand: Hand,
    val beatStart: Float,
    val duration: Float,
    val octave: Int = 0
)

enum class Hand { RIGHT, LEFT }

data class Song(
    val id: String,
    val title: String,
    val composer: String,
    val bpmBase: Int,          // BPM de référence (vitesse "Normal")
    val timeSignature: Int,    // numérateur (ex: 3 pour 3/4)
    val notes: List<SongNote>,
    val totalBeats: Float
)

// ── Partition Lettre à Élise — Beethoven ──────────────────────────────────
// Tonalité : La mineur (La-Do-Mi)
// Mesure : 3/4 — BPM référence : 76
// On transcrit les 2 premiers thèmes (A et B) soit ~32 mesures

val lettrAElise = Song(
    id            = "lettre_a_elise",
    title         = "Lettre à Élise",
    composer      = "Ludwig van Beethoven",
    bpmBase       = 76,
    timeSignature = 3,
    totalBeats    = 96f,
    notes         = buildEliseNotes()
)

private fun buildEliseNotes(): List<SongNote> {
    val notes = mutableListOf<SongNote>()

    // ── Helpers ──────────────────────────────────────────────────────
    fun r(note: String?, beat: Float, dur: Float = 1f, oct: Int = 0) =
        notes.add(SongNote(note, Hand.RIGHT, beat, dur, oct))
    fun l(note: String?, beat: Float, dur: Float = 1f, oct: Int = 0) =
        notes.add(SongNote(note, Hand.LEFT, beat, dur, oct))

    // ── THÈME A — mesures 1-8 (beat 0-23) ────────────────────────────
    // Mélodie main droite (motif Mi-Ré#-Mi-Ré#-Mi-Si-Ré-Do-La)
    // Mesure 1
    r("Mi",  0f,  0.5f, 1)
    r("Ré#", 0.5f,0.5f, 1)
    r("Mi",  1f,  0.5f, 1)
    r("Ré#", 1.5f,0.5f, 1)
    r("Mi",  2f,  0.5f, 1)
    // Mesure 2
    r("Si",  3f,  0.5f, 0)
    r("Ré",  3.5f,0.5f, 1)
    r("Do",  4f,  0.5f, 1)
    // Mesure 3
    r("La",  5f,  1f,   0)
    // Mesure 4 (silence + arpège La mineur)
    r("Do",  8f,  0.5f, 0)
    r("Mi",  8.5f,0.5f, 0)
    r("La",  9f,  0.5f, 0)
    // Mesure 5
    r("Si",  10f, 1f,   0)
    r("Mi",  11f, 0.5f, 0)
    r("Sol#",11.5f,0.5f,0)
    // Mesure 6
    r("Si",  12f, 1f,   0)
    r("Mi",  13f, 1f,   1)
    // Mesure 7-8 : retour motif
    r("Mi",  15f, 0.5f, 1)
    r("Ré#", 15.5f,0.5f,1)
    r("Mi",  16f, 0.5f, 1)
    r("Ré#", 16.5f,0.5f,1)
    r("Mi",  17f, 0.5f, 1)
    r("Si",  18f, 0.5f, 0)
    r("Ré",  18.5f,0.5f,1)
    r("Do",  19f, 0.5f, 1)
    r("La",  20f, 1f,   0)

    // ── Accompagnement main gauche thème A ────────────────────────────
    // Basse + accord toutes les mesures (3 temps)
    val leftPatternA = listOf(
        0f, 3f, 6f, 9f, 12f, 15f, 18f, 21f
    )
    val bassNotesA = listOf("La","Mi","La","Mi","La","Mi","La","Mi")
    val chord1A    = listOf("Mi","La","Mi","La","Mi","La","Mi","La")
    val chord2A    = listOf("Do","Do","Do","Do","Do","Do","Do","Do")

    leftPatternA.forEachIndexed { i, beat ->
        l(bassNotesA[i % bassNotesA.size],  beat,        0.5f, -1)
        l(chord1A[i % chord1A.size],        beat + 1f,   0.5f, 0)
        l(chord2A[i % chord2A.size],        beat + 2f,   0.5f, 0)
    }

    // ── THÈME B — mesures 9-16 (beat 24-47) ──────────────────────────
    val b = 24f
    // Mélodie
    r("Fa",  b+0f,  0.5f, 1)
    r("Mi",  b+0.5f,0.5f, 1)
    r("Fa",  b+1f,  0.5f, 1)
    r("Mi",  b+1.5f,0.5f, 1)
    r("Fa",  b+2f,  0.5f, 1)

    r("Si",  b+3f,  0.5f, 0)
    r("Ré",  b+3.5f,0.5f, 1)
    r("Do",  b+4f,  0.5f, 1)

    r("La",  b+5f,  1f,   0)

    r("Do",  b+6f,  0.5f, 0)
    r("Mi",  b+6.5f,0.5f, 0)
    r("La",  b+7f,  0.5f, 0)

    r("Si",  b+8f,  1f,   0)
    r("Fa",  b+9f,  0.5f, 1)
    r("Sol#",b+9.5f,0.5f, 0)

    r("Si",  b+10f, 1f,   0)
    r("Mi",  b+11f, 1f,   1)

    r("Mi",  b+12f, 0.5f, 1)
    r("Ré#", b+12.5f,0.5f,1)
    r("Mi",  b+13f, 0.5f, 1)
    r("Ré#", b+13.5f,0.5f,1)
    r("Mi",  b+14f, 0.5f, 1)

    r("Si",  b+15f, 0.5f, 0)
    r("Ré",  b+15.5f,0.5f,1)
    r("Do",  b+16f, 0.5f, 1)
    r("La",  b+17f, 1f,   0)

    // Accompagnement main gauche thème B
    val leftPatternB = listOf(b, b+3f, b+6f, b+9f, b+12f, b+15f, b+18f, b+21f)
    val bassNotesB  = listOf("La","Mi","La","Do","La","Mi","La","Mi")
    val chord1B     = listOf("Mi","Si","Mi","Sol","Mi","Si","Mi","Si")
    val chord2B     = listOf("Do","Ré","Do","Mi","Do","Ré","Do","La")

    leftPatternB.forEachIndexed { i, beat ->
        l(bassNotesB[i % bassNotesB.size],  beat,        0.5f, -1)
        l(chord1B[i % chord1B.size],        beat + 1f,   0.5f, 0)
        l(chord2B[i % chord2B.size],        beat + 2f,   0.5f, 0)
    }

    // ── THÈME A bis — mesures 17-24 (beat 48-71) ─────────────────────
    val c = 48f
    r("Mi",  c+0f,  0.5f, 1)
    r("Ré#", c+0.5f,0.5f, 1)
    r("Mi",  c+1f,  0.5f, 1)
    r("Ré#", c+1.5f,0.5f, 1)
    r("Mi",  c+2f,  0.5f, 1)
    r("Si",  c+3f,  0.5f, 0)
    r("Ré",  c+3.5f,0.5f, 1)
    r("Do",  c+4f,  0.5f, 1)
    r("La",  c+5f,  1f,   0)
    r("Do",  c+6f,  0.5f, 0)
    r("Mi",  c+6.5f,0.5f, 0)
    r("La",  c+7f,  0.5f, 0)
    r("Si",  c+8f,  1f,   0)
    r("Mi",  c+9f,  0.5f, 0)
    r("Sol#",c+9.5f,0.5f, 0)
    r("Si",  c+10f, 1f,   0)
    r("Mi",  c+11f, 1f,   1)
    r("Mi",  c+12f, 0.5f, 1)
    r("Ré#", c+12.5f,0.5f,1)
    r("Mi",  c+13f, 0.5f, 1)
    r("Ré#", c+13.5f,0.5f,1)
    r("Mi",  c+14f, 0.5f, 1)
    r("Si",  c+15f, 0.5f, 0)
    r("Ré",  c+15.5f,0.5f,1)
    r("Do",  c+16f, 0.5f, 1)
    r("La",  c+17f, 1.5f, 0)

    leftPatternA.map { it + c }.forEachIndexed { i, beat ->
        l(bassNotesA[i % bassNotesA.size],  beat,        0.5f, -1)
        l(chord1A[i % chord1A.size],        beat + 1f,   0.5f, 0)
        l(chord2A[i % chord2A.size],        beat + 2f,   0.5f, 0)
    }

    // ── CODA — mesures 25-32 (beat 72-95) ────────────────────────────
    val d = 72f
    r("La",  d+0f,  0.5f, 0)
    r("Si",  d+0.5f,0.5f, 0)
    r("Do",  d+1f,  0.5f, 1)
    r("Si",  d+1.5f,0.5f, 0)
    r("Do",  d+2f,  0.5f, 1)

    r("Mi",  d+3f,  0.5f, 1)
    r("Fa",  d+3.5f,0.5f, 1)
    r("Sol", d+4f,  0.5f, 1)

    r("La",  d+5f,  1f,   1)

    r("Mi",  d+6f,  0.5f, 1)
    r("Fa",  d+6.5f,0.5f, 1)
    r("Sol#",d+7f,  0.5f, 1)

    r("La",  d+8f,  1f,   1)
    r("Mi",  d+9f,  0.5f, 1)
    r("Sol#",d+9.5f,0.5f, 1)

    r("Si",  d+10f, 1f,   0)
    r("Mi",  d+11f, 1f,   1)

    r("Mi",  d+12f, 0.5f, 1)
    r("Ré#", d+12.5f,0.5f,1)
    r("Mi",  d+13f, 0.5f, 1)
    r("Ré#", d+13.5f,0.5f,1)
    r("Mi",  d+14f, 0.5f, 1)

    r("Si",  d+15f, 0.5f, 0)
    r("Ré",  d+15.5f,0.5f,1)
    r("Do",  d+16f, 0.5f, 1)
    r("La",  d+17f, 2f,   0)

    // MG coda
    val leftPatternC = listOf(d, d+3f, d+6f, d+9f, d+12f, d+15f, d+18f, d+21f)
    leftPatternC.forEachIndexed { i, beat ->
        l(bassNotesA[i % bassNotesA.size],  beat,        0.5f, -1)
        l(chord1A[i % chord1A.size],        beat + 1f,   0.5f, 0)
        l(chord2A[i % chord2A.size],        beat + 2f,   0.5f, 0)
    }

    return notes.sortedBy { it.beatStart }
}
