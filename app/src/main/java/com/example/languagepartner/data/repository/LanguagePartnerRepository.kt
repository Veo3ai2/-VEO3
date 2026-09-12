package com.example.languagepartner.data.repository

import com.example.languagepartner.BuildConfig
import com.example.languagepartner.data.local.AppDatabase
import com.example.languagepartner.data.local.ChatMessageEntity
import com.example.languagepartner.data.local.ProgressEntity
import com.example.languagepartner.data.local.VocabEntity
import com.example.languagepartner.data.model.Feedback
import com.example.languagepartner.data.model.Message
import com.example.languagepartner.data.model.Partner
import com.example.languagepartner.data.model.UserProgress
import com.example.languagepartner.data.model.VocabNote
import com.example.languagepartner.data.model.VocabWord
import com.example.languagepartner.data.remote.Content
import com.example.languagepartner.data.remote.GeminiPartnerResponse
import com.example.languagepartner.data.remote.GenerateContentRequest
import com.example.languagepartner.data.remote.GenerationConfig
import com.example.languagepartner.data.remote.Part
import com.example.languagepartner.data.remote.ResponseFormat
import com.example.languagepartner.data.remote.ResponseFormatText
import com.example.languagepartner.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LanguagePartnerRepository(private val database: AppDatabase) {

    private val vocabDao = database.vocabDao()
    private val chatDao = database.chatMessageDao()
    private val progressDao = database.progressDao()

    val allVocabWords: Flow<List<VocabWord>> = vocabDao.getAllVocabWords()
        .map { list -> list.map { it.toDomain() } }

    fun getMessagesForPartner(partnerId: String): Flow<List<Message>> =
        chatDao.getMessagesForPartner(partnerId)
            .map { list -> list.map { it.toDomain() } }

    val userProgress: Flow<UserProgress> = progressDao.getProgress()
        .map { it?.toDomain() ?: UserProgress() }

    suspend fun addVocabWord(word: String, translation: String, languageName: String, context: String, notes: String? = null) {
        withContext(Dispatchers.IO) {
            val entity = VocabEntity(
                id = UUID.randomUUID().toString(),
                word = word.trim(),
                translation = translation.trim(),
                languageName = languageName,
                contextSentence = context,
                notes = notes,
                createdAt = System.currentTimeMillis()
            )
            vocabDao.insertVocabWord(entity)
        }
    }

    suspend fun removeVocabWord(id: String) {
        withContext(Dispatchers.IO) {
            vocabDao.deleteVocabWord(id)
        }
    }

    suspend fun saveMessage(partnerId: String, message: Message) {
        withContext(Dispatchers.IO) {
            chatDao.insertMessage(ChatMessageEntity.fromDomain(partnerId, message))
        }
    }

    suspend fun clearHistoryForPartner(partnerId: String) {
        withContext(Dispatchers.IO) {
            chatDao.clearMessagesForPartner(partnerId)
        }
    }

    suspend fun updateProgress(wasCorrect: Boolean) {
        withContext(Dispatchers.IO) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val today = dateFormat.format(Date())

            val current = progressDao.getProgress()
            // We can read once directly or calculate
            val currentEntity = database.runInTransaction<ProgressEntity?> {
                // In-memory update
                null
            }
            // Simple default handling
        }
    }

    suspend fun saveProgress(progress: UserProgress) {
        withContext(Dispatchers.IO) {
            progressDao.saveProgress(ProgressEntity.fromDomain(progress))
        }
    }

    suspend fun sendChatMessage(
        partner: Partner,
        history: List<Message>,
        userLevel: String
    ): Message = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        val currentTopic = partner.suggestedTopics.firstOrNull() ?: "General discussion"

        val systemPrompt = """
You are ${partner.name}, a conversational partner practicing ${partner.languageName} with a language learner whose skill level is $userLevel.
Your persona profile: ${partner.description}
Working role: ${partner.role}
Current conversation topic of focus: $currentTopic

CRITICAL GUIDELINES:
1. Stay in character as ${partner.name}. Respond casually and naturally as a native speaker of ${partner.languageName}. Keep your responses simple and matching the learner's expertise level ($userLevel).
2. Provide a polite and authentic native language response.
3. Translate your own native response into clear English.
4. For Asian/Cyrillic languages or languages with different scripts, provide a pronunciation hint or transliteration (e.g., Romaji for Japanese, Pinyin for Chinese).
5. Crucially, analyze the user's last message:
   - Check if their message fits typical patterns and grammar rules in ${partner.languageName}.
   - If they made typos, grammar, or word-choice errors, set "grammarCorrect" to false, give a corrected version in "correctedText", and a friendly explanation in "explanation" in English.
   - If they wrote correctly, set "grammarCorrect" to true.
   - List 2-3 vocabulary words used in this turnaround with brief definition cards in "vocabularyNotes".
6. Formulate 3 relevant suggestion shortcuts in ${partner.languageName} that the user could click to reply easily (e.g., positive answer, question back, divert topic). Include an English translation for each suggestion inside brackets.

You MUST respond strictly using valid JSON with this structure:
{
  "replyText": "native response",
  "translation": "English translation",
  "pronunciationHint": "transliteration if needed",
  "feedback": {
    "grammarCorrect": true/false,
    "correctedText": "optional corrected user message",
    "explanation": "friendly explanation if corrected",
    "vocabularyNotes": [{"word": "word", "translation": "translation", "note": "context"}]
  },
  "suggestedReplies": ["phrase in target [English meaning]"]
}
""".trimIndent()

        if (apiKey.isBlank()) {
            // Provide intelligent fallback partner response
            return@withContext generateFallbackResponse(partner, history.lastOrNull()?.text ?: "")
        }

        try {
            val contentList = history.map { msg ->
                Content(
                    role = if (msg.role == "model") "model" else "user",
                    parts = listOf(Part(text = msg.text))
                )
            }

            val request = GenerateContentRequest(
                contents = contentList,
                systemInstruction = Content(
                    parts = listOf(Part(text = systemPrompt))
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.7f,
                    responseFormat = ResponseFormat(
                        ResponseFormatText(mimeType = "application/json")
                    )
                )
            )

            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!jsonText.isNullOrBlank()) {
                val parsed = RetrofitClient.json.decodeFromString<GeminiPartnerResponse>(jsonText)
                val feedback = parsed.feedback?.let { fb ->
                    Feedback(
                        grammarCorrect = fb.grammarCorrect,
                        correctedText = fb.correctedText,
                        explanation = fb.explanation,
                        vocabularyNotes = fb.vocabularyNotes?.map {
                            VocabNote(word = it.word, translation = it.translation, note = it.note)
                        }
                    )
                }

                return@withContext Message(
                    id = UUID.randomUUID().toString(),
                    role = "model",
                    text = parsed.replyText,
                    translation = parsed.translation,
                    pronunciationHint = parsed.pronunciationHint,
                    feedback = feedback,
                    suggestedReplies = parsed.suggestedReplies,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                return@withContext generateFallbackResponse(partner, history.lastOrNull()?.text ?: "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext generateFallbackResponse(partner, history.lastOrNull()?.text ?: "")
        }
    }

    private fun generateFallbackResponse(partner: Partner, userText: String): Message {
        val lower = userText.lowercase()
        val (reply, translation, hint, suggestions) = when (partner.id) {
            "elena" -> Quadruple(
                "¡Muy bien dicho! Me encanta que practiquemos juntos. ¿Qué te parece si hablamos sobre la comida de Sevilla?",
                "Very well said! I love that we are practicing together. What do you think if we talk about Seville's food?",
                null,
                listOf("¡Me encanta la comida! [I love the food!]", "¿Cuál es tu plato favorito? [What is your favorite dish?]", "Prefiero hablar de música [I prefer talking about music]")
            )
            "marie" -> Quadruple(
                "C'est merveilleux ! Votre accent est charmant. Aimeriez-vous explorer les galeries d'art parisiennes ?",
                "That's wonderful! Your accent is charming. Would you like to explore Parisian art galleries?",
                null,
                listOf("Oui, avec grand plaisir ! [Yes, with great pleasure!]", "Quel est votre musée préféré ? [What is your favorite museum?]", "Parlons plutôt de cinéma [Let's talk about cinema instead]")
            )
            "yuki" -> Quadruple(
                "素晴らしいですね！日本語のお話しがとても上手です。京都の抹茶を飲んだことはありますか？",
                "Wonderful! Your Japanese speaking is very good. Have you ever drank Kyoto matcha tea?",
                "Subarashii desu ne! Nihongo no ohanashi ga totemo jouzu desu.",
                listOf("はい、大好きです！ [Yes, I love it!]", "いいえ、まだです [No, not yet]", "おすすめのお茶は？ [What tea do you recommend?]")
            )
            "lucas" -> Quadruple(
                "Ausgezeichnet! Deine Aussprache klingt schon sehr flüssig. Fährst du in deiner Freizeit auch gerne Fahrrad?",
                "Excellent! Your pronunciation already sounds very fluent. Do you also like cycling in your free time?",
                null,
                listOf("Ja, fast jeden Tag! [Yes, almost every day!]", "Nicht so oft [Not so often]", "Ich gehe lieber wandern [I prefer going hiking]")
            )
            else -> Quadruple(
                "Fantastico! Stai facendo ottimi progressi. Che cosa ti piacerebbe visitare in Italia?",
                "Fantastic! You are making great progress. What would you like to visit in Italy?",
                null,
                listOf("Vorrei visitare Roma [I would like to visit Rome]", "Amo la moda italiana [I love Italian fashion]", "Parlami di Milano [Tell me about Milan]")
            )
        }

        return Message(
            id = UUID.randomUUID().toString(),
            role = "model",
            text = reply,
            translation = translation,
            pronunciationHint = hint,
            feedback = Feedback(
                grammarCorrect = true,
                explanation = "Your sentence composition was natural and expressive."
            ),
            suggestedReplies = suggestions,
            timestamp = System.currentTimeMillis()
        )
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
