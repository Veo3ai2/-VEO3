package com.example.languagepartner.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.languagepartner.data.model.VocabWord

@Entity(tableName = "vocab_words")
data class VocabEntity(
    @PrimaryKey
    val id: String,
    val word: String,
    val translation: String,
    val languageName: String,
    val contextSentence: String,
    val notes: String?,
    val createdAt: Long
) {
    fun toDomain(): VocabWord = VocabWord(
        id = id,
        word = word,
        translation = translation,
        languageName = languageName,
        contextSentence = contextSentence,
        notes = notes,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(word: VocabWord): VocabEntity = VocabEntity(
            id = word.id,
            word = word.word,
            translation = word.translation,
            languageName = word.languageName,
            contextSentence = word.contextSentence,
            notes = word.notes,
            createdAt = word.createdAt
        )
    }
}
