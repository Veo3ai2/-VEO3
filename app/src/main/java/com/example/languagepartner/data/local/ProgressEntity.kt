package com.example.languagepartner.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.languagepartner.data.model.UserProgress

@Entity(tableName = "user_progress")
data class ProgressEntity(
    @PrimaryKey
    val id: Int = 1,
    val streak: Int,
    val totalMessagesSent: Int,
    val grammarAccuracyScore: Int,
    val lastActiveDate: String?,
    @ColumnInfo(defaultValue = "10")
    val dailyMessageGoal: Int = 10,
    @ColumnInfo(defaultValue = "0")
    val todayMessagesSent: Int = 0,
    @ColumnInfo(defaultValue = "5")
    val dailyVocabGoal: Int = 5,
    @ColumnInfo(defaultValue = "0")
    val todayVocabAdded: Int = 0
) {
    fun toDomain(): UserProgress = UserProgress(
        streak = streak,
        totalMessagesSent = totalMessagesSent,
        grammarAccuracyScore = grammarAccuracyScore,
        lastActiveDate = lastActiveDate,
        dailyMessageGoal = dailyMessageGoal,
        todayMessagesSent = todayMessagesSent,
        dailyVocabGoal = dailyVocabGoal,
        todayVocabAdded = todayVocabAdded
    )

    companion object {
        fun fromDomain(progress: UserProgress): ProgressEntity = ProgressEntity(
            id = 1,
            streak = progress.streak,
            totalMessagesSent = progress.totalMessagesSent,
            grammarAccuracyScore = progress.grammarAccuracyScore,
            lastActiveDate = progress.lastActiveDate,
            dailyMessageGoal = progress.dailyMessageGoal,
            todayMessagesSent = progress.todayMessagesSent,
            dailyVocabGoal = progress.dailyVocabGoal,
            todayVocabAdded = progress.todayVocabAdded
        )
    }
}
