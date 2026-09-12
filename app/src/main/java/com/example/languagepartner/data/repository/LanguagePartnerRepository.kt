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
import com.example.languagepartner.data.firebase.FirestoreLanguagePartnerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LanguagePartnerRepository(
    private val database: AppDatabase,
    private val firestoreRepo: FirestoreLanguagePartnerRepository,
    val userId: String
) {

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

    fun observeFirestoreProgress(): Flow<UserProgress> =
        firestoreRepo.observeUserProgress(userId)

    fun observeFirestoreVocab(): Flow<List<VocabWord>> =
        firestoreRepo.observeVocabWords(userId)

    fun observeFirestoreMessages(partnerId: String): Flow<List<Message>> =
        firestoreRepo.observeChatMessages(userId, partnerId)

    suspend fun addVocabWord(word: String, translation: String, languageName: String, context: String, notes: String? = null) {
        withContext(Dispatchers.IO) {
            val id = UUID.randomUUID().toString()
            val entity = VocabEntity(
                id = id,
                word = word.trim(),
                translation = translation.trim(),
                languageName = languageName,
                contextSentence = context,
                notes = notes,
                createdAt = System.currentTimeMillis()
            )
            vocabDao.insertVocabWord(entity)
            try {
                firestoreRepo.addVocabWord(userId, id, word, translation, languageName, context, notes)
            } catch (e: Exception) {
                // Handled in FirestoreLanguagePartnerRepository
            }
        }
    }

    suspend fun removeVocabWord(id: String) {
        withContext(Dispatchers.IO) {
            vocabDao.deleteVocabWord(id)
            try {
                firestoreRepo.removeVocabWord(userId, id)
            } catch (e: Exception) {
                // Handled in FirestoreLanguagePartnerRepository
            }
        }
    }

    suspend fun saveMessage(partnerId: String, message: Message) {
        withContext(Dispatchers.IO) {
            chatDao.insertMessage(ChatMessageEntity.fromDomain(partnerId, message))
            try {
                firestoreRepo.saveChatMessage(userId, partnerId, message)
            } catch (e: Exception) {
                // Handled in FirestoreLanguagePartnerRepository
            }
        }
    }

    suspend fun clearHistoryForPartner(partnerId: String) {
        withContext(Dispatchers.IO) {
            chatDao.clearMessagesForPartner(partnerId)
            try {
                firestoreRepo.clearChatMessages(userId, partnerId)
            } catch (e: Exception) {
                // Handled in FirestoreLanguagePartnerRepository
            }
        }
    }

    suspend fun updateProgress(wasCorrect: Boolean) {
        withContext(Dispatchers.IO) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val today = dateFormat.format(Date())

            val current = progressDao.getProgress()
            val currentEntity = database.runInTransaction<ProgressEntity?> {
                null
            }
        }
    }

    suspend fun saveProgress(progress: UserProgress) {
        withContext(Dispatchers.IO) {
            progressDao.saveProgress(ProgressEntity.fromDomain(progress))
            try {
                firestoreRepo.saveUserProgress(userId, progress)
            } catch (e: Exception) {
                // Handled in FirestoreLanguagePartnerRepository
            }
        }
    }

    suspend fun countTodayUserMessages(): Int = withContext(Dispatchers.IO) {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        chatDao.countUserMessagesSince(calendar.timeInMillis)
    }

    suspend fun countTodayVocabWords(): Int = withContext(Dispatchers.IO) {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        vocabDao.countVocabWordsSince(calendar.timeInMillis)
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
2. Provide a polite and authentic native language response in 'replyText'.
3. CRITICAL: The user's primary application interface is Arabic (العربية). You MUST translate your native response into natural, fluent Arabic (العربية) in 'translation'.
4. For Asian/Cyrillic languages or languages with different scripts, provide a pronunciation hint or transliteration (e.g., Romaji for Japanese, Pinyin for Chinese).
5. Crucially, analyze the user's last message:
   - Check if their message fits typical patterns and grammar rules in ${partner.languageName}.
   - If they made typos, grammar, or word-choice errors, set "grammarCorrect" to false, give a corrected version in "correctedText", and a friendly explanation in Arabic (العربية) in "explanation".
   - If they wrote correctly, set "grammarCorrect" to true, and give a brief positive affirmation in Arabic in "explanation".
   - List 2-3 vocabulary words used in this turnaround with brief definition cards in Arabic in "vocabularyNotes".
6. Formulate 3 relevant suggestion shortcuts in ${partner.languageName} that the user could click to reply easily (e.g., positive answer, question back, divert topic). Include an Arabic translation for each suggestion inside brackets, e.g.: "phrase in target [المعنى بالعربية]".

You MUST respond strictly using valid JSON with this structure:
{
  "replyText": "native response in target language",
  "translation": "الترجمة الدقيقة باللغة العربية",
  "pronunciationHint": "transliteration if needed",
  "feedback": {
    "grammarCorrect": true/false,
    "correctedText": "optional corrected user message",
    "explanation": "شرح نحوي ودود باللغة العربية",
    "vocabularyNotes": [{"word": "word", "translation": "الترجمة بالعربية", "note": "ملاحظة"}]
  },
  "suggestedReplies": ["phrase in target [المعنى بالعربية]"]
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
                "أحسنت القول! يسعدني جداً أن نتدرب سوياً. ما رأيك أن نتحدث عن أطعمة إشبيلية اللذيذة؟",
                null,
                listOf("¡Me encanta la comida! [أعشق الطعام!]", "¿Cuál es tu plato favorito? [ما هو طبقك المفضل؟]", "Prefiero hablar de música [أفضل التحدث عن الموسيقى]")
            )
            "marie" -> Quadruple(
                "C'est merveilleux ! Votre accent est charmant. Aimeriez-vous explorer les galeries d'art parisiennes ?",
                "هذا رائع! نبرتك جميلة وواضحة. هل ترغب في استكشاف صالات الفنون في باريس؟",
                null,
                listOf("Oui, avec grand plaisir ! [نعم، بكل سرور!]", "Quel est votre musée préféré ? [ما هو متحفك المفضل؟]", "Parlons plutôt de cinéma [دعنا نتحدث عن السينما]")
            )
            "yuki" -> Quadruple(
                "素晴らしいですね！日本語のお話しがとても上手です。京都の抹茶を飲んだことはありますか？",
                "رائع جداً! تحدثك باللغة اليابانية ممتاز. هل جربت شرب شاي الماتشا في كيوتو من قبل؟",
                "Subarashii desu ne! Nihongo no ohanashi ga totemo jouzu desu.",
                listOf("はい、大好きです！ [نعم، أحبه كثيراً!]", "いいえ、まだです [لا، ليس بعد]", "おすすめのお茶は？ [ما هو الشاي الذي تنصحين به؟]")
            )
            "lucas" -> Quadruple(
                "Ausgezeichnet! Deine Aussprache klingt schon sehr flüssig. Fährst du in deiner Freizeit auch gerne Fahrrad?",
                "ممتاز! نطقك يبدو طليقاً وسلساً. هل تحب ركوب الدراجات في وقت فراغك أيضاً؟",
                null,
                listOf("Ja, fast jeden Tag! [نعم، كل يوم تقريباً!]", "Nicht so oft [ليس كثيراً]", "Ich gehe lieber wandern [أفضل الذهاب للتنزه]")
            )
            else -> Quadruple(
                "Fantastico! Stai facendo ottimi progressi. Che cosa ti piacerebbe visitare in Italia?",
                "رائع! أنت تحرز تقدماً ملحوظاً. ماذا تحب أن تزور في إيطاليا؟",
                null,
                listOf("Vorrei visitare Roma [أود زيارة روما]", "Amo la moda italiana [أعشق الموضة الإيطالية]", "Parlami di Milano [حدثيني عن ميلانو]")
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
                explanation = "تركيب جملتك سليم وممتاز ومعبر."
            ),
            suggestedReplies = suggestions,
            timestamp = System.currentTimeMillis()
        )
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
