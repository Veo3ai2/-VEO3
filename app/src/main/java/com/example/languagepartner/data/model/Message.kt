package com.example.languagepartner.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val role: String, // "user" or "model"
    val text: String,
    val translation: String? = null,
    val feedback: Feedback? = null,
    val pronunciationHint: String? = null,
    val suggestedReplies: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
