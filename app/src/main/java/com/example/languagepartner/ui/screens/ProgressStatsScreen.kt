package com.example.languagepartner.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    modifier: Modifier = Modifier
) {
    val accuracy = progress.grammarAccuracyScore
    val totalSent = progress.totalMessagesSent
    val streak = progress.streak

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
                        text = "STATS",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ANALYTICS ENGINE",
                        color = BrandLime,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Your Fluency Metrics",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Metrics 3-Card Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Streak Card
                MetricCard(
                    title = "DAILY STREAK",
                    value = "$streak",
                    subtext = "Active Days",
                    icon = Icons.Default.LocalFireDepartment,
                    accentColor = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )

                // Total Messages Card
                MetricCard(
                    title = "DIALOGUES",
                    value = "$totalSent",
                    subtext = "Messages Sent",
                    icon = Icons.Default.QuestionAnswer,
                    accentColor = Color(0xFF60A5FA),
                    modifier = Modifier.weight(1f)
                )

                // Accuracy Card
                MetricCard(
                    title = "ACCURACY",
                    value = "$accuracy%",
                    subtext = "Grammar Score",
                    icon = Icons.Default.Verified,
                    accentColor = BrandLime,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Accuracy Meter Gauge Card
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
                        Text(
                            text = "LINGUISTIC ACCURACY ANALYSIS",
                            color = BrandLime,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$accuracy / 100",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { (accuracy / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = BrandLime,
                        trackColor = SurfaceElevated
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val evaluationText = when {
                        accuracy >= 90 -> "Exceptional syntax discipline. Your phrasing closely mirrors native speech patterns."
                        accuracy >= 75 -> "Solid conversational comprehension with occasional minor tense or preposition slips."
                        accuracy >= 50 -> "Developing vocabulary foundations. Focus on recommended grammatical revisions in chat."
                        else -> "Initial learning phase. Keep conversing to unlock higher syntactical rhythm."
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
                text = "ACHIEVEMENTS DECK",
                color = BrandLime,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        val badges = listOf(
            AchievementBadge(
                id = "first_words",
                title = "First Words",
                description = "Initiate your initial dialogue exchange with a native AI partner.",
                icon = Icons.Default.QuestionAnswer,
                isUnlocked = totalSent >= 1
            ),
            AchievementBadge(
                id = "grammar_master",
                title = "Grammar Master",
                description = "Maintain >= 85% grammatical accuracy across at least 5 exchanges.",
                icon = Icons.Default.Verified,
                isUnlocked = totalSent >= 5 && accuracy >= 85
            ),
            AchievementBadge(
                id = "polyglot_apprentice",
                title = "Polyglot Apprentice",
                description = "Engage in 10 or more conversational turnaround exchanges.",
                icon = Icons.Default.EmojiEvents,
                isUnlocked = totalSent >= 10
            ),
            AchievementBadge(
                id = "daily_streak_enthusiast",
                title = "Daily Streak Enthusiast",
                description = "Keep active practice across 3 consecutive daily sessions.",
                icon = Icons.Default.ElectricBolt,
                isUnlocked = streak >= 3
            )
        )

        items(badges.size) { index ->
            val badge = badges[index]
            AchievementCard(badge = badge)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
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
                fontSize = 22.sp,
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
                    text = if (badge.isUnlocked) "UNLOCKED" else "LOCKED",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (badge.isUnlocked) Color.Black else TextSecondary
                )
            }
        }
    }
}
