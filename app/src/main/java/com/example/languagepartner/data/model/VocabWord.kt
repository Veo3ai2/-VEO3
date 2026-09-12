package com.example.languagepartner.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VocabWord(
    val id: String,
    val word: String,
    val translation: String,
    val languageName: String,
    val contextSentence: String,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
