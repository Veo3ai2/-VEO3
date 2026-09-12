package com.example.languagepartner.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val role: String? = null,
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class ResponseFormat(
    val text: ResponseFormatText? = null
)

@Serializable
data class ResponseFormatText(
    val mimeType: String,
    val schema: JsonObject? = null
)

@Serializable
data class GenerationConfig(
    val responseFormat: ResponseFormat? = null,
    val temperature: Float? = null,
    val responseModalities: List<String>? = null,
    val speechConfig: SpeechConfig? = null
)

@Serializable
data class SpeechConfig(
    val voiceConfig: VoiceConfig
)

@Serializable
data class VoiceConfig(
    val prebuiltVoiceConfig: PrebuiltVoiceConfig
)

@Serializable
data class PrebuiltVoiceConfig(
    val voiceName: String
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@Serializable
data class Candidate(
    val content: Content? = null
)

@Serializable
data class GeminiPartnerResponse(
    val replyText: String = "",
    val translation: String = "",
    val pronunciationHint: String? = null,
    val feedback: GeminiFeedbackResponse? = null,
    val suggestedReplies: List<String> = emptyList()
)

@Serializable
data class GeminiFeedbackResponse(
    val grammarCorrect: Boolean = true,
    val correctedText: String? = null,
    val explanation: String? = null,
    val vocabularyNotes: List<GeminiVocabNoteResponse>? = null
)

@Serializable
data class GeminiVocabNoteResponse(
    val word: String = "",
    val translation: String = "",
    val note: String? = null
)
