package com.example.languagepartner.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProgress(
    val streak: Int = 1,
    val totalMessagesSent: Int = 0,
    val grammarAccuracyScore: Int = 100,
    val lastActiveDate: String? = null
)
