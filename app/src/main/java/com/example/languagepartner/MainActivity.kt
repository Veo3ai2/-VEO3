package com.example.languagepartner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Application
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.languagepartner.data.model.PartnerCatalog
import com.example.languagepartner.ui.auth.authStateFlow
import com.example.languagepartner.ui.auth.attemptAutoSignIn
import com.example.languagepartner.ui.auth.signOut
import com.example.languagepartner.ui.components.ConfettiCelebrationOverlay
import com.example.languagepartner.ui.screens.AuthScreen
import com.example.languagepartner.ui.screens.ChatScreen
import com.example.languagepartner.ui.screens.PartnerSelectorScreen
import com.example.languagepartner.ui.screens.ProgressStatsScreen
import com.example.languagepartner.ui.screens.VocabBuilderScreen
import com.example.languagepartner.ui.theme.BorderSubtle
import com.example.languagepartner.ui.theme.BrandBlack
import com.example.languagepartner.ui.theme.BrandLime
import com.example.languagepartner.ui.theme.BrandLimeDim
import com.example.languagepartner.ui.theme.LanguagePartnerTheme
import com.example.languagepartner.ui.theme.SurfaceCard
import com.example.languagepartner.ui.theme.SurfaceDark
import com.example.languagepartner.ui.theme.TextPrimary
import com.example.languagepartner.ui.theme.TextSecondary
import com.example.languagepartner.ui.viewmodel.LanguagePartnerViewModel
import com.example.languagepartner.ui.viewmodel.LanguagePartnerViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LanguagePartnerTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(
    auth: FirebaseAuth = Firebase.auth
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val currentUser by auth.authStateFlow().collectAsState(initial = auth.currentUser)

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {},
            onUnauthenticated = {},
            scope = scope
        )
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = {}
        )
    } else {
        LanguagePartnerMainApp(
            userId = user.uid,
            userEmail = user.email ?: user.displayName ?: "Learner",
            onSignOut = {
                signOut(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {},
                    scope = scope
                )
            }
        )
    }
}

private data class NavItem(
    val id: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun LanguagePartnerMainApp(
    userId: String,
    userEmail: String,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: LanguagePartnerViewModel = viewModel(
        factory = LanguagePartnerViewModelFactory(application, userId)
    )
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
    val celebrationEvent by viewModel.celebrationEvent.collectAsState()

    val navItems = listOf(
        NavItem("chat", "المحادثة", Icons.Default.ChatBubbleOutline),
        NavItem("partners", "الشركاء", Icons.Default.PeopleOutline),
        NavItem("vocab", "المفردات", Icons.Default.MenuBook),
        NavItem("stats", "الإحصائيات", Icons.Default.BarChart)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .border(BorderStroke(1.dp, BorderSubtle)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(BrandLimeDim)
                            .border(BorderStroke(1.dp, BrandLime), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "السحابة متصلة",
                            tint = BrandLime,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "رفيق اللغة",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "مزامنة سحابية نشطة",
                            color = BrandLime,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceCard)
                            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "حساب المستخدم",
                                tint = TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = userEmail,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 120.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "تسجيل الخروج",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
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
                        onClearChat = { viewModel.clearChat() },
                        onExportChat = {
                            if (messages.isEmpty()) {
                                Toast.makeText(context, "لا توجد رسائل لتصديرها", Toast.LENGTH_SHORT).show()
                            } else {
                                try {
                                    val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                    val stringBuilder = StringBuilder()
                                    stringBuilder.append("========================================\n")
                                    stringBuilder.append("تطبيق رفيق اللغة - تصدير المحادثة\n")
                                    stringBuilder.append("الشريك اللغوي: ${partner.name} (${partner.languageName})\n")
                                    stringBuilder.append("تاريخ التصدير: ${timeFormat.format(Date())}\n")
                                    stringBuilder.append("========================================\n\n")

                                    messages.forEach { msg ->
                                        val sender = if (msg.role == "user") "أنت (المتعلم)" else "${partner.name} (${partner.languageName})"
                                        val msgTime = timeFormat.format(Date(msg.timestamp))
                                        stringBuilder.append("[$msgTime] $sender:\n")
                                        stringBuilder.append(msg.text)
                                        stringBuilder.append("\n")

                                        if (!msg.translation.isNullOrBlank()) {
                                            stringBuilder.append("الترجمة: ${msg.translation}\n")
                                        }
                                        if (msg.feedback != null) {
                                            if (!msg.feedback.correctedText.isNullOrBlank()) {
                                                stringBuilder.append("تصحيح القواعد: ${msg.feedback.correctedText}\n")
                                            }
                                            if (!msg.feedback.explanation.isNullOrBlank()) {
                                                stringBuilder.append("الشرح: ${msg.feedback.explanation}\n")
                                            }
                                        }
                                        stringBuilder.append("----------------------------------------\n\n")
                                    }

                                    // Write to export file
                                    val exportDir = File(context.cacheDir, "exports")
                                    if (!exportDir.exists()) exportDir.mkdirs()
                                    val file = File(exportDir, "chat_${partner.id}_${System.currentTimeMillis()}.txt")
                                    file.writeText(stringBuilder.toString())

                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )

                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_SUBJECT, "محادثة مع ${partner.name} - رفيق اللغة")
                                        putExtra(Intent.EXTRA_TEXT, "تصدير محادثة رفيق اللغة مع ${partner.name}")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }

                                    context.startActivity(
                                        Intent.createChooser(shareIntent, "حفظ أو مشاركة المحادثة كملف نصي")
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(context, "حدث خطأ أثناء تصدير المحادثة", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
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
                        },
                        onSpeakWord = { word, lang ->
                            viewModel.speakWord(word, lang)
                        }
                    )
                }
                "stats" -> {
                    ProgressStatsScreen(
                        progress = userProgress,
                        onSetDailyGoal = { newGoal ->
                            viewModel.setDailyMessageGoal(newGoal)
                        },
                        onSetDailyVocabGoal = { newGoal ->
                            viewModel.setDailyVocabGoal(newGoal)
                        }
                    )
                }
            }

            // Celebratory confetti overlay on goal completion
            celebrationEvent?.let { event ->
                ConfettiCelebrationOverlay(
                    visible = true,
                    title = event.title,
                    subtitle = event.subtitle,
                    onDismiss = { viewModel.dismissCelebration() }
                )
            }
        }
    }
}
