package com.example.languagepartner.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Feedback(
    val grammarCorrect: Boolean,
    val correctedText: String? = null,
    val explanation: String? = null,
    val alternativeSuggestions: List<String>? = null,
    val vocabularyNotes: List<VocabNote>? = null
)

@Serializable
data class VocabNote(
    val word: String,
    val translation: String,
    val note: String? = null
)
