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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.data.LessonQuiz
import com.example.mypianoapp.data.QuizQuestion
import com.example.mypianoapp.data.reviewQuestions
import com.example.mypianoapp.ui.theme.*
import kotlinx.coroutines.delay

/**
 * QCM de palier — 4 questions fixes + 2 de révision aléatoires = 6 au total.
 * 100% requis pour valider.
 * @param onPass    appelé si 6/6 — débloque les leçons suivantes
 * @param onFail    appelé si < 6/6 — le palier reste verrouillé
 */
@Composable
fun LessonQuizScreen(
    quiz: LessonQuiz,
    lessonId: Int,
    onPass: () -> Unit,
    onFail: () -> Unit
) {
    // Mélange : 4 questions du palier + 2 questions de révision au hasard
    val allQuestions = remember {
        val fixed   = quiz.questions
        val reviews = reviewQuestions.shuffled().take(2)
        (fixed + reviews).shuffled()
    }

    val totalQ   = allQuestions.size   // toujours 6
    var currentQ by remember { mutableIntStateOf(0) }
    var score    by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var done     by remember { mutableStateOf(false) }

    // Auto-avance après feedback
    LaunchedEffect(selected) {
        if (selected != null) {
            delay(1000)
            if (currentQ + 1 >= totalQ) {
                done = true
            } else {
                currentQ++
                selected = null
            }
        }
    }

    if (done) {
        val passed = score == totalQ
        QuizResultScreen(
            score  = score,
            total  = totalQ,
            passed = passed,
            onContinue = { if (passed) onPass() else onFail() }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EbonyDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Palier de validation", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                Text("Question ${currentQ + 1} / $totalQ", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (score == currentQ && currentQ > 0) HarmonyGreen.copy(0.15f)
                        else IvoryGoldGlow
                    )
                    .border(
                        1.dp,
                        if (score == currentQ && currentQ > 0) HarmonyGreen.copy(0.4f)
                        else IvoryGold.copy(0.3f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "✓ $score / $totalQ",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (score == currentQ && currentQ > 0) HarmonyGreen else IvoryGold
                )
            }
        }

        // Règle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DissonanceRed.copy(0.08f))
                .border(1.dp, DissonanceRed.copy(0.25f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lock, null, tint = DissonanceRed, modifier = Modifier.size(14.dp))
            Text(
                "6/6 requis pour débloquer la suite",
                style = MaterialTheme.typography.labelSmall,
                color = DissonanceRed
            )
        }

        // Barre de progression
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            repeat(totalQ) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            when {
                                i < currentQ  -> KeysViolet
                                i == currentQ -> KeysVioletLight.copy(0.5f)
                                else          -> EbonySurface
                            }
                        )
                )
            }
        }

        // ── Question animée ───────────────────────────────────────────
        AnimatedContent(
            targetState  = currentQ,
            transitionSpec = {
                (slideInHorizontally(tween(260)) { it / 2 } + fadeIn(tween(220))) togetherWith
                (slideOutHorizontally(tween(240)) { -it / 2 } + fadeOut(tween(180)))
            },
            label = "question"
        ) { idx ->
            QuestionCard(
                question = allQuestions[idx],
                selected = selected,
                onSelect = { choice ->
                    if (selected == null) {
                        selected = choice
                        if (choice == allQuestions[idx].correctIndex) score++
                    }
                }
            )
        }
    }
}

// ── Carte question + réponses ─────────────────────────────────────────────

@Composable
private fun QuestionCard(
    question: QuizQuestion,
    selected: Int?,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(EbonyCard)
                .border(1.dp, EbonyBorder, RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Text(question.question, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        }
        question.answers.forEachIndexed { idx, answer ->
            AnswerButton(
                text     = answer,
                index    = idx,
                selected = selected,
                correct  = question.correctIndex,
                onSelect = onSelect
            )
        }
    }
}

@Composable
private fun AnswerButton(
    text: String,
    index: Int,
    selected: Int?,
    correct: Int,
    onSelect: (Int) -> Unit
) {
    val isSelected  = selected == index
    val isCorrect   = index == correct
    val hasAnswered = selected != null

    val bgColor by animateColorAsState(
        targetValue = when {
            !hasAnswered -> EbonyCard
            isCorrect    -> HarmonyGreen.copy(0.18f)
            isSelected   -> DissonanceRed.copy(0.15f)
            else         -> EbonyCard
        },
        animationSpec = tween(300), label = "bg_$index"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            !hasAnswered -> EbonyBorder
            isCorrect    -> HarmonyGreen.copy(0.6f)
            isSelected   -> DissonanceRed.copy(0.5f)
            else         -> EbonyBorder.copy(0.4f)
        },
        animationSpec = tween(300), label = "border_$index"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(enabled = !hasAnswered) { onSelect(index) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when {
                        !hasAnswered -> EbonySurface
                        isCorrect    -> HarmonyGreen.copy(0.3f)
                        isSelected   -> DissonanceRed.copy(0.25f)
                        else         -> EbonySurface.copy(0.5f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                hasAnswered && isCorrect              -> Icon(Icons.Default.CheckCircle, null, tint = HarmonyGreen, modifier = Modifier.size(16.dp))
                hasAnswered && isSelected && !isCorrect -> Icon(Icons.Default.Close, null, tint = DissonanceRed, modifier = Modifier.size(16.dp))
                else -> Text(listOf("A","B","C","D")[index], style = MaterialTheme.typography.labelMedium, color = if (hasAnswered) TextMuted else TextSecondary)
            }
        }
        Text(
            text  = text,
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                !hasAnswered -> TextPrimary
                isCorrect    -> HarmonyGreen
                isSelected   -> DissonanceRed
                else         -> TextMuted
            },
            modifier = Modifier.weight(1f)
        )
    }
}

// ── Résultat QCM ─────────────────────────────────────────────────────────

@Composable
private fun QuizResultScreen(
    score: Int,
    total: Int,
    passed: Boolean,
    onContinue: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EbonyDeep)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter   = scaleIn(tween(400, easing = EaseOutBack)) + fadeIn(tween(300))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Icône résultat
                Text(if (passed) "🏆" else "😅", style = MaterialTheme.typography.displayLarge)

                Text(
                    if (passed) "Palier validé !" else "Pas encore...",
                    style     = MaterialTheme.typography.displaySmall,
                    color     = TextPrimary,
                    textAlign = TextAlign.Center
                )

                // Score circulaire
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress    = { score.toFloat() / total.toFloat() },
                        modifier    = Modifier.size(110.dp),
                        strokeWidth = 9.dp,
                        color       = if (passed) HarmonyGreen else DissonanceRed,
                        trackColor  = EbonySurface
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$score/$total", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                        Text("bonnes", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }

                // Message résultat
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (passed) HarmonyGreen.copy(0.12f) else DissonanceRed.copy(0.1f)
                        )
                        .border(
                            1.dp,
                            if (passed) HarmonyGreen.copy(0.4f) else DissonanceRed.copy(0.35f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            if (passed) "✅ La suite du parcours est débloquée !"
                            else        "❌ Il faut 6/6 pour débloquer les leçons suivantes.",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (passed) HarmonyGreen else DissonanceRed
                        )
                        if (!passed) {
                            val wrong = total - score
                            Text(
                                "$wrong erreur${if (wrong > 1) "s" else ""}. Relis les leçons et retente le palier.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Bouton
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                if (passed) listOf(HarmonyGreen, HarmonyGreen.copy(0.7f))
                                else        listOf(KeysViolet, KeysVioletLight.copy(0.8f))
                            )
                        )
                        .clickable(onClick = onContinue),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!passed) Icon(Icons.Default.Refresh, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                        Text(
                            if (passed) "Continuer" else "Retenter le palier",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}
