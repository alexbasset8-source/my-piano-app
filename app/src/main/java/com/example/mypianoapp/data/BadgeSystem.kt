package com.example.mypianoapp.data

/**
 * Système de badges de l'application.
 * Chaque badge a une condition calculée depuis UserProgress.
 */
data class Badge(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String,
    val rarity: BadgeRarity
)

enum class BadgeRarity { COMMON, RARE, EPIC, LEGENDARY }

object BadgeSystem {

    val allBadges = listOf(
        // ── Premiers pas ──────────────────────────────────────────────
        Badge("first_lesson",   "🏅", "Premier pas",       "Terminer ta 1ère leçon",             BadgeRarity.COMMON),
        Badge("first_exercise", "🎯", "À l'entraînement", "Compléter ton 1er exercice",          BadgeRarity.COMMON),
        Badge("first_piano",    "🎹", "Touche par touche", "Jouer du piano pendant 1 minute",     BadgeRarity.COMMON),

        // ── Leçons ────────────────────────────────────────────────────
        Badge("lessons_3",  "📖", "Apprenti",       "Compléter 3 leçons",                   BadgeRarity.COMMON),
        Badge("lessons_5",  "📚", "Studieux",       "Compléter 5 leçons",                   BadgeRarity.RARE),
        Badge("lessons_10", "🎓", "Diplômé",        "Terminer tout le parcours débutant",   BadgeRarity.LEGENDARY),

        // ── XP ────────────────────────────────────────────────────────
        Badge("xp_50",   "⭐",  "Première étoile", "Atteindre 50 XP",                     BadgeRarity.COMMON),
        Badge("xp_200",  "🌟",  "En progression",  "Atteindre 200 XP",                    BadgeRarity.RARE),
        Badge("xp_500",  "💫",  "Virtuose",        "Atteindre 500 XP",                    BadgeRarity.EPIC),

        // ── Streak ────────────────────────────────────────────────────
        Badge("streak_3",  "🔥", "Lancé !",          "3 jours consécutifs",                BadgeRarity.COMMON),
        Badge("streak_7",  "🔥🔥", "Sur un roll",   "7 jours consécutifs",                BadgeRarity.RARE),
        Badge("streak_30", "⚡", "Indestructible",   "30 jours consécutifs",               BadgeRarity.LEGENDARY),

        // ── Niveaux ───────────────────────────────────────────────────
        Badge("level_2", "🥈", "Niveau 2",           "Atteindre le niveau 2",              BadgeRarity.COMMON),
        Badge("level_3", "🥇", "Niveau 3",           "Atteindre le niveau 3",              BadgeRarity.RARE),
        Badge("level_5", "💎", "Niveau 5",           "Atteindre le niveau 5",              BadgeRarity.EPIC),
    )

    /** Retourne les IDs des badges débloqués pour un UserProgress donné */
    fun unlockedBadgeIds(progress: UserProgress): Set<String> {
        return buildSet {
            if (progress.completedLessonIds.isNotEmpty())        add("first_lesson")
            if (progress.todayExercises > 0 || progress.totalXp > 0) add("first_exercise")
            if (progress.todayMinutes >= 1)                      add("first_piano")
            if (progress.completedLessonIds.size >= 3)           add("lessons_3")
            if (progress.completedLessonIds.size >= 5)           add("lessons_5")
            if (progress.completedLessonIds.size >= 10)          add("lessons_10")
            if (progress.totalXp >= 50)                          add("xp_50")
            if (progress.totalXp >= 200)                         add("xp_200")
            if (progress.totalXp >= 500)                         add("xp_500")
            if (progress.currentStreak >= 3)                     add("streak_3")
            if (progress.currentStreak >= 7)                     add("streak_7")
            if (progress.currentStreak >= 30)                    add("streak_30")
            if (progress.level >= 2)                             add("level_2")
            if (progress.level >= 3)                             add("level_3")
            if (progress.level >= 5)                             add("level_5")
        }
    }

    fun getRarityColor(rarity: BadgeRarity) = when (rarity) {
        BadgeRarity.COMMON    -> 0xFF9090B8L   // gris-bleu
        BadgeRarity.RARE      -> 0xFF06B6D4L   // teal
        BadgeRarity.EPIC      -> 0xFF7C3AEDL   // violet
        BadgeRarity.LEGENDARY -> 0xFFF5C842L   // or
    }
}
