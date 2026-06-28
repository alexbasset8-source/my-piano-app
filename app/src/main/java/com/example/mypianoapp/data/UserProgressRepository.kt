package com.example.mypianoapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_progress")

class UserProgressRepository(private val context: Context) {

    companion object {
        private val KEY_USER_NAME         = stringPreferencesKey("user_name")
        private val KEY_TOTAL_XP          = intPreferencesKey("total_xp")
        private val KEY_COMPLETED_LESSONS = stringPreferencesKey("completed_lessons")
        private val KEY_STREAK            = intPreferencesKey("current_streak")
        private val KEY_LAST_SESSION_DAY  = longPreferencesKey("last_session_day")
        private val KEY_TODAY_MINUTES     = intPreferencesKey("today_minutes")
        private val KEY_TODAY_EXERCISES   = intPreferencesKey("today_exercises")
        private val KEY_TODAY_LESSONS     = intPreferencesKey("today_lessons")
        private val KEY_TODAY_DATE        = longPreferencesKey("today_date")
        val KEY_ONBOARDING_DONE       = booleanPreferencesKey("onboarding_done")
        private val KEY_PASSED_QUIZZES    = stringPreferencesKey("passed_quizzes")
        private val KEY_DAILY_GOAL        = intPreferencesKey("daily_goal_minutes")
    }

    private val json = Json { ignoreUnknownKeys = true }

    // ── Lecture ───────────────────────────────────────────────────────

    val userProgressFlow: Flow<UserProgress> = context.dataStore.data.map { prefs ->
        val completedJson = prefs[KEY_COMPLETED_LESSONS] ?: "[]"
        val completedIds = try {
            json.decodeFromString<Set<Int>>(completedJson)
        } catch (e: Exception) { emptySet() }
        val passedJson = prefs[KEY_PASSED_QUIZZES] ?: "[]"
        val passedQuizIds = try { json.decodeFromString<Set<Int>>(passedJson) } catch (e: Exception) { emptySet() }
        UserProgress(
            userName                = prefs[KEY_USER_NAME] ?: "Pianiste",
            totalXp                 = prefs[KEY_TOTAL_XP] ?: 0,
            completedLessonIds      = completedIds,
            currentStreak           = prefs[KEY_STREAK] ?: 0,
            lastSessionDateEpochDay = prefs[KEY_LAST_SESSION_DAY] ?: -1L,
            todayMinutes            = prefs[KEY_TODAY_MINUTES] ?: 0,
            todayExercises          = prefs[KEY_TODAY_EXERCISES] ?: 0,
            todayLessonsCompleted   = prefs[KEY_TODAY_LESSONS] ?: 0,
            todayDateEpochDay       = prefs[KEY_TODAY_DATE] ?: -1L,
            dailyGoalMinutes        = prefs[KEY_DAILY_GOAL] ?: 10,
            passedQuizIds           = passedQuizIds
        )
    }

    val onboardingDoneFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_DONE] ?: false
    }

    // ── Écriture ──────────────────────────────────────────────────────

    suspend fun completeOnboarding(name: String, dailyGoalMinutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_NAME]       = name.trim().ifEmpty { "Pianiste" }
            prefs[KEY_DAILY_GOAL]      = dailyGoalMinutes
            prefs[KEY_ONBOARDING_DONE] = true
        }
    }

    suspend fun passQuiz(lessonId: Int) {
        context.dataStore.edit { prefs ->
            val current = try {
                json.decodeFromString<Set<Int>>(prefs[KEY_PASSED_QUIZZES] ?: "[]")
            } catch (e: Exception) { emptySet() }
            prefs[KEY_PASSED_QUIZZES] = json.encodeToString(current + lessonId)
        }
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { it[KEY_USER_NAME] = name }
    }

    suspend fun completeLesson(lessonId: Int, xpReward: Int, todayEpochDay: Long) {
        context.dataStore.edit { prefs ->
            val current = try {
                json.decodeFromString<Set<Int>>(prefs[KEY_COMPLETED_LESSONS] ?: "[]")
            } catch (e: Exception) { emptySet() }
            if (lessonId !in current) {
                prefs[KEY_COMPLETED_LESSONS] = json.encodeToString(current + lessonId)
                prefs[KEY_TOTAL_XP] = (prefs[KEY_TOTAL_XP] ?: 0) + xpReward
            }
            val savedDay = prefs[KEY_TODAY_DATE] ?: -1L
            if (savedDay == todayEpochDay) {
                prefs[KEY_TODAY_LESSONS] = (prefs[KEY_TODAY_LESSONS] ?: 0) + 1
            } else {
                prefs[KEY_TODAY_DATE]      = todayEpochDay
                prefs[KEY_TODAY_LESSONS]   = 1
                prefs[KEY_TODAY_MINUTES]   = 0
                prefs[KEY_TODAY_EXERCISES] = 0
            }
        }
    }

    suspend fun addPracticeMinutes(minutes: Int, todayEpochDay: Long) {
        context.dataStore.edit { prefs ->
            val savedDay = prefs[KEY_TODAY_DATE] ?: -1L
            if (savedDay == todayEpochDay) {
                prefs[KEY_TODAY_MINUTES] = (prefs[KEY_TODAY_MINUTES] ?: 0) + minutes
            } else {
                prefs[KEY_TODAY_DATE]      = todayEpochDay
                prefs[KEY_TODAY_MINUTES]   = minutes
                prefs[KEY_TODAY_LESSONS]   = 0
                prefs[KEY_TODAY_EXERCISES] = 0
            }
        }
    }

    suspend fun completeExercise(xpReward: Int, todayEpochDay: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOTAL_XP] = (prefs[KEY_TOTAL_XP] ?: 0) + xpReward
            val savedDay = prefs[KEY_TODAY_DATE] ?: -1L
            if (savedDay == todayEpochDay) {
                prefs[KEY_TODAY_EXERCISES] = (prefs[KEY_TODAY_EXERCISES] ?: 0) + 1
            } else {
                prefs[KEY_TODAY_DATE]      = todayEpochDay
                prefs[KEY_TODAY_EXERCISES] = 1
                prefs[KEY_TODAY_LESSONS]   = 0
                prefs[KEY_TODAY_MINUTES]   = 0
            }
        }
    }

    suspend fun updateStreak(todayEpochDay: Long) {
        context.dataStore.edit { prefs ->
            val lastDay = prefs[KEY_LAST_SESSION_DAY] ?: -1L
            val streak  = prefs[KEY_STREAK] ?: 0
            prefs[KEY_LAST_SESSION_DAY] = todayEpochDay
            prefs[KEY_STREAK] = when {
                lastDay == -1L                -> 1
                todayEpochDay - lastDay == 1L -> streak + 1
                todayEpochDay == lastDay      -> streak
                else                          -> 1
            }
        }
    }

    suspend fun resetProgress() {
        context.dataStore.edit { it.clear() }
    }
}
