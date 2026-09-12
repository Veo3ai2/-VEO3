package com.example.languagepartner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.languagepartner.data.model.PartnerCatalog
import com.example.languagepartner.ui.screens.ChatScreen
import com.example.languagepartner.ui.screens.PartnerSelectorScreen
import com.example.languagepartner.ui.screens.ProgressStatsScreen
import com.example.languagepartner.ui.screens.VocabBuilderScreen
import com.example.languagepartner.ui.theme.BorderSubtle
import com.example.languagepartner.ui.theme.BrandBlack
import com.example.languagepartner.ui.theme.BrandLime
import com.example.languagepartner.ui.theme.LanguagePartnerTheme
import com.example.languagepartner.ui.theme.SurfaceDark
import com.example.languagepartner.ui.theme.TextPrimary
import com.example.languagepartner.ui.theme.TextSecondary
import com.example.languagepartner.ui.viewmodel.LanguagePartnerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LanguagePartnerTheme {
                LanguagePartnerMainApp()
            }
        }
    }
}

private data class NavItem(
    val id: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun LanguagePartnerMainApp(
    viewModel: LanguagePartnerViewModel = viewModel()
) {
    val context = LocalContext.current
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Handled
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val selectedPartner by viewModel.selectedPartner.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val messages by viewModel.currentMessages.collectAsState()
    val vocabulary by viewModel.vocabulary.collectAsState()
    val userProgress by viewModel.userProgress.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val userLevel by viewModel.userLevel.collectAsState()
    val autoSpeak by viewModel.autoPlayAudio.collectAsState()
    val playingMessageId by viewModel.currentlyPlayingMessageId.collectAsState()

    val navItems = listOf(
        NavItem("chat", "Chat", Icons.Default.ChatBubbleOutline),
        NavItem("partners", "Partners", Icons.Default.PeopleOutline),
        NavItem("vocab", "Lexicon", Icons.Default.MenuBook),
        NavItem("stats", "Metrics", Icons.Default.BarChart)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, BorderSubtle))
            ) {
                navItems.forEach { item ->
                    val isSelected = activeTab == item.id
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setActiveTab(item.id) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title.uppercase(),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = BrandLime,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = BrandLime
                        ),
                        modifier = Modifier.testTag("nav_tab_${item.id}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BrandBlack)
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "chat" -> {
                    val partner = selectedPartner ?: PartnerCatalog.PARTNERS.first()
                    ChatScreen(
                        partner = partner,
                        messages = messages,
                        isGenerating = isGenerating,
                        userLevel = userLevel,
                        autoSpeak = autoSpeak,
                        playingMessageId = playingMessageId,
                        onSendMessage = { viewModel.sendMessage(it) },
                        onSpeakText = { id, text, lang -> viewModel.speakText(id, text, lang) },
                        onStopSpeech = { viewModel.stopSpeech() },
                        onToggleAutoSpeak = { viewModel.toggleAutoPlay() },
                        onSetUserLevel = { viewModel.setUserLevel(it) },
                        onAddVocab = { word, trans, ctx, note ->
                            viewModel.addVocabulary(word, trans, ctx, note)
                        },
                        onClearChat = { viewModel.clearChat() }
                    )
                }
                "partners" -> {
                    PartnerSelectorScreen(
                        selectedPartnerId = selectedPartner?.id,
                        onSelectPartner = { partner ->
                            viewModel.selectPartner(partner)
                        }
                    )
                }
                "vocab" -> {
                    VocabBuilderScreen(
                        vocabulary = vocabulary,
                        onAddWord = { word, trans, lang, ctx, note ->
                            viewModel.addVocabulary(word, trans, ctx, note)
                        },
                        onDeleteWord = { id ->
                            viewModel.removeVocabulary(id)
                        }
                    )
                }
                "stats" -> {
                    ProgressStatsScreen(
                        progress = userProgress
                    )
                }
            }
        }
    }
}
