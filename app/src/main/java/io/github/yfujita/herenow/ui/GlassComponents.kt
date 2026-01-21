package io.github.yfujita.herenow.ui

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedAuroraBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")

    val t1: Float by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(12000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "t1",
    )

    val t2: Float by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(18000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "t2",
    )

    val t3: Float by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(25000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "t3",
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                Color(0xFF6366F1).copy(alpha = 0.3f),
                                Color.Transparent,
                            ),
                        center =
                            Offset(
                                width * 0.2f + width * 0.5f * t1,
                                height * 0.3f + height * 0.1f * t2,
                            ),
                        radius = width * 1.0f,
                    ),
            )

            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                Color(0xFFEC4899).copy(alpha = 0.25f),
                                Color.Transparent,
                            ),
                        center =
                            Offset(
                                width * 0.9f - width * 0.6f * t2,
                                height * 0.6f + height * 0.2f * t3,
                            ),
                        radius = width * 1.1f,
                    ),
            )

            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                Color(0xFF10B981).copy(alpha = 0.2f),
                                Color.Transparent,
                            ),
                        center =
                            Offset(
                                width * 0.5f + width * 0.2f * t3,
                                height * 0.8f - height * 0.4f * t1,
                            ),
                        radius = width * 0.9f,
                    ),
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.Transparent,
                                    Color(0xFF0F172A).copy(alpha = 0.3f),
                                ),
                        ),
                    ),
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    glassAlpha: Float = 0.08f,
    borderColor: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val borderBrush =
        if (borderColor != null) {
            Brush.verticalGradient(
                colors =
                    listOf(
                        borderColor.copy(alpha = 0.6f),
                        borderColor.copy(alpha = 0.3f),
                        borderColor.copy(alpha = 0.1f),
                    ),
            )
        } else {
            Brush.verticalGradient(
                colors =
                    listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.1f),
                        Color.White.copy(alpha = 0.05f),
                    ),
            )
        }

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(24.dp)),
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .then(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.blur(30.dp)
                        } else {
                            Modifier
                        },
                    )
                    .background(Color.White.copy(alpha = glassAlpha)),
        )

        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .border(
                        width = 1.dp,
                        brush = borderBrush,
                        shape = RoundedCornerShape(24.dp),
                    )
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        Color.White.copy(alpha = 0.05f),
                                        Color.White.copy(alpha = 0.01f),
                                    ),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                            ),
                    ),
        )

        Box(content = content)
    }
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(32.dp))
                .clickable(onClick = onClick),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .then(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.blur(15.dp)
                        } else {
                            Modifier
                        },
                    )
                    .background(Color.White.copy(alpha = 0.15f)),
        )

        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .border(
                        width = 1.dp,
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.White.copy(alpha = 0.5f),
                                        Color.White.copy(alpha = 0.1f),
                                    ),
                            ),
                        shape = RoundedCornerShape(32.dp),
                    )
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        Color.White.copy(alpha = 0.1f),
                                        Color.White.copy(alpha = 0.02f),
                                    ),
                            ),
                    ),
        )

        Box(content = content)
    }
}
