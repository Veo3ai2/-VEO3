package com.example.languagepartner.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.languagepartner.data.model.Partner
import com.example.languagepartner.data.model.PartnerCatalog
import com.example.languagepartner.ui.theme.BorderSubtle
import com.example.languagepartner.ui.theme.BrandBlack
import com.example.languagepartner.ui.theme.BrandLime
import com.example.languagepartner.ui.theme.SurfaceCard
import com.example.languagepartner.ui.theme.SurfaceElevated
import com.example.languagepartner.ui.theme.TextPrimary
import com.example.languagepartner.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartnerSelectorScreen(
    selectedPartnerId: String?,
    onSelectPartner: (Partner) -> Unit,
    modifier: Modifier = Modifier
) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(BrandLime)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "مباشر",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "دليل الشركاء",
                        color = BrandLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "اختر شريك المحادثة للممارسة",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        itemsIndexed(PartnerCatalog.PARTNERS) { index, partner ->
            val isSelected = partner.id == selectedPartnerId
            val numberStr = String.format("%02d", index + 1)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("partner_card_${partner.id}")
                    .clickable { onSelectPartner(partner) },
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) BrandLime else SurfaceCard
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) BrandLime else BorderSubtle
                )
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Big decorative watermark number
                    Text(
                        text = numberStr,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (isSelected) Color(0x15000000) else Color(0x08FFFFFF),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 4.dp, bottom = 0.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Top row with flag & titles
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(if (isSelected) Color(0x20000000) else Color(0x20FFFFFF))
                                    .border(1.dp, if (isSelected) Color(0x30000000) else BorderSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = partner.flag,
                                    fontSize = 26.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = partner.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isSelected) Color.Black else TextPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(if (isSelected) Color.Black else Color(0x22D4FF00))
                                            .border(1.dp, if (isSelected) Color.Black else BrandLime.copy(alpha = 0.4f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = partner.languageName.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isSelected) BrandLime else BrandLime
                                        )
                                    }
                                }
                                Text(
                                    text = partner.role,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.Black.copy(alpha = 0.7f) else TextSecondary,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .border(1.dp, if (isSelected) Color.Black else BorderSubtle)
                                    .background(if (isSelected) Color.Black else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = numberStr,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) BrandLime else TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Description
                        Text(
                            text = partner.description,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = if (isSelected) Color.Black.copy(alpha = 0.85f) else TextPrimary.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Foci topics
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CompassCalibration,
                                contentDescription = null,
                                tint = if (isSelected) Color.Black.copy(alpha = 0.7f) else BrandLime,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "المواضيع:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black.copy(alpha = 0.7f) else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                partner.suggestedTopics.take(2).forEach { topic ->
                                    Box(
                                        modifier = Modifier
                                            .background(if (isSelected) Color.Black.copy(alpha = 0.1f) else Color(0x15FFFFFF))
                                            .border(1.dp, if (isSelected) Color.Black.copy(alpha = 0.2f) else BorderSubtle)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = topic.uppercase(),
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isSelected) Color.Black else TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
