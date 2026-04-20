package com.rakibul.hmidashboard.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakibul.hmidashboard.ui.theme.*
import kotlin.math.min
import kotlin.math.sqrt

private const val G = 9.81f
private const val MAX_G_DISPLAY = 1.5f

@Composable
fun GForceIndicator(
    gForceX: Float,
    gForceY: Float,
    modifier: Modifier = Modifier
) {
    val normalizedX by animateFloatAsState(
        targetValue = (-gForceX / G).coerceIn(-MAX_G_DISPLAY, MAX_G_DISPLAY),
        animationSpec = tween(durationMillis = 150),
        label = "gx_anim"
    )
    val normalizedY by animateFloatAsState(
        targetValue = (-gForceY / G).coerceIn(-MAX_G_DISPLAY, MAX_G_DISPLAY),
        animationSpec = tween(durationMillis = 150),
        label = "gy_anim"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "G-FORCE",
            style = TextStyle(
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = AutomotiveTextMuted,
                letterSpacing = 2.sp
            )
        )

        // Canvas fills remaining Column space
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = min(size.width, size.height) / 2f * 0.88f

            // Outer boundary circle
            drawCircle(AutomotiveSurfaceVariant, radius = r, center = Offset(cx, cy), style = Stroke(width = 1.5f))

            // 1g reference ring
            val oneGRadius = r / MAX_G_DISPLAY
            drawCircle(GaugeTickMinor, radius = oneGRadius, center = Offset(cx, cy), style = Stroke(width = 1f))

            // Crosshairs
            drawLine(GaugeTickMinor, Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth = 0.8f)
            drawLine(GaugeTickMinor, Offset(cx, cy - r), Offset(cx, cy + r), strokeWidth = 0.8f)

            // G-force dot
            val dotX = cx + normalizedX * (r / MAX_G_DISPLAY)
            val dotY = cy + normalizedY * (r / MAX_G_DISPLAY)
            val gMag = sqrt((normalizedX * normalizedX + normalizedY * normalizedY).toDouble()).toFloat()

            val dotColor = when {
                gMag > 1.0f -> GaugeDanger
                gMag > 0.5f -> GaugeWarning
                else -> GaugeNormal
            }

            drawCircle(dotColor.copy(alpha = 0.3f), radius = 14f, center = Offset(dotX, dotY))
            drawCircle(dotColor, radius = 6f, center = Offset(dotX, dotY))
        }

        Text(
            text = "X: ${"%.2f".format(normalizedX)}g  Y: ${"%.2f".format(normalizedY)}g",
            style = TextStyle(fontSize = 8.sp, color = AutomotiveTextMuted, letterSpacing = 0.5.sp)
        )
    }
}