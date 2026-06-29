package com.example.mypianoapp.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.data.LessonCatalog
import com.example.mypianoapp.data.LessonData
import com.example.mypianoapp.data.LessonStep
import com.example.mypianoapp.data.StepType
import com.example.mypianoapp.data.UserProgress
import com.example.mypianoapp.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LessonDetailScreen(
    lessonId: Int,
    progress: UserProgress,
    onLessonComplete: (Int, Int) -> Unit,
    onQuizPassed: (Int) -> Unit = {},
    onBack: () -> Unit
) {
    val lesson      = LessonCatalog.lessons.firstOrNull { it.id == lessonId } ?: return
    val hasQuiz     = lesson.quiz != null && !progress.isLessonCompleted(lessonId)

    // totalSteps = steps + (quiz si present) + recap
    val quizStepIndex = if (hasQuiz) lesson.steps.size else -1
    val recapIndex    = lesson.steps.size + (if (hasQuiz) 1 else 0)
    val totalSteps    = recapIndex + 1

    var currentStep          by remember { mutableStateOf(0) }
    val isQuizStep            = currentStep == quizStepIndex
    val isRecap               = currentStep == recapIndex
    val alreadyDone           = progress.isLessonCompleted(lessonId)
    var showReward           by remember { mutableStateOf(false) }
    var totalPracticeSeconds by remember { mutableStateOf(0) }
    var quizPassed           by remember { mutableStateOf(false) }

    if (showReward) {
        RewardDialog(
            lesson          = lesson,
            isNew           = !alreadyDone,
            practiceSeconds = totalPracticeSeconds,
            nextLessonTitle = LessonCatalog.lessons.firstOrNull { it.id == lessonId + 1 }?.title,
            onDismiss       = {
                showReward = false
                if (!alreadyDone) onLessonComplete(lesson.id, lesson.xpReward)
                onBack()
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(EbonyDeep)) {
        LessonTopBar(lesson = lesson, currentStep = currentStep, totalSteps = totalSteps, onBack = onBack)

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState > initialState)
                    (slideInHorizontally(tween(280)) { it / 2 } + fadeIn(tween(280))) togetherWith
                    (slideOutHorizontally(tween(280)) { -it / 2 } + fadeOut(tween(200)))
                else
                    (slideInHorizontally(tween(280)) { -it / 2 } + fadeIn(tween(280))) togetherWith
                    (slideOutHorizontally(tween(280)) { it / 2 } + fadeOut(tween(200)))
            },
            label    = "step_content",
            modifier = Modifier.weight(1f)
        ) { step ->
            when {
                // Etape normale
                step < lesson.steps.size -> StepContent(
                    step           = lesson.steps[step],
                    onPracticeTime = { seconds -> totalPracticeSeconds += seconds },
                    modifier       = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                )
                // QCM palier
                step == quizStepIndex && lesson.quiz != null -> LessonQuizScreen(
                    quiz     = lesson.quiz,
                    lessonId = lesson.id,
                    onPass   = {
                        quizPassed = true
                        onQuizPassed(lesson.id)
                        currentStep++
                    },
                    onFail   = {
                        // Reste sur le QCM pour retenter
                        currentStep = quizStepIndex
                    }
                )
                // Recap
                else -> RecapContent(
                    lesson      = lesson,
                    alreadyDone = alreadyDone,
                    modifier    = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                )
            }
        }

        // Masquer la nav pendant le QCM (il a ses propres boutons)
        if (!isQuizStep) {
            LessonBottomNav(
                currentStep = currentStep,
                totalSteps  = totalSteps,
                isRecap     = isRecap,
                onPrev      = { if (currentStep > 0) currentStep-- },
                onNext      = { if (currentStep < totalSteps - 1) currentStep++ },
                onFinish    = { showReward = true }
            )
        }
    }
}

// ── Timer play/pause/stop ────────────────────────────────────────────────

enum class TimerState { IDLE, RUNNING, PAUSED, DONE }

@Composable
private fun ExerciseTimer(
    durationLabel: String,           // ex: "3 minutes"
    onFinished: (elapsedSeconds: Int) -> Unit
) {
    // Parse la durée cible depuis le label (ex: "3 minutes" → 180s, "30 secondes" → 30s)
    val targetSeconds = remember(durationLabel) {
        val mins = Regex("""(\d+)\s*min""").find(durationLabel)?.groupValues?.get(1)?.toIntOrNull()
        val secs = Regex("""(\d+)\s*sec""").find(durationLabel)?.groupValues?.get(1)?.toIntOrNull()
        val total = (mins?.times(60) ?: 0) + (secs ?: 0)
        if (total > 0) total else 60
    }

    var timerState  by remember { mutableStateOf(TimerState.IDLE) }
    var elapsed     by remember { mutableStateOf(0) }
    var reported    by remember { mutableStateOf(false) }

    // Chrono
    LaunchedEffect(timerState) {
        if (timerState == TimerState.RUNNING) {
            while (timerState == TimerState.RUNNING && elapsed < targetSeconds) {
                delay(1_000)
                elapsed++
            }
            if (elapsed >= targetSeconds && timerState == TimerState.RUNNING) {
                timerState = TimerState.DONE
            }
        }
    }

    // Notifier les minutes de pratique quand stop ou done
    LaunchedEffect(timerState) {
        if ((timerState == TimerState.DONE || timerState == TimerState.IDLE) && elapsed > 0 && !reported) {
            reported = true
            onFinished(elapsed)
        }
    }

    val progress = if (targetSeconds > 0) elapsed.toFloat() / targetSeconds.toFloat() else 0f
    val isDone   = timerState == TimerState.DONE

    // Animation pulsante quand en cours
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val dotScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.4f,
        animationSpec = infiniteRepeatable(tween(600, easing = EaseInOut), RepeatMode.Reverse),
        label         = "dot"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(EbonyCard)
            .border(
                1.dp,
                when (timerState) {
                    TimerState.RUNNING -> NotesTeal.copy(0.5f)
                    TimerState.DONE    -> HarmonyGreen.copy(0.5f)
                    else               -> EbonyBorder
                },
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header timer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicateur d'état
                if (timerState == TimerState.RUNNING) {
                    Box(
                        modifier = Modifier
                            .scale(dotScale)
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(NotesTeal)
                    )
                }
                Text(
                    text = when (timerState) {
                        TimerState.IDLE    -> "Chronomètre"
                        TimerState.RUNNING -> "En cours..."
                        TimerState.PAUSED  -> "En pause"
                        TimerState.DONE    -> "Terminé !"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = when (timerState) {
                        TimerState.RUNNING -> NotesTeal
                        TimerState.DONE    -> HarmonyGreen
                        TimerState.PAUSED  -> IvoryGold
                        else               -> TextSecondary
                    }
                )
            }

            // Temps affiché
            Text(
                text  = "%02d:%02d".format(elapsed / 60, elapsed % 60),
                style = MaterialTheme.typography.titleLarge,
                color = when (timerState) {
                    TimerState.RUNNING -> NotesTeal
                    TimerState.DONE    -> HarmonyGreen
                    TimerState.PAUSED  -> IvoryGold
                    else               -> TextMuted
                }
            )
        }

        // Barre de progression
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(EbonySurface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        when {
                            isDone -> HarmonyGreen
                            else   -> NotesTeal
                        }
                    )
            )
        }

        // Boutons play/pause/stop
        if (!isDone) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Play / Pause
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (timerState) {
                                TimerState.RUNNING -> IvoryGold.copy(0.15f)
                                else               -> NotesTeal.copy(0.15f)
                            }
                        )
                        .border(
                            1.dp,
                            when (timerState) {
                                TimerState.RUNNING -> IvoryGold.copy(0.4f)
                                else               -> NotesTeal.copy(0.4f)
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            timerState = when (timerState) {
                                TimerState.IDLE, TimerState.PAUSED -> TimerState.RUNNING
                                TimerState.RUNNING                 -> TimerState.PAUSED
                                else -> timerState
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (timerState == TimerState.RUNNING)
                                Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint     = if (timerState == TimerState.RUNNING) IvoryGold else NotesTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text  = if (timerState == TimerState.RUNNING) "Pause" else "Démarrer",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (timerState == TimerState.RUNNING) IvoryGold else NotesTeal
                        )
                    }
                }

                // Stop (visible seulement si démarré)
                if (timerState != TimerState.IDLE) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DissonanceRed.copy(0.1f))
                            .border(1.dp, DissonanceRed.copy(0.35f), RoundedCornerShape(12.dp))
                            .clickable {
                                timerState = TimerState.IDLE
                                if (elapsed > 0 && !reported) {
                                    reported = true
                                    onFinished(elapsed)
                                }
                                elapsed = 0
                                reported = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint     = DissonanceRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        } else {
            // Message de fin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = HarmonyGreen, modifier = Modifier.size(18.dp))
                Text(
                    "Exercice terminé — %02d:%02d pratiqués".format(elapsed / 60, elapsed % 60),
                    style = MaterialTheme.typography.labelMedium,
                    color = HarmonyGreen
                )
            }
        }
    }
}

// ── Contenu d'une étape ──────────────────────────────────────────────────

@Composable
private fun StepContent(
    step: LessonStep,
    onPracticeTime: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val (typeLabel, typeBg, typeText) = when (step.type) {
            StepType.THEORY   -> Triple("Théorie",           EbonySurface,  KeysVioletLight)
            StepType.EXERCISE -> Triple("Exercice pratique", NotesTealGlow, NotesTeal)
            StepType.TIP      -> Triple("Conseil",           IvoryGoldGlow, IvoryGold)
        }

        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(typeBg)
            .padding(horizontal = 10.dp, vertical = 5.dp)) {
            Text(typeLabel, style = MaterialTheme.typography.labelSmall, color = typeText)
        }
        Text(step.title, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        Text(step.body,  style = MaterialTheme.typography.bodyMedium,     color = TextSecondary)

        step.highlight?.let { hl ->
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(KeysVioletGlow)
                    .border(1.dp, KeysViolet.copy(0.3f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("💡", style = MaterialTheme.typography.titleMedium)
                Text(hl, style = MaterialTheme.typography.bodySmall, color = KeysVioletLight)
            }
        }

        step.exercise?.let { ex ->
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(EbonyCard)
                    .border(1.dp, NotesTeal.copy(0.25f), RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🎯", style = MaterialTheme.typography.titleMedium)
                    Text("Exercice", style = MaterialTheme.typography.labelMedium, color = NotesTeal)
                }
                Text(ex.instruction, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)

                if (ex.notes.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ex.notes.forEach { note ->
                            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                .background(EbonySurface)
                                .border(1.dp, EbonyBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(note, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                            }
                        }
                    }
                }

                // ── Timer play/pause/stop ─────────────────────────────
                ExerciseTimer(
                    durationLabel = ex.duration,
                    onFinished    = { elapsedSeconds -> onPracticeTime(elapsedSeconds) }
                )

                if (ex.goal.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏁", style = MaterialTheme.typography.bodySmall)
                        Text(ex.goal, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
            }
        }
    }
}

// ── Dialog de récompense ─────────────────────────────────────────────────

@Composable
private fun RewardDialog(
    lesson: LessonData,
    isNew: Boolean,
    practiceSeconds: Int,
    nextLessonTitle: String?,
    onDismiss: () -> Unit
) {
    var trophyVisible by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "trophy")
    val trophyScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.08f,
        animationSpec = infiniteRepeatable(tween(700, easing = EaseInOut), RepeatMode.Reverse),
        label         = "trophy_scale"
    )
    LaunchedEffect(Unit) { delay(100); trophyVisible = true }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = EbonyCard,
        shape            = RoundedCornerShape(24.dp),
        title  = null,
        text   = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(visible = trophyVisible, enter = scaleIn(tween(400, easing = EaseOutBack)) + fadeIn(tween(300))) {
                    Box(
                        modifier = Modifier
                            .scale(trophyScale)
                            .size(80.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(IvoryGoldGlow)
                            .border(1.dp, IvoryGold.copy(0.5f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EmojiEvents, null, tint = IvoryGold, modifier = Modifier.size(44.dp))
                    }
                }
                Text(
                    if (isNew) "Leçon terminée !" else "Révision complète !",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                if (isNew) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                            .background(IvoryGoldGlow)
                            .border(1.dp, IvoryGold.copy(0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text("+${lesson.xpReward} XP", style = MaterialTheme.typography.titleLarge, color = IvoryGold)
                    }
                }
                // Temps de pratique
                if (practiceSeconds > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(NotesTealGlow)
                            .border(1.dp, NotesTeal.copy(0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏱", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "%02d min %02d sec pratiqués".format(practiceSeconds / 60, practiceSeconds % 60),
                            style = MaterialTheme.typography.bodySmall,
                            color = NotesTeal
                        )
                    }
                }
                if (isNew && nextLessonTitle != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(HarmonyGreen.copy(0.1f))
                            .border(1.dp, HarmonyGreen.copy(0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔓", style = MaterialTheme.typography.titleMedium)
                        Column {
                            Text("Leçon débloquée !", style = MaterialTheme.typography.labelSmall, color = HarmonyGreen)
                            Text(nextLessonTitle, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(KeysViolet)
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Continuer", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
            }
        }
    )
}

// ── Top bar ───────────────────────────────────────────────────────────────

@Composable
private fun LessonTopBar(lesson: LessonData, currentStep: Int, totalSteps: Int, onBack: () -> Unit) {
    val frac = (currentStep + 1).toFloat() / totalSteps.toFloat()
    val animFrac by animateFloatAsState(targetValue = frac, animationSpec = tween(400), label = "topbar")

    Column(modifier = Modifier.fillMaxWidth().background(EbonyCard)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                    .background(EbonySurface).clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text("Étape ${currentStep + 1} sur $totalSteps", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .background(IvoryGoldGlow)
                    .border(1.dp, IvoryGold.copy(0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text("+${lesson.xpReward} XP", style = MaterialTheme.typography.labelSmall, color = IvoryGold)
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(EbonySurface)) {
            Box(modifier = Modifier.fillMaxWidth(animFrac).fillMaxHeight().background(KeysViolet))
        }
    }
}

// ── Récap final ───────────────────────────────────────────────────────────

@Composable
private fun RecapContent(lesson: LessonData, alreadyDone: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(24.dp))
                .background(KeysVioletGlow).border(1.dp, KeysViolet.copy(0.4f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.EmojiEvents, null, tint = IvoryGold, modifier = Modifier.size(40.dp))
        }
        Text("Leçon terminée !", style = MaterialTheme.typography.displaySmall, color = TextPrimary, textAlign = TextAlign.Center)
        Text(
            if (alreadyDone) "Déjà complétée — révision effectuée" else "+${lesson.xpReward} XP gagnés",
            style = MaterialTheme.typography.titleMedium,
            color = if (alreadyDone) TextSecondary else IvoryGold
        )
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(EbonyCard).border(1.dp, EbonyBorder, RoundedCornerShape(16.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("CE QU'ON A VU", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(lesson.recap, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            lesson.steps.forEach { step ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = HarmonyGreen, modifier = Modifier.size(18.dp))
                    Text(step.title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}

// ── Navigation bas ────────────────────────────────────────────────────────

@Composable
private fun LessonBottomNav(
    currentStep: Int,
    totalSteps: Int,
    isRecap: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(EbonyCard)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentStep > 0) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                    .background(EbonySurface).border(1.dp, EbonyBorder, RoundedCornerShape(14.dp))
                    .clickable(onClick = onPrev),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
        } else {
            Spacer(Modifier.size(48.dp))
        }
        Box(
            modifier = Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(14.dp))
                .background(if (isRecap) HarmonyGreen else KeysViolet)
                .clickable(onClick = if (isRecap) onFinish else onNext),
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    when {
                        isRecap -> "Terminer la leçon"
                        currentStep == totalSteps - 2 -> "Voir le récap"
                        else -> "Étape suivante"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
                if (!isRecap) Icon(Icons.Default.ArrowForward, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
            }
        }
    }
}
