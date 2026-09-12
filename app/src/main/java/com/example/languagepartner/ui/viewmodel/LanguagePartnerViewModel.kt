package com.example.languagepartner.ui.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepartner.data.local.AppDatabase
import com.example.languagepartner.data.model.Feedback
import com.example.languagepartner.data.model.Message
import com.example.languagepartner.data.model.Partner
import com.example.languagepartner.data.model.PartnerCatalog
import com.example.languagepartner.data.model.UserProgress
import com.example.languagepartner.data.model.VocabWord
import com.example.languagepartner.data.repository.LanguagePartnerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.languagepartner.data.firebase.FirestoreLanguagePartnerRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class LanguagePartnerViewModel(
    application: Application,
    val userId: String
) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val firestoreRepo = FirestoreLanguagePartnerRepository(application)
    private val repository: LanguagePartnerRepository
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _selectedPartner = MutableStateFlow<Partner?>(PartnerCatalog.PARTNERS.firstOrNull())
    val selectedPartner: StateFlow<Partner?> = _selectedPartner.asStateFlow()

    private val _activeTab = MutableStateFlow("chat")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    private val _userLevel = MutableStateFlow("Beginner")
    val userLevel: StateFlow<String> = _userLevel.asStateFlow()

    private val _autoPlayAudio = MutableStateFlow(false)
    val autoPlayAudio: StateFlow<Boolean> = _autoPlayAudio.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _currentlyPlayingMessageId = MutableStateFlow<String?>(null)
    val currentlyPlayingMessageId: StateFlow<String?> = _currentlyPlayingMessageId.asStateFlow()

    data class CelebrationEvent(
        val title: String,
        val subtitle: String
    )
    private val _celebrationEvent = MutableStateFlow<CelebrationEvent?>(null)
    val celebrationEvent: StateFlow<CelebrationEvent?> = _celebrationEvent.asStateFlow()

    fun dismissCelebration() {
        _celebrationEvent.value = null
    }

    val vocabulary: StateFlow<List<VocabWord>>

    val userProgress: StateFlow<UserProgress>

    val currentMessages: StateFlow<List<Message>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = LanguagePartnerRepository(db, firestoreRepo, userId)

        vocabulary = repository.allVocabWords.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        userProgress = repository.userProgress.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProgress()
        )

        currentMessages = _selectedPartner.flatMapLatest { partner ->
            if (partner != null) {
                repository.getMessagesForPartner(partner.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Observe Firestore user progress to restore cloud backup
        viewModelScope.launch {
            try {
                repository.observeFirestoreProgress().collect { remoteProgress ->
                    val local = repository.userProgress.firstOrNull() ?: UserProgress()
                    if (remoteProgress.totalMessagesSent > local.totalMessagesSent) {
                        repository.saveProgress(remoteProgress)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewModelScope.launch {
            try {
                val todayMsgs = repository.countTodayUserMessages()
                val todayVocab = repository.countTodayVocabWords()
                val current = repository.userProgress.firstOrNull() ?: UserProgress()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val today = dateFormat.format(Date())
                if ((todayMsgs > 0 && current.todayMessagesSent != todayMsgs) ||
                    (todayVocab > 0 && current.todayVocabAdded != todayVocab)
                ) {
                    repository.saveProgress(
                        current.copy(
                            todayMessagesSent = if (todayMsgs > 0) todayMsgs else current.todayMessagesSent,
                            todayVocabAdded = if (todayVocab > 0) todayVocab else current.todayVocabAdded,
                            lastActiveDate = today
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        try {
            tts = TextToSpeech(application, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
        }
    }

    fun selectPartner(partner: Partner) {
        _selectedPartner.value = partner
        _activeTab.value = "chat"
    }

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
    }

    fun setUserLevel(level: String) {
        _userLevel.value = level
    }

    fun toggleAutoPlay() {
        _autoPlayAudio.value = !_autoPlayAudio.value
    }

    fun speakText(messageId: String, text: String, languageCode: String) {
        if (!isTtsReady || tts == null) return
        _currentlyPlayingMessageId.value = messageId
        try {
            val locale = Locale.forLanguageTag(languageCode)
            tts?.language = locale
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, messageId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun speakWord(word: String, languageName: String) {
        if (!isTtsReady || tts == null) return
        val langCode = when (languageName.lowercase()) {
            "spanish", "إسبانية", "الإسبانية" -> "es"
            "french", "فرنسية", "الفرنسية" -> "fr"
            "japanese", "يابانية", "اليابانية" -> "ja"
            "german", "ألمانية", "الألمانية" -> "de"
            "italian", "إيطالية", "الإيطالية" -> "it"
            else -> "en"
        }
        speakText("vocab_${UUID.randomUUID()}", word, langCode)
    }

    fun stopSpeech() {
        tts?.stop()
        _currentlyPlayingMessageId.value = null
    }

    fun sendMessage(text: String) {
        val partner = _selectedPartner.value ?: return
        val trimmed = text.trim()
        if (trimmed.isBlank() || _isGenerating.value) return

        stopSpeech()

        val userMessage = Message(
            id = UUID.randomUUID().toString(),
            role = "user",
            text = trimmed,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.saveMessage(partner.id, userMessage)
            _isGenerating.value = true

            try {
                val currentList = currentMessages.value + userMessage
                val responseMsg = repository.sendChatMessage(
                    partner = partner,
                    history = currentList,
                    userLevel = _userLevel.value
                )

                // Update user message with feedback if grammar correction exists
                if (responseMsg.feedback != null) {
                    val updatedUserMsg = userMessage.copy(feedback = responseMsg.feedback)
                    repository.saveMessage(partner.id, updatedUserMsg)
                }

                repository.saveMessage(partner.id, responseMsg)

                // Update progress metrics
                val prev = userProgress.value
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val today = dateFormat.format(Date())
                var streak = prev.streak
                var todaySent = if (prev.lastActiveDate == today) prev.todayMessagesSent else 0

                if (prev.lastActiveDate != null && prev.lastActiveDate != today) {
                    // check if yesterday
                    streak += 1
                    todaySent = 0
                }
                todaySent += 1

                val isCorrect = responseMsg.feedback?.grammarCorrect != false
                val totalSent = prev.totalMessagesSent + 1
                val score = if (prev.totalMessagesSent == 0) {
                    if (isCorrect) 100 else 75
                } else {
                    val currentCorrectRatio = if (isCorrect) 1.0 else 0.0
                    val nextScore = ((prev.grammarAccuracyScore * prev.totalMessagesSent) + (if (isCorrect) 100 else 0)) / totalSent
                    nextScore.coerceIn(0, 100)
                }

                val newProgress = UserProgress(
                    streak = streak,
                    totalMessagesSent = totalSent,
                    grammarAccuracyScore = score,
                    lastActiveDate = today,
                    dailyMessageGoal = prev.dailyMessageGoal,
                    todayMessagesSent = todaySent,
                    dailyVocabGoal = prev.dailyVocabGoal,
                    todayVocabAdded = if (prev.lastActiveDate == today) prev.todayVocabAdded else 0
                )
                repository.saveProgress(newProgress)

                // Check for streak or message goal celebration
                if (todaySent == prev.dailyMessageGoal && prev.dailyMessageGoal > 0) {
                    _celebrationEvent.value = CelebrationEvent(
                        title = "🎯 تم تحقيق هدف المحادثة اليومي!",
                        subtitle = "أكملت $todaySent رسائل محادثة اليوم بنجاح! أحسنت ومبارك إنجازك."
                    )
                } else if (streak > prev.streak) {
                    _celebrationEvent.value = CelebrationEvent(
                        title = "🔥 سلسلة أيام جديدة: $streak أيام!",
                        subtitle = "استمرارك اليومي يعزز طلاقتك اللغوية وسرعة اكتسابك."
                    )
                }

                // Check auto-speak
                if (_autoPlayAudio.value) {
                    speakText(responseMsg.id, responseMsg.text, partner.language)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun setDailyMessageGoal(goal: Int) {
        val validGoal = goal.coerceIn(1, 100)
        viewModelScope.launch {
            val current = userProgress.value
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val today = dateFormat.format(Date())
            val todaySent = if (current.lastActiveDate == today) {
                current.todayMessagesSent
            } else {
                repository.countTodayUserMessages()
            }
            repository.saveProgress(
                current.copy(
                    dailyMessageGoal = validGoal,
                    todayMessagesSent = todaySent,
                    lastActiveDate = current.lastActiveDate ?: today
                )
            )
        }
    }

    fun setDailyVocabGoal(goal: Int) {
        val validGoal = goal.coerceIn(1, 100)
        viewModelScope.launch {
            val current = userProgress.value
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val today = dateFormat.format(Date())
            val todayVocab = if (current.lastActiveDate == today) {
                current.todayVocabAdded
            } else {
                repository.countTodayVocabWords()
            }
            repository.saveProgress(
                current.copy(
                    dailyVocabGoal = validGoal,
                    todayVocabAdded = todayVocab,
                    lastActiveDate = current.lastActiveDate ?: today
                )
            )
        }
    }

    fun addVocabulary(word: String, translation: String, context: String, notes: String? = null) {
        val partner = _selectedPartner.value ?: return
        viewModelScope.launch {
            repository.addVocabWord(
                word = word,
                translation = translation,
                languageName = partner.languageName,
                context = context,
                notes = notes
            )
            val current = userProgress.value
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val today = dateFormat.format(Date())
            val newVocab = if (current.lastActiveDate == today) current.todayVocabAdded + 1 else 1
            val todaySent = if (current.lastActiveDate == today) current.todayMessagesSent else 0
            val updatedProgress = current.copy(
                todayVocabAdded = newVocab,
                todayMessagesSent = todaySent,
                lastActiveDate = today
            )
            repository.saveProgress(updatedProgress)

            // Trigger confetti if daily vocab goal is hit
            if (newVocab == current.dailyVocabGoal && current.dailyVocabGoal > 0) {
                _celebrationEvent.value = CelebrationEvent(
                    title = "🎉 تم تحقيق هدف المفردات اليومي!",
                    subtitle = "أضفت $newVocab مفردات جديدة اليوم إلى معجمك اللغوي! رائع جداً."
                )
            }
        }
    }

    fun removeVocabulary(id: String) {
        viewModelScope.launch {
            repository.removeVocabWord(id)
        }
    }

    fun clearChat() {
        val partner = _selectedPartner.value ?: return
        viewModelScope.launch {
            repository.clearHistoryForPartner(partner.id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class LanguagePartnerViewModelFactory(
    private val application: Application,
    private val userId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LanguagePartnerViewModel::class.java)) {
            return LanguagePartnerViewModel(application, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

