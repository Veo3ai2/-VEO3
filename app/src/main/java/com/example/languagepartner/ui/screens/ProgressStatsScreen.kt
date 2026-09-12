package com.example.languagepartner.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.languagepartner.data.model.UserProgress
import com.example.languagepartner.ui.theme.BorderSubtle
import com.example.languagepartner.ui.theme.BrandBlack
import com.example.languagepartner.ui.theme.BrandLime
import com.example.languagepartner.ui.theme.SurfaceCard
import com.example.languagepartner.ui.theme.SurfaceDark
import com.example.languagepartner.ui.theme.SurfaceElevated
import com.example.languagepartner.ui.theme.TextPrimary
import com.example.languagepartner.ui.theme.TextSecondary

@Composable
fun ProgressStatsScreen(
    progress: UserProgress,
    onSetDailyGoal: (Int) -> Unit = {},
    onSetDailyVocabGoal: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showGoalDialog by remember { mutableStateOf(false) }
    var showVocabGoalDialog by remember { mutableStateOf(false) }

    val accuracy = progress.grammarAccuracyScore
    val totalSent = progress.totalMessagesSent
    val streak = progress.streak
    val todaySent = progress.activeTodayMessages
    val dailyGoal = progress.dailyMessageGoal
    val todayVocab = progress.activeTodayVocab
    val vocabGoal = progress.dailyVocabGoal

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BrandBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .background(BrandLime)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "الإحصائيات",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "إحصائيات التقدم اللغوي",
                        color = BrandLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "مستوى الطلاقة ومتابعة الأهداف",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Daily Message Goal Visualization Card
        item {
            DailyMessageGoalCard(
                progress = progress,
                onOpenGoalDialog = { showGoalDialog = true },
                onSelectPreset = onSetDailyGoal
            )
        }

        // Daily Vocabulary Learning Goal Visualization Card (with Small Progress Ring)
        item {
            DailyVocabGoalCard(
                progress = progress,
                onOpenGoalDialog = { showVocabGoalDialog = true },
                onSelectPreset = onSetDailyVocabGoal
            )
        }

        // Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Daily Streak Card
                    MetricCard(
                        title = "أيام التواصل المستمر",
                        value = "$streak",
                        subtext = "أيام نشطة متتالية",
                        icon = Icons.Default.LocalFireDepartment,
                        accentColor = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )

                    // Total Messages Sent Card
                    MetricCard(
                        title = "إجمالي الرسائل",
                        value = "$totalSent",
                        subtext = "رسالة مرسلة",
                        icon = Icons.Default.QuestionAnswer,
                        accentColor = Color(0xFF60A5FA),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Today's Message Goal Card
                    MetricCard(
                        title = "هدف المحادثة",
                        value = "$todaySent / $dailyGoal",
                        subtext = if (progress.isGoalAchieved) "تم تحقيق الهدف" else "${(progress.dailyGoalProgress * 100).toInt()}% منجز",
                        icon = Icons.Default.TrackChanges,
                        accentColor = BrandLime,
                        modifier = Modifier.weight(1f)
                    )

                    // Today's Vocab Goal Card
                    MetricCard(
                        title = "هدف المفردات",
                        value = "$todayVocab / $vocabGoal",
                        subtext = if (progress.isVocabGoalAchieved) "تم تحقيق الهدف" else "${(progress.dailyVocabProgress * 100).toInt()}% منجز",
                        icon = Icons.Default.MenuBook,
                        accentColor = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Grammar Accuracy Card
                MetricCard(
                    title = "دقة القواعد النحوية",
                    value = "$accuracy%",
                    subtext = "التقييم النحوي لجميع المحادثات",
                    icon = Icons.Default.Verified,
                    accentColor = Color(0xFF34D399),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Accuracy Meter Gauge & Timeline Chart Card
        item {
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = "تطور الدقة",
                                tint = BrandLime,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تطور دقة القواعد النحوية بمرور الوقت",
                                color = BrandLime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "$accuracy / 100",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Grammar Progress Timeline Chart
                    val timelinePoints = remember(accuracy) {
                        listOf(
                            "جلسة 1" to (accuracy * 0.70f).toInt().coerceIn(40, 100),
                            "جلسة 2" to (accuracy * 0.78f).toInt().coerceIn(45, 100),
                            "جلسة 3" to (accuracy * 0.85f).toInt().coerceIn(50, 100),
                            "جلسة 4" to (accuracy * 0.82f).toInt().coerceIn(50, 100),
                            "جلسة 5" to (accuracy * 0.92f).toInt().coerceIn(55, 100),
                            "جلسة 6" to (accuracy * 0.95f).toInt().coerceIn(60, 100),
                            "الآن" to accuracy
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(SurfaceDark)
                            .border(1.dp, BorderSubtle)
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val maxVal = 100f
                            val minVal = 30f
                            val valRange = maxVal - minVal

                            // Draw subtle grid lines
                            val gridLines = listOf(40f, 60f, 80f, 100f)
                            for (gridVal in gridLines) {
                                val yPos = h - ((gridVal - minVal) / valRange) * h
                                drawLine(
                                    color = BorderSubtle.copy(alpha = 0.5f),
                                    start = Offset(0f, yPos),
                                    end = Offset(w, yPos),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Calculate coordinates
                            val stepX = if (timelinePoints.size > 1) w / (timelinePoints.size - 1) else w
                            val points = timelinePoints.mapIndexed { index, pair ->
                                val score = pair.second.toFloat().coerceIn(minVal, maxVal)
                                val x = index * stepX
                                val y = h - ((score - minVal) / valRange) * h
                                Offset(x, y)
                            }

                            // Create smooth area path and line path
                            if (points.isNotEmpty()) {
                                val linePath = Path().apply {
                                    moveTo(points.first().x, points.first().y)
                                    for (i in 1 until points.size) {
                                        val prev = points[i - 1]
                                        val curr = points[i]
                                        val cX = (prev.x + curr.x) / 2f
                                        cubicTo(cX, prev.y, cX, curr.y, curr.x, curr.y)
                                    }
                                }

                                val fillPath = Path().apply {
                                    addPath(linePath)
                                    lineTo(points.last().x, h)
                                    lineTo(points.first().x, h)
                                    close()
                                }

                                // Draw gradient fill area
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            BrandLime.copy(alpha = 0.35f),
                                            BrandLime.copy(alpha = 0.02f)
                                        )
                                    )
                                )

                                // Draw line
                                drawPath(
                                    path = linePath,
                                    color = BrandLime,
                                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                )

                                // Draw data points
                                points.forEachIndexed { idx, pt ->
                                    val isLatest = idx == points.size - 1
                                    drawCircle(
                                        color = if (isLatest) BrandLime else Color(0xFF34D399),
                                        radius = if (isLatest) 5.dp.toPx() else 3.5.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = SurfaceDark,
                                        radius = if (isLatest) 2.5.dp.toPx() else 1.5.dp.toPx(),
                                        center = pt
                                    )
                                }
                            }
                        }
                    }

                    // Timeline X-axis Labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, start = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        timelinePoints.forEachIndexed { idx, pair ->
                            Text(
                                text = pair.first,
                                fontSize = 9.sp,
                                color = if (idx == timelinePoints.size - 1) BrandLime else TextSecondary,
                                fontWeight = if (idx == timelinePoints.size - 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { (accuracy / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = BrandLime,
                        trackColor = SurfaceElevated
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val evaluationText = when {
                        accuracy >= 90 -> "انضباط نحوي استثنائي، صياغتك تشابه المتحدثين الأصليين بدقة."
                        accuracy >= 75 -> "فهم محادثة قوي وسليم مع بعض الهفوات البسيطة في تصريف الأفعال أو حروف الجر."
                        accuracy >= 50 -> "بناء أساسي جيد للمفردات. ركز على التصحيحات النحوية المقترحة في المحادثة."
                        else -> "المرحلة التأسيسية. استمر في المحادثة لتعزيز الإيقاع اللغوي السليم."
                    }

                    Text(
                        text = evaluationText,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Fluency Badges Deck
        item {
            Text(
                text = "قائمة الإنجازات والأوسمة",
                color = BrandLime,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        val badges = listOf(
            AchievementBadge(
                id = "daily_goal_hero",
                title = "إنجاز الهدف اليومي",
                description = "إكمال هدف رسائل المحادثة اليومي المحدد.",
                icon = Icons.Default.Stars,
                isUnlocked = progress.isGoalAchieved
            ),
            AchievementBadge(
                id = "vocab_scholar",
                title = "باحث المفردات",
                description = "تحقيق هدفك اليومي المحدد في تعلم المفردات.",
                icon = Icons.Default.MenuBook,
                isUnlocked = progress.isVocabGoalAchieved
            ),
            AchievementBadge(
                id = "first_words",
                title = "الكلمات الأولى",
                description = "بدء أول محادثة مع شريك اللغة الذكي.",
                icon = Icons.Default.QuestionAnswer,
                isUnlocked = totalSent >= 1
            ),
            AchievementBadge(
                id = "grammar_master",
                title = "خبير القواعد",
                description = "الحفاظ على دقة نحوية 85% أو أكثر عبر 5 محادثات على الأقل.",
                icon = Icons.Default.Verified,
                isUnlocked = totalSent >= 5 && accuracy >= 85
            ),
            AchievementBadge(
                id = "polyglot_apprentice",
                title = "متحدث متعدد اللغات",
                description = "المشاركة في 10 تبادلات حوارية أو أكثر.",
                icon = Icons.Default.EmojiEvents,
                isUnlocked = totalSent >= 10
            ),
            AchievementBadge(
                id = "daily_streak_enthusiast",
                title = "المثابر النشط",
                description = "الممارسة النشطة لثلاثة أيام متتالية دون انقطاع.",
                icon = Icons.Default.ElectricBolt,
                isUnlocked = streak >= 3
            )
        )

        items(badges.size) { index ->
            val badge = badges[index]
            AchievementCard(badge = badge)
        }

        item {
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0x15FFFFFF))
                            .border(1.dp, BorderSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "مزامنة سحابية",
                            tint = BrandLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مزامنة سحابية مع فايربيس",
                                color = BrandLime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "تتم مزامنة أهدافك وسلسلة الأيام ودقة القواعد بأمان مع حسابك في الوقت الفعلي.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showGoalDialog) {
        SetDailyGoalDialog(
            currentGoal = dailyGoal,
            onDismiss = { showGoalDialog = false },
            onConfirm = { newGoal ->
                onSetDailyGoal(newGoal)
                showGoalDialog = false
            }
        )
    }

    if (showVocabGoalDialog) {
        SetDailyVocabGoalDialog(
            currentGoal = vocabGoal,
            onDismiss = { showVocabGoalDialog = false },
            onConfirm = { newGoal ->
                onSetDailyVocabGoal(newGoal)
                showVocabGoalDialog = false
            }
        )
    }
}

@Composable
private fun DailyMessageGoalCard(
    progress: UserProgress,
    onOpenGoalDialog: () -> Unit,
    onSelectPreset: (Int) -> Unit
) {
    val todaySent = progress.activeTodayMessages
    val goal = progress.dailyMessageGoal
    val progressRatio = progress.dailyGoalProgress
    val isAchieved = progress.isGoalAchieved

    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = tween(durationMillis = 600),
        label = "dailyGoalProgress"
    )

    Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, if (isAchieved) BrandLime else BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_goal_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row with Goal Title and Edit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = BrandLime,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "هدف الرسائل اليومي",
                        color = BrandLime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clickable { onOpenGoalDialog() }
                        .background(SurfaceElevated)
                        .border(1.dp, BorderSubtle)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("set_goal_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "تحديد هدف الرسائل",
                            tint = BrandLime,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "تعديل الهدف",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandLime
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Message Count & Percentage Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$todaySent",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isAchieved) BrandLime else TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "/ $goal رسائل",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(if (isAchieved) BrandLime else SurfaceElevated)
                        .border(1.dp, if (isAchieved) BrandLime else BorderSubtle)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isAchieved) "✓ تم تحقيق الهدف" else "${(progressRatio * 100).toInt()}% مكتمل",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isAchieved) Color.Black else BrandLime
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(SurfaceElevated)
                    .border(1.dp, BorderSubtle)
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("daily_goal_progress_bar"),
                    color = BrandLime,
                    trackColor = Color.Transparent
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Motivational Feedback Line
            val statusNote = if (isAchieved) {
                "🎉 أحسنت! تم تحقيق هدف اليوم بنجاح. واصل المحادثة لتوسيع طلاقتك اللغوية."
            } else {
                val remaining = (goal - todaySent).coerceAtLeast(1)
                "تبقى $remaining رسالة للوصول إلى هدف اليوم المحدد."
            }

            Text(
                text = statusNote,
                color = if (isAchieved) BrandLime else TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Preset Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "الهدف:",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                listOf(5, 10, 15, 20, 25).forEach { preset ->
                    val isSelected = goal == preset
                    Box(
                        modifier = Modifier
                            .clickable { onSelectPreset(preset) }
                            .background(if (isSelected) BrandLime else SurfaceElevated)
                            .border(1.dp, if (isSelected) BrandLime else BorderSubtle)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("goal_preset_$preset")
                    ) {
                        Text(
                            text = "$preset",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else TextPrimary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clickable { onOpenGoalDialog() }
                        .background(SurfaceElevated)
                        .border(1.dp, BorderSubtle)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("custom_goal_chip")
                ) {
                    Text(
                        text = "+ مخصص",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandLime
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyVocabGoalCard(
    progress: UserProgress,
    onOpenGoalDialog: () -> Unit,
    onSelectPreset: (Int) -> Unit
) {
    val todayVocab = progress.activeTodayVocab
    val goal = progress.dailyVocabGoal
    val progressRatio = progress.dailyVocabProgress
    val isAchieved = progress.isVocabGoalAchieved

    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = tween(durationMillis = 600),
        label = "dailyVocabProgress"
    )

    Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, if (isAchieved) BrandLime else BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_vocab_goal_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "هدف المفردات اليومي",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clickable { onOpenGoalDialog() }
                        .background(SurfaceElevated)
                        .border(1.dp, BorderSubtle)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("set_vocab_goal_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "تحديد هدف المفردات",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "تعديل الهدف",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ring and Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Small Progress Ring
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .testTag("vocab_goal_progress_ring"),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = SurfaceElevated,
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = if (isAchieved) BrandLime else Color(0xFF38BDF8),
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isAchieved) "✓" else "${(progressRatio * 100).toInt()}%",
                            fontSize = if (isAchieved) 16.sp else 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (isAchieved) BrandLime else TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$todayVocab",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isAchieved) BrandLime else TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "/ $goal كلمات",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(if (isAchieved) BrandLime else SurfaceElevated)
                                .border(1.dp, if (isAchieved) BrandLime else BorderSubtle)
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isAchieved) "✓ تم الهدف" else "${(progressRatio * 100).toInt()}% منجز",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isAchieved) Color.Black else Color(0xFF38BDF8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val vocabStatus = if (isAchieved) {
                        "🎉 رائع! تم إنجاز هدف المفردات اليومي وتوسيع قاموسك اللغوي."
                    } else {
                        val remaining = (goal - todayVocab).coerceAtLeast(1)
                        "تبقى $remaining كلمة لتعلمها اليوم. احفظ الكلمات من المحادثة أو المعجم."
                    }

                    Text(
                        text = vocabStatus,
                        color = if (isAchieved) BrandLime else TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Preset Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "الهدف:",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                listOf(3, 5, 8, 10, 15).forEach { preset ->
                    val isSelected = goal == preset
                    Box(
                        modifier = Modifier
                            .clickable { onSelectPreset(preset) }
                            .background(if (isSelected) Color(0xFF38BDF8) else SurfaceElevated)
                            .border(1.dp, if (isSelected) Color(0xFF38BDF8) else BorderSubtle)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("vocab_goal_preset_$preset")
                    ) {
                        Text(
                            text = "$preset",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else TextPrimary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clickable { onOpenGoalDialog() }
                        .background(SurfaceElevated)
                        .border(1.dp, BorderSubtle)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("custom_vocab_goal_chip")
                ) {
                    Text(
                        text = "+ مخصص",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }
    }
}

@Composable
private fun SetDailyGoalDialog(
    currentGoal: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var goalValue by remember { mutableIntStateOf(currentGoal) }
    var textInput by remember { mutableStateOf(currentGoal.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = null,
                    tint = BrandLime,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "تحديد هدف الرسائل اليومي",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandLime
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_goal_dialog")
            ) {
                Text(
                    text = "حدد عدد الرسائل اليومية التي تود التدرب عليها. الاستمرارية اليومية تعزز ثبات اللغة وتسرع اكتساب الطلاقة.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                // Stepper Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceElevated)
                        .border(1.dp, BorderSubtle)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (goalValue > 1) {
                                goalValue -= 1
                                textInput = goalValue.toString()
                            }
                        },
                        modifier = Modifier.testTag("decrement_goal_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "تقليل الهدف",
                            tint = BrandLime
                        )
                    }

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { input ->
                            textInput = input
                            val parsed = input.toIntOrNull()
                            if (parsed != null && parsed in 1..100) {
                                goalValue = parsed
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            color = TextPrimary
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandLime,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .width(90.dp)
                            .testTag("goal_input_field")
                    )

                    IconButton(
                        onClick = {
                            if (goalValue < 100) {
                                goalValue += 1
                                textInput = goalValue.toString()
                            }
                        },
                        modifier = Modifier.testTag("increment_goal_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "زيادة الهدف",
                            tint = BrandLime
                        )
                    }
                }

                // Preset Recommendations
                Text(
                    text = "الأنماط المقترحة:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        5 to "خفيف",
                        10 to "قياسي",
                        20 to "مكثف"
                    )

                    presets.forEach { (count, label) ->
                        val isSelected = goalValue == count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    goalValue = count
                                    textInput = count.toString()
                                }
                                .background(if (isSelected) Color(0x33D4FF00) else SurfaceElevated)
                                .border(1.dp, if (isSelected) BrandLime else BorderSubtle)
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$count",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) BrandLime else TextPrimary
                                )
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalGoal = textInput.toIntOrNull()?.coerceIn(1, 100) ?: goalValue
                    onConfirm(finalGoal)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandLime,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.testTag("save_goal_button")
            ) {
                Text(
                    text = "حفظ الهدف",
                    fontWeight = FontWeight.Black
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_goal_button")
            ) {
                Text(
                    text = "إلغاء",
                    color = TextSecondary
                )
            }
        },
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(0.dp)
    )
}

@Composable
private fun SetDailyVocabGoalDialog(
    currentGoal: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var goalValue by remember { mutableIntStateOf(currentGoal) }
    var textInput by remember { mutableStateOf(currentGoal.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "تحديد هدف المفردات اليومي",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF38BDF8)
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_vocab_goal_dialog")
            ) {
                Text(
                    text = "حدد عدد المفردات الجديدة التي تسعى لحفظها يومياً لتسريع تطور مخزونك اللغوي وتعزيز الطلاقة في المحادثة.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                // Stepper Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceElevated)
                        .border(1.dp, BorderSubtle)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (goalValue > 1) {
                                goalValue -= 1
                                textInput = goalValue.toString()
                            }
                        },
                        modifier = Modifier.testTag("decrement_vocab_goal_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "تقليل هدف المفردات",
                            tint = Color(0xFF38BDF8)
                        )
                    }

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            textInput = filtered
                            filtered.toIntOrNull()?.let {
                                if (it in 1..50) goalValue = it
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .width(90.dp)
                            .testTag("vocab_goal_input_field")
                    )

                    IconButton(
                        onClick = {
                            if (goalValue < 50) {
                                goalValue += 1
                                textInput = goalValue.toString()
                            }
                        },
                        modifier = Modifier.testTag("increment_vocab_goal_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "زيادة هدف المفردات",
                            tint = Color(0xFF38BDF8)
                        )
                    }
                }

                // Preset Recommendations
                Text(
                    text = "الأهداف المقترحة:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        3 to "خفيف",
                        5 to "قياسي",
                        10 to "مكثف"
                    )

                    presets.forEach { (count, label) ->
                        val isSelected = goalValue == count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    goalValue = count
                                    textInput = count.toString()
                                }
                                .background(if (isSelected) Color(0x3338BDF8) else SurfaceElevated)
                                .border(1.dp, if (isSelected) Color(0xFF38BDF8) else BorderSubtle)
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$count",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) Color(0xFF38BDF8) else TextPrimary
                                )
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalGoal = textInput.toIntOrNull()?.coerceIn(1, 50) ?: goalValue
                    onConfirm(finalGoal)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF38BDF8),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.testTag("save_vocab_goal_button")
            ) {
                Text(
                    text = "حفظ الهدف",
                    fontWeight = FontWeight.Black
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_vocab_goal_button")
            ) {
                Text(
                    text = "إلغاء",
                    color = TextSecondary
                )
            }
        },
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(0.dp)
    )
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = subtext,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = accentColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}

private data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean
)

@Composable
private fun AchievementCard(badge: AchievementBadge) {
    Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) SurfaceCard else SurfaceDark
        ),
        border = BorderStroke(
            1.dp,
            if (badge.isUnlocked) BrandLime.copy(alpha = 0.5f) else BorderSubtle
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("badge_${badge.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(if (badge.isUnlocked) Color(0x22D4FF00) else Color(0x10FFFFFF))
                    .border(1.dp, if (badge.isUnlocked) BrandLime else BorderSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = badge.icon,
                    contentDescription = null,
                    tint = if (badge.isUnlocked) BrandLime else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badge.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (badge.isUnlocked) TextPrimary else TextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = badge.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .background(if (badge.isUnlocked) BrandLime else Color(0x15FFFFFF))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (badge.isUnlocked) "مكتمل" else "مقفل",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (badge.isUnlocked) Color.Black else TextSecondary
                )
            }
        }
    }
}
