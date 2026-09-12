package com.example.languagepartner.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.languagepartner.data.model.Feedback
import com.example.languagepartner.data.model.Message
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val partnerId: String,
    val role: String,
    val text: String,
    val translation: String?,
    val pronunciationHint: String?,
    val suggestedRepliesJson: String,
    val grammarCorrect: Boolean?,
    val correctedText: String?,
    val explanation: String?,
    val timestamp: Long
) {
    fun toDomain(): Message {
        val replies = try {
            if (suggestedRepliesJson.isNotEmpty()) {
                Json.decodeFromString<List<String>>(suggestedRepliesJson)
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val feedback = if (grammarCorrect != null) {
            Feedback(
                grammarCorrect = grammarCorrect,
                correctedText = correctedText,
                explanation = explanation
            )
        } else null

        return Message(
            id = id,
            role = role,
            text = text,
            translation = translation,
            feedback = feedback,
            pronunciationHint = pronunciationHint,
            suggestedReplies = replies,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomain(partnerId: String, message: Message): ChatMessageEntity {
            val repliesJson = try {
                Json.encodeToString(message.suggestedReplies)
            } catch (e: Exception) {
                ""
            }

            return ChatMessageEntity(
                id = message.id,
                partnerId = partnerId,
                role = message.role,
                text = message.text,
                translation = message.translation,
                pronunciationHint = message.pronunciationHint,
                suggestedRepliesJson = repliesJson,
                grammarCorrect = message.feedback?.grammarCorrect,
                correctedText = message.feedback?.correctedText,
                explanation = message.feedback?.explanation,
                timestamp = message.timestamp
            )
        }
    }
}
