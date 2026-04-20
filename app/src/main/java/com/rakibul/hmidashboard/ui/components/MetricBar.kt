package com.rakibul.hmidashboard.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakibul.hmidashboard.ui.theme.*

@Composable
fun MetricBar(
    label: String,
    value: Float,
    maxValue: Float,
    displayText: String,
    barColor: Color,
    modifier: Modifier = Modifier,
    warnThreshold: Float = 0f,
    dangerThreshold: Float = 0f,
    invertThresholds: Boolean = false
) {
    val animatedFraction by animateFloatAsState(
        targetValue = (value / maxValue).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400),
        label = "bar_anim"
    )

    val effectiveColor = when {
        invertThresholds -> when {
            value >= dangerThreshold && dangerThreshold > 0f -> GaugeDanger
            value >= warnThreshold && warnThreshold > 0f -> GaugeWarning
            else -> barColor
        }
        else -> when {
            dangerThreshold > 0f && value <= dangerThreshold -> GaugeDanger
            warnThreshold > 0f && value <= warnThreshold -> GaugeWarning
            else -> barColor
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = AutomotiveTextMuted,
                    letterSpacing = 2.sp
                )
            )
            Text(
                text = displayText,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = effectiveColor,
                    letterSpacing = 0.5.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AutomotiveSurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedFraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(effectiveColor)
            )
        }
    }
}