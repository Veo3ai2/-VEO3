package com.example.languagepartner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.languagepartner.ui.theme.BrandLime
import com.example.languagepartner.ui.theme.SurfaceDark
import com.example.languagepartner.ui.theme.TextPrimary
import kotlin.random.Random

data class ConfettiParticle(
    val initialX: Float,
    val initialY: Float,
    val speedX: Float,
    val speedY: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float,
    val shapeType: Int // 0: rectangle, 1: circle, 2: ribbon
)

@Composable
fun ConfettiCelebrationOverlay(
    visible: Boolean,
    title: String = "هدف محقق!",
    subtitle: String = "أحسنت! واصل التقدم الرائع اليوم.",
    onDismiss: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(400))
    ) {
        val animationProgress = remember { Animatable(0f) }

        val particles = remember {
            val palette = listOf(
                BrandLime,
                Color(0xFF38BDF8), // Blue
                Color(0xFFF43F5E), // Rose
                Color(0xFFFBBF24), // Amber
                Color(0xFFA855F7), // Purple
                Color(0xFF34D399)  // Emerald
            )
            List(70) {
                ConfettiParticle(
                    initialX = Random.nextFloat(),
                    initialY = Random.nextFloat() * 0.15f - 0.1f, // start near top
                    speedX = (Random.nextFloat() - 0.5f) * 0.5f,
                    speedY = 0.6f + Random.nextFloat() * 0.7f,
                    color = palette[Random.nextInt(palette.size)],
                    size = 10f + Random.nextFloat() * 14f,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                    shapeType = Random.nextInt(3)
                )
            }
        }

        LaunchedEffect(visible) {
            if (visible) {
                animationProgress.snapTo(0f)
                animationProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 3200, easing = LinearEasing)
                )
                onDismiss()
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val p = animationProgress.value
                val canvasW = size.width
                val canvasH = size.height

                particles.forEach { particle ->
                    val x = (particle.initialX + particle.speedX * p) * canvasW
                    val y = (particle.initialY + particle.speedY * p) * canvasH
                    val alpha = (1f - (p * 1.1f).coerceIn(0f, 1f)).coerceIn(0f, 1f)

                    if (y in 0f..canvasH && x in 0f..canvasW) {
                        rotate(
                            degrees = particle.rotationSpeed * p,
                            pivot = Offset(x, y)
                        ) {
                            when (particle.shapeType) {
                                0 -> {
                                    drawRect(
                                        color = particle.color.copy(alpha = alpha),
                                        topLeft = Offset(x - particle.size / 2, y - particle.size / 3),
                                        size = Size(particle.size, particle.size * 0.6f)
                                    )
                                }
                                1 -> {
                                    drawCircle(
                                        color = particle.color.copy(alpha = alpha),
                                        radius = particle.size / 2.5f,
                                        center = Offset(x, y)
                                    )
                                }
                                else -> {
                                    drawRoundRect(
                                        color = particle.color.copy(alpha = alpha),
                                        topLeft = Offset(x - particle.size / 4, y - particle.size),
                                        size = Size(particle.size * 0.5f, particle.size * 1.5f),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Banner card
            Box(
                modifier = Modifier
                    .padding(top = 48.dp, start = 20.dp, end = 20.dp)
                    .background(SurfaceDark.copy(alpha = 0.95f), RoundedCornerShape(12.dp))
                    .border(2.dp, BrandLime, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "جائزة إنجاز",
                        tint = BrandLime,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = title,
                            color = BrandLime,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = subtitle,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
