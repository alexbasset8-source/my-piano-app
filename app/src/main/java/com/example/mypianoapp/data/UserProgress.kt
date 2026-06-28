package com.example.mypianoapp.data

import kotlinx.serialization.Serializable

@Serializable
data class UserProgress(
    val userName: String = "Pianiste",
    val totalXp: Int = 0,
    val completedLessonIds: Set<Int> = emptySet(),
    val passedQuizIds: Set<Int> = emptySet(),   // IDs des leçons dont le QCM est réussi
    val currentStreak: Int = 0,
    val lastSessionDateEpochDay: Long = -1L,
    val todayMinutes: Int = 0,
    val todayExercises: Int = 0,
    val todayLessonsCompleted: Int = 0,
    val todayDateEpochDay: Long = -1L,
    val dailyGoalMinutes: Int = 10
) {
    val level: Int get() = when {
        totalXp < 100  -> 1
        totalXp < 250  -> 2
        totalXp < 500  -> 3
        totalXp < 900  -> 4
        totalXp < 1400 -> 5
        else           -> 6
    }

    val xpForCurrentLevel: Int get() = when (level) {
        1 -> 0;   2 -> 100;  3 -> 250
        4 -> 500; 5 -> 900;  else -> 1400
    }
    val xpForNextLevel: Int get() = when (level) {
        1 -> 100;  2 -> 250;  3 -> 500
        4 -> 900;  5 -> 1400; else -> 2000
    }
    val xpProgressInLevel: Int  get() = totalXp - xpForCurrentLevel
    val xpNeededForNextLevel: Int get() = xpForNextLevel - xpForCurrentLevel
    val xpFraction: Float get() =
        (xpProgressInLevel.toFloat() / xpNeededForNextLevel.toFloat()).coerceIn(0f, 1f)

    val globalProgressFraction: Float get() {
        val total = LessonCatalog.lessons.size
        return if (total == 0) 0f
        else (completedLessonIds.size.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }

    val dailyGoalFraction: Float get() =
        (todayMinutes.toFloat() / dailyGoalMinutes.toFloat()).coerceIn(0f, 1f)

    fun isLessonCompleted(id: Int) = id in completedLessonIds
    fun isQuizPassed(lessonId: Int) = lessonId in passedQuizIds

    /**
     * Règle de déblocage :
     * - Leçon 1 toujours accessible
     * - Leçons 2–3 : leçon précédente complétée
     * - Leçon 4    : leçon 3 complétée ET quiz 3 réussi
     * - Leçons 5–6 : leçon précédente complétée
     * - Leçon 7    : leçon 6 complétée ET quiz 6 réussi
     * - Leçons 8–9 : leçon précédente complétée
     * - Leçon 10   : leçon 9 complétée ET quiz 9 réussi
     */
    fun isLessonUnlocked(id: Int): Boolean {
        if (id == 1) return true
        val prevCompleted = (id - 1) in completedLessonIds
        return when (id) {
            4    -> prevCompleted && 3 in passedQuizIds
            7    -> prevCompleted && 6 in passedQuizIds
            10   -> prevCompleted && 9 in passedQuizIds
            else -> prevCompleted
        }
    }
}
