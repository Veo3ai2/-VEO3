package com.example.languagepartner.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.languagepartner.data.model.VocabWord
import com.example.languagepartner.ui.theme.BorderSubtle
import com.example.languagepartner.ui.theme.BrandBlack
import com.example.languagepartner.ui.theme.BrandLime
import com.example.languagepartner.ui.theme.SurfaceCard
import com.example.languagepartner.ui.theme.SurfaceDark
import com.example.languagepartner.ui.theme.SurfaceElevated
import com.example.languagepartner.ui.theme.TextPrimary
import com.example.languagepartner.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VocabBuilderScreen(
    vocabulary: List<VocabWord>,
    onAddWord: (word: String, translation: String, languageName: String, context: String, notes: String?) -> Unit,
    onDeleteWord: (id: String) -> Unit,
    onSpeakWord: (word: String, languageName: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedLanguageFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val languageFilters = listOf(
        "All" to "الكل",
        "Spanish" to "الإسبانية",
        "French" to "الفرنسية",
        "Japanese" to "اليابانية",
        "German" to "الألمانية",
        "Italian" to "الإيطالية"
    )
    val filteredList = if (selectedLanguageFilter == "All") {
        vocabulary
    } else {
        vocabulary.filter { it.languageName.equals(selectedLanguageFilter, ignoreCase = true) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrandBlack)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(BrandLime)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "المفردات",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "بطاقات الاستذكار",
                                color = BrandLime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "المفردات المحفوظة",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0x22D4FF00))
                            .border(1.dp, BrandLime.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${vocabulary.size} كلمات",
                            color = BrandLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                // Language filter chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    languageFilters.forEach { (langKey, langLabel) ->
                        val isSelected = langKey == selectedLanguageFilter
                        Box(
                            modifier = Modifier
                                .clickable { selectedLanguageFilter = langKey }
                                .background(if (isSelected) BrandLime else SurfaceElevated)
                                .border(1.dp, if (isSelected) BrandLime else BorderSubtle)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = langLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else TextSecondary
                            )
                        }
                    }
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .border(1.dp, BorderSubtle)
                            .background(SurfaceCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "سجل المفردات فارغ",
                                color = BrandLime,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "لم يتم حفظ أي مفردات بعد. انقر على أي كلمة أثناء المحادثة أو اضغط على زر + لإضافتها يدوياً.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { vocabWord ->
                    VocabFlashcard(
                        vocabWord = vocabWord,
                        onDelete = { onDeleteWord(vocabWord.id) },
                        onSpeak = { onSpeakWord(vocabWord.word, vocabWord.languageName) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button to manually add vocabulary
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = BrandLime,
            contentColor = Color.Black,
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_vocab_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة مفردة")
        }
    }

    if (showAddDialog) {
        var wordInput by remember { mutableStateOf("") }
        var translationInput by remember { mutableStateOf("") }
        var languageInput by remember { mutableStateOf(if (selectedLanguageFilter != "All") selectedLanguageFilter else "الإسبانية") }
        var contextInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "إضافة مفردة يدوياً",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandLime
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = wordInput,
                        onValueChange = { wordInput = it },
                        label = { Text("الكلمة / العبارة") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandLime,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = translationInput,
                        onValueChange = { translationInput = it },
                        label = { Text("المعنى بالعربية") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandLime,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = languageInput,
                        onValueChange = { languageInput = it },
                        label = { Text("اللغة (مثال: الإسبانية، الفرنسية)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandLime,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contextInput,
                        onValueChange = { contextInput = it },
                        label = { Text("جملة توضيحية في السياق") },
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
                        if (wordInput.isNotBlank() && translationInput.isNotBlank()) {
                            onAddWord(
                                wordInput.trim(),
                                translationInput.trim(),
                                languageInput.trim(),
                                contextInput.trim(),
                                null
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandLime, contentColor = Color.Black),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text("إضافة", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(0.dp)
        )
    }
}

@Composable
fun VocabFlashcard(
    vocabWord: VocabWord,
    onDelete: () -> Unit,
    onSpeak: () -> Unit = {}
) {
    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "cardFlip"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vocab_card_${vocabWord.id}")
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { isFlipped = !isFlipped },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rotation > 90f) BrandLime else SurfaceCard
        ),
        border = BorderStroke(1.dp, if (rotation > 90f) BrandLime else BorderSubtle)
    ) {
        if (rotation > 90f) {
            // BACK of card (Revealed translation)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { rotationY = 180f }
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المعنى باللغة العربية",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        imageVector = Icons.Default.Flip,
                        contentDescription = "قلب البطاقة",
                        tint = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = vocabWord.translation,
                    color = Color.Black,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )

                if (!vocabWord.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = vocabWord.notes,
                        color = Color.Black.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "انقر للعودة إلى الوجه الأول",
                    color = Color.Black.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // FRONT of card (Foreign word)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x22D4FF00))
                            .border(1.dp, BrandLime.copy(alpha = 0.4f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = vocabWord.languageName.uppercase(),
                            color = BrandLime,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "استماع لنطق الكلمة",
                                tint = BrandLime,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف الكلمة",
                                tint = TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = vocabWord.word,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )

                if (vocabWord.contextSentence.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "السياق: \"${vocabWord.contextSentence}\"",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Flip,
                        contentDescription = null,
                        tint = BrandLime,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "انقر لعرض الترجمة والمعنى",
                        color = BrandLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
