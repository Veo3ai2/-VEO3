package com.example.languagepartner.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProgress(
    val streak: Int = 1,
    val totalMessagesSent: Int = 0,
    val grammarAccuracyScore: Int = 100,
    val lastActiveDate: String? = null,
    val dailyMessageGoal: Int = 10,
    val todayMessagesSent: Int = 0,
    val dailyVocabGoal: Int = 5,
    val todayVocabAdded: Int = 0
) {
    val activeTodayMessages: Int
        get() {
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val today = dateFormat.format(java.util.Date())
            return if (lastActiveDate == today) todayMessagesSent else 0
        }

    val activeTodayVocab: Int
        get() {
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val today = dateFormat.format(java.util.Date())
            return if (lastActiveDate == today) todayVocabAdded else 0
        }

    val dailyGoalProgress: Float
        get() = if (dailyMessageGoal > 0) {
            (activeTodayMessages.toFloat() / dailyMessageGoal.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val dailyVocabProgress: Float
        get() = if (dailyVocabGoal > 0) {
            (activeTodayVocab.toFloat() / dailyVocabGoal.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val isGoalAchieved: Boolean
        get() = activeTodayMessages >= dailyMessageGoal && dailyMessageGoal > 0

    val isVocabGoalAchieved: Boolean
        get() = activeTodayVocab >= dailyVocabGoal && dailyVocabGoal > 0
}
