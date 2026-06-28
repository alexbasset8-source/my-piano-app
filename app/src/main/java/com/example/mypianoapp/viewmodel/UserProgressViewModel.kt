package com.example.mypianoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypianoapp.data.Badge
import com.example.mypianoapp.data.BadgeSystem
import com.example.mypianoapp.data.UserProgress
import com.example.mypianoapp.data.UserProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class UserProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserProgressRepository(application)

    val progress: StateFlow<UserProgress> = repository.userProgressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserProgress())

    val onboardingDone: StateFlow<Boolean> = repository.onboardingDoneFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _levelUpEvent = MutableStateFlow<Int?>(null)
    val levelUpEvent: StateFlow<Int?> = _levelUpEvent

    private val _newBadges = MutableStateFlow<List<Badge>>(emptyList())
    val newBadges: StateFlow<List<Badge>> = _newBadges

    private var knownBadgeIds: Set<String> = emptySet()

    init {
        viewModelScope.launch {
            progress.collect { p -> knownBadgeIds = BadgeSystem.unlockedBadgeIds(p) }
        }
    }

    fun completeOnboarding(name: String, dailyGoalMinutes: Int) {
        viewModelScope.launch {
            repository.completeOnboarding(name, dailyGoalMinutes)
        }
    }

    fun completeLesson(lessonId: Int, xpReward: Int) {
        viewModelScope.launch {
            val before = progress.value
            repository.completeLesson(lessonId, xpReward, LocalDate.now().toEpochDay())
            repository.updateStreak(LocalDate.now().toEpochDay())
            checkGamificationEvents(before)
        }
    }

    fun completeExercise(xpEarned: Int) {
        viewModelScope.launch {
            val before = progress.value
            repository.completeExercise(xpEarned, LocalDate.now().toEpochDay())
            repository.updateStreak(LocalDate.now().toEpochDay())
            checkGamificationEvents(before)
        }
    }

    fun addPracticeMinutes(minutes: Int) {
        viewModelScope.launch {
            val before = progress.value
            repository.addPracticeMinutes(minutes, LocalDate.now().toEpochDay())
            checkGamificationEvents(before)
        }
    }

    fun passQuiz(lessonId: Int) {
        viewModelScope.launch {
            repository.passQuiz(lessonId)
        }
    }

    fun saveUserName(name: String) {
        viewModelScope.launch { repository.saveUserName(name) }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetProgress()
            _levelUpEvent.value = null
            _newBadges.value    = emptyList()
            knownBadgeIds       = emptySet()
        }
    }

    fun dismissLevelUp() { _levelUpEvent.value = null }
    fun dismissBadge(badgeId: String) {
        _newBadges.update { it.filterNot { b -> b.id == badgeId } }
    }

    private suspend fun checkGamificationEvents(before: UserProgress) {
        kotlinx.coroutines.delay(300)
        val after = progress.value
        if (after.level > before.level) _levelUpEvent.value = after.level
        val newlyUnlocked = BadgeSystem.unlockedBadgeIds(after) - knownBadgeIds
        if (newlyUnlocked.isNotEmpty()) {
            _newBadges.update { it + BadgeSystem.allBadges.filter { b -> b.id in newlyUnlocked } }
            knownBadgeIds = BadgeSystem.unlockedBadgeIds(after)
        }
    }
}
