package com.example.mypianoapp.data

import androidx.compose.ui.graphics.Color

// ── Types d'exercices ──────────────────────────────────────────────────────

enum class ExerciseType {
    NOTE_RECOGNITION,   // Trouve la bonne touche sur le clavier
    SCALE_GUIDED,       // Joue la gamme note par note dans l'ordre
    RHYTHM_TAP          // Tape le rythme au bon moment
}

// ── Modèle ─────────────────────────────────────────────────────────────────

data class ExerciseData(
    val id: Int,
    val emoji: String,
    val title: String,
    val description: String,
    val type: ExerciseType,
    val xpReward: Int,
    val minLessonsRequired: Int,
    val accentColorHex: Long,      // stocké comme Long pour éviter Color dans data layer
    // Paramètres spécifiques au type
    val notes: List<String> = emptyList(),       // notes impliquées
    val targetNotes: List<String> = emptyList(), // pour SCALE_GUIDED : ordre à jouer
    val rhythmPattern: List<Int> = emptyList(),  // pour RHYTHM_TAP : durées en ms
    val rounds: Int = 5                          // nombre de questions/rounds
)

// ── Catalogue ──────────────────────────────────────────────────────────────

object ExerciseCatalog {

    val exercises = listOf(

        ExerciseData(
            id                  = 1,
            emoji               = "🎯",
            title               = "Reconnais les notes",
            description         = "Une note s'affiche — trouve la bonne touche sur le clavier.",
            type                = ExerciseType.NOTE_RECOGNITION,
            xpReward            = 15,
            minLessonsRequired  = 1,
            accentColorHex      = 0xFF7C3AED,
            notes               = listOf("Do", "Ré", "Mi", "Fa", "Sol"),
            rounds              = 6
        ),

        ExerciseData(
            id                  = 2,
            emoji               = "🎼",
            title               = "Gamme de Do",
            description         = "Joue les 7 notes de la gamme de Do dans l'ordre.",
            type                = ExerciseType.SCALE_GUIDED,
            xpReward            = 20,
            minLessonsRequired  = 3,
            accentColorHex      = 0xFF06B6D4,
            targetNotes         = listOf("Do", "Ré", "Mi", "Fa", "Sol", "La", "Si"),
            rounds              = 1
        ),

        ExerciseData(
            id                  = 3,
            emoji               = "🥁",
            title               = "Rythme en noires",
            description         = "Tape le bouton en suivant la pulsation régulière.",
            type                = ExerciseType.RHYTHM_TAP,
            xpReward            = 10,
            minLessonsRequired  = 4,
            accentColorHex      = 0xFFF5C842,
            rhythmPattern       = listOf(600, 600, 600, 600, 600, 600, 600, 600),
            rounds              = 1
        ),

        ExerciseData(
            id                  = 4,
            emoji               = "🎹",
            title               = "Notes dièses",
            description         = "Trouve les touches noires (dièses) sur le clavier.",
            type                = ExerciseType.NOTE_RECOGNITION,
            xpReward            = 25,
            minLessonsRequired  = 9,
            accentColorHex      = 0xFFEF4444,
            notes               = listOf("Do#", "Ré#", "Fa#", "Sol#", "La#"),
            rounds              = 6
        ),

        ExerciseData(
            id                  = 5,
            emoji               = "🎵",
            title               = "Gamme complète",
            description         = "Joue les 7 notes + Do aigu en passant le pouce.",
            type                = ExerciseType.SCALE_GUIDED,
            xpReward            = 30,
            minLessonsRequired  = 5,
            accentColorHex      = 0xFF22C55E,
            targetNotes         = listOf("Do", "Ré", "Mi", "Fa", "Sol", "La", "Si", "Do"),
            rounds              = 1
        ),
    )
}
