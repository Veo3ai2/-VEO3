package com.example.languagepartner.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.languagepartner.data.model.Message
import com.example.languagepartner.data.model.Partner
import com.example.languagepartner.ui.theme.AccentAmber
import com.example.languagepartner.ui.theme.AccentAmberDark
import com.example.languagepartner.ui.theme.BorderSubtle
import com.example.languagepartner.ui.theme.BrandBlack
import com.example.languagepartner.ui.theme.BrandLime
import com.example.languagepartner.ui.theme.SurfaceCard
import com.example.languagepartner.ui.theme.SurfaceDark
import com.example.languagepartner.ui.theme.SurfaceElevated
import com.example.languagepartner.ui.theme.TextPrimary
import com.example.languagepartner.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    partner: Partner,
    messages: List<Message>,
    isGenerating: Boolean,
    userLevel: String,
    autoSpeak: Boolean,
    playingMessageId: String?,
    onSendMessage: (String) -> Unit,
    onSpeakText: (String, String, String) -> Unit,
    onStopSpeech: () -> Unit,
    onToggleAutoSpeak: () -> Unit,
    onSetUserLevel: (String) -> Unit,
    onAddVocab: (String, String, String, String?) -> Unit,
    onClearChat: () -> Unit,
    onExportChat: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    var levelMenuExpanded by remember { mutableStateOf(false) }

    // Word tap dialog state
    var selectedWordForVocab by remember { mutableStateOf<Pair<String, String>?>(null) }
    var customTranslation by remember { mutableStateOf("") }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                inputText = spoken
            }
        }
    }

    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandBlack)
            .imePadding()
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag avatar
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0x20FFFFFF))
                    .border(1.dp, BorderSubtle),
                contentAlignment = Alignment.Center
            ) {
                Text(text = partner.flag, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = partner.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(BrandLime)
                    )
                }
                Text(
                    text = "${partner.languageName.uppercase()} • ${partner.role.take(24)}…",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }

            // Auto-speak toggle button
            Box(
                modifier = Modifier
                    .clickable { onToggleAutoSpeak() }
                    .background(if (autoSpeak) BrandLime.copy(alpha = 0.2f) else Color.Transparent)
                    .border(1.dp, if (autoSpeak) BrandLime else BorderSubtle)
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "النطق التلقائي",
                        tint = if (autoSpeak) BrandLime else TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "الصوت",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (autoSpeak) BrandLime else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // User Level Selector
            Box {
                val levelDisplay = when (userLevel) {
                    "Beginner" -> "مبتدئ"
                    "Intermediate" -> "متوسط"
                    "Advanced" -> "متقدم"
                    else -> userLevel
                }
                Box(
                    modifier = Modifier
                        .clickable { levelMenuExpanded = true }
                        .background(Color(0x15FFFFFF))
                        .border(1.dp, BorderSubtle)
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = levelDisplay,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                DropdownMenu(
                    expanded = levelMenuExpanded,
                    onDismissRequest = { levelMenuExpanded = false },
                    modifier = Modifier.background(SurfaceDark)
                ) {
                    listOf("Beginner" to "مبتدئ", "Intermediate" to "متوسط", "Advanced" to "متقدم").forEach { (lvl, title) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = title,
                                    color = if (lvl == userLevel) BrandLime else TextPrimary,
                                    fontSize = 12.sp
                                )
                            },
                            onClick = {
                                onSetUserLevel(lvl)
                                levelMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onExportChat,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "تصدير المحادثة كملف نصي",
                    tint = BrandLime,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onClearChat,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "مسح المحادثة",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Welcome Banner Card
                Card(
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "بيئة محادثة مع ناطق أصلي",
                                color = BrandLime,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = partner.welcomeMessage,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0x22D4FF00))
                                .border(1.dp, BrandLime.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "💡 انقر على أي كلمة أثناء المحادثة لحفظها في المفردات",
                                color = BrandLime,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    partner = partner,
                    isPlayingAudio = playingMessageId == message.id,
                    onSpeak = { onSpeakText(message.id, message.text, partner.language) },
                    onStopAudio = onStopSpeech,
                    onWordTap = { word, sentence ->
                        selectedWordForVocab = Pair(word, sentence)
                        customTranslation = ""
                    }
                )
            }

            if (isGenerating) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(SurfaceDark)
                            .border(1.dp, BorderSubtle)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = BrandLime,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${partner.name} يكتب رداً باللغة ${partner.languageName}…",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Suggestions Carousel
        val latestModel = messages.lastOrNull { it.role == "model" }
        val suggestions = latestModel?.suggestedReplies ?: emptyList()
        if (suggestions.isNotEmpty() && !isGenerating) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestions) { suggestion ->
                    Box(
                        modifier = Modifier
                            .clickable {
                                // Extract foreign text part before [Arabic translation]
                                val cleanText = suggestion.substringBefore("[").trim()
                                onSendMessage(cleanText)
                            }
                            .background(SurfaceElevated)
                            .border(1.dp, BrandLime.copy(alpha = 0.4f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = suggestion,
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .border(BorderStroke(1.dp, BorderSubtle))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Microphone Button
            IconButton(
                onClick = {
                    try {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, partner.language)
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث باللغة ${partner.languageName}")
                        }
                        speechLauncher.launch(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0x15FFFFFF))
                    .border(1.dp, BorderSubtle)
                    .testTag("speech_mic_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "تحدث",
                    tint = BrandLime,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text Input
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = "اكتب باللغة ${partner.languageName}…",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandLime,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = BrandBlack,
                    unfocusedContainerColor = BrandBlack
                ),
                shape = RoundedCornerShape(0.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputText.isNotBlank() && !isGenerating) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                }),
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(if (inputText.isNotBlank() && !isGenerating) BrandLime else Color(0x20FFFFFF))
                    .clickable(enabled = inputText.isNotBlank() && !isGenerating) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                    .testTag("send_message_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "إرسال",
                    tint = if (inputText.isNotBlank() && !isGenerating) Color.Black else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    // Word Tap Lexicon Dialog
    selectedWordForVocab?.let { (word, contextSentence) ->
        AlertDialog(
            onDismissRequest = { selectedWordForVocab = null },
            title = {
                Text(
                    text = "حفظ الكلمة في المفردات",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandLime
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = word,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "السياق: $contextSentence",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                    OutlinedTextField(
                        value = customTranslation,
                        onValueChange = { customTranslation = it },
                        label = { Text("الترجمة بالعربية") },
                        placeholder = { Text("مثال: استكشاف / لذيذ") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandLime,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trans = customTranslation.ifBlank { "تمت الإضافة من المحادثة" }
                        onAddVocab(word, trans, contextSentence, null)
                        selectedWordForVocab = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandLime, contentColor = Color.Black),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text("حفظ الكلمة", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedWordForVocab = null }) {
                    Text("إلغاء", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(0.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MessageBubble(
    message: Message,
    partner: Partner,
    isPlayingAudio: Boolean,
    onSpeak: () -> Unit,
    onStopAudio: () -> Unit,
    onWordTap: (word: String, sentence: String) -> Unit
) {
    val isUser = message.role == "user"
    var showTranslation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            // Model message header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(text = partner.flag, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = partner.name.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = BrandLime
                )
            }
        }

        // Main Bubble
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.85f else 0.95f)
                .background(if (isUser) Color(0xFF1E2430) else SurfaceCard)
                .border(1.dp, if (isUser) Color(0x334B5563) else BorderSubtle)
                .padding(12.dp)
        ) {
            Column {
                if (isUser) {
                    Text(
                        text = message.text,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                } else {
                    // Clickable word breakdown for target language
                    val words = message.text.split(" ")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        words.forEach { word ->
                            val cleanWord = word.trim('.', ',', '!', '?', '¿', '¡', '"', ':', ';')
                            Text(
                                text = word,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                modifier = Modifier
                                    .clickable {
                                        if (cleanWord.isNotBlank()) {
                                            onWordTap(cleanWord, message.text)
                                        }
                                    }
                            )
                        }
                    }

                    // Pronunciation hint (Romaji etc.)
                    if (!message.pronunciationHint.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🗣 ${message.pronunciationHint}",
                            color = BrandLime.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Translation section
                    if (!message.translation.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        AnimatedVisibility(visible = showTranslation) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceDark)
                                    .border(1.dp, BorderSubtle)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = message.translation,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Actions row (Translate toggle + Speaker)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!message.translation.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { showTranslation = !showTranslation }
                                    .padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showTranslation) "إخفاء الترجمة" else "عرض الترجمة",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        // Audio button
                        IconButton(
                            onClick = {
                                if (isPlayingAudio) onStopAudio() else onSpeak()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "تشغيل الصوت",
                                tint = if (isPlayingAudio) BrandLime else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Corrective Feedback Card on user message
        if (isUser && message.feedback != null) {
            val fb = message.feedback
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (!fb.grammarCorrect) AccentAmberDark else SurfaceDark
                ),
                border = BorderStroke(
                    1.dp,
                    if (!fb.grammarCorrect) AccentAmber else BrandLime.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .padding(top = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (!fb.grammarCorrect) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (!fb.grammarCorrect) AccentAmber else BrandLime,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (!fb.grammarCorrect) "ملاحظات نحوية وتصحيح" else "قواعد سليمة ودقيقة",
                            color = if (!fb.grammarCorrect) AccentAmber else BrandLime,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!fb.grammarCorrect && !fb.correctedText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "النص المدخل: ${message.text}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "التصحيح المقترح: ${fb.correctedText}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    if (!fb.explanation.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = fb.explanation,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = if (!fb.grammarCorrect) Color(0xFFFDE68A) else TextSecondary
                        )
                    }
                }
            }
        }
    }
}
