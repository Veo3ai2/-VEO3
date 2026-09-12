package com.example.languagepartner.data.local

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
    val lastActiveDate: String?
) {
    fun toDomain(): UserProgress = UserProgress(
        streak = streak,
        totalMessagesSent = totalMessagesSent,
        grammarAccuracyScore = grammarAccuracyScore,
        lastActiveDate = lastActiveDate
    )

    companion object {
        fun fromDomain(progress: UserProgress): ProgressEntity = ProgressEntity(
            id = 1,
            streak = progress.streak,
            totalMessagesSent = progress.totalMessagesSent,
            grammarAccuracyScore = progress.grammarAccuracyScore,
            lastActiveDate = progress.lastActiveDate
        )
    }
}
