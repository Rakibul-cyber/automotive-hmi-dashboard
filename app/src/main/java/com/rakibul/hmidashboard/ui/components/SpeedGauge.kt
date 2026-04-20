package com.rakibul.hmidashboard.ui.components

import android.graphics.Paint as NativePaint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakibul.hmidashboard.ui.theme.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val GAUGE_START_ANGLE = 150f
private const val GAUGE_SWEEP = 240f

@Composable
fun SpeedGauge(
    speedKmh: Float,
    maxSpeed: Float = 240f,
    gpsSpeedKmh: Float = -1f,
    modifier: Modifier = Modifier
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = speedKmh.coerceIn(0f, maxSpeed),
        animationSpec = tween(durationMillis = 180),
        label = "speed_anim"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val radius = min(w, h) / 2f * 0.86f
            val ringStroke = min(w, h) * 0.055f
            val halfStroke = ringStroke / 2f

            val arcRect = Size(radius * 2f, radius * 2f)
            val arcTopLeft = Offset(cx - radius, cy - radius)

            // Background arc ring
            drawArc(
                color = GaugeRingBg,
                startAngle = GAUGE_START_ANGLE,
                sweepAngle = GAUGE_SWEEP,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcRect,
                style = Stroke(width = ringStroke, cap = StrokeCap.Round)
            )

            // Colored speed arc — three-zone segmented
            val fraction = animatedSpeed / maxSpeed
            val activeSweep = fraction * GAUGE_SWEEP

            if (activeSweep > 0f) {
                val normalBreak = (80f / maxSpeed) * GAUGE_SWEEP
                val warnBreak = (130f / maxSpeed) * GAUGE_SWEEP

                fun drawZoneArc(color: androidx.compose.ui.graphics.Color, start: Float, sweep: Float) {
                    if (sweep > 0f) drawArc(
                        color = color,
                        startAngle = GAUGE_START_ANGLE + start,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcRect,
                        style = Stroke(width = ringStroke, cap = StrokeCap.Butt)
                    )
                }

                when {
                    activeSweep <= normalBreak -> drawZoneArc(GaugeNormal, 0f, activeSweep)
                    activeSweep <= warnBreak -> {
                        drawZoneArc(GaugeNormal, 0f, normalBreak)
                        drawZoneArc(GaugeWarning, normalBreak, activeSweep - normalBreak)
                    }
                    else -> {
                        drawZoneArc(GaugeNormal, 0f, normalBreak)
                        drawZoneArc(GaugeWarning, normalBreak, warnBreak - normalBreak)
                        drawZoneArc(GaugeDanger, warnBreak, activeSweep - warnBreak)
                    }
                }
            }

            // Tick marks and labels
            val tickOuterR = radius - halfStroke - 2.dp.toPx()
            val labelPaint = NativePaint().apply {
                isAntiAlias = true
                color = GaugeTickMajor.toArgb()
                textSize = (min(w, h) * 0.040f)
                textAlign = NativePaint.Align.CENTER
            }

            for (i in 0..12) {
                val tickFraction = i / 12f
                val angleRad = Math.toRadians((GAUGE_START_ANGLE + tickFraction * GAUGE_SWEEP).toDouble())
                val cosA = cos(angleRad).toFloat()
                val sinA = sin(angleRad).toFloat()

                val isMajor = i % 2 == 0
                val tickLen = if (isMajor) min(w, h) * 0.055f else min(w, h) * 0.028f
                val color = if (isMajor) GaugeTickMajor else GaugeTickMinor

                val outerPt = Offset(cx + tickOuterR * cosA, cy + tickOuterR * sinA)
                val innerPt = Offset(cx + (tickOuterR - tickLen) * cosA, cy + (tickOuterR - tickLen) * sinA)
                drawLine(color, outerPt, innerPt, strokeWidth = if (isMajor) 2.5f else 1.2f)

                if (isMajor) {
                    val labelR = tickOuterR - tickLen - min(w, h) * 0.045f
                    val lx = cx + labelR * cosA
                    val ly = cy + labelR * sinA + labelPaint.textSize * 0.38f
                    drawContext.canvas.nativeCanvas.drawText(
                        (i * 20).toString(), lx, ly, labelPaint
                    )
                }
            }

            // Needle
            val needleAngleRad = Math.toRadians((GAUGE_START_ANGLE + fraction * GAUGE_SWEEP).toDouble())
            val needleCosA = cos(needleAngleRad).toFloat()
            val needleSinA = sin(needleAngleRad).toFloat()
            val needleLen = radius - halfStroke - min(w, h) * 0.10f
            val needleTip = Offset(cx + needleLen * needleCosA, cy + needleLen * needleSinA)
            val needleBase = Offset(cx - min(w, h) * 0.04f * needleCosA, cy - min(w, h) * 0.04f * needleSinA)

            // Needle glow
            drawLine(
                color = GaugeNeedle.copy(alpha = 0.2f),
                start = needleBase,
                end = needleTip,
                strokeWidth = min(w, h) * 0.025f,
                cap = StrokeCap.Round
            )
            // Needle core
            drawLine(
                color = GaugeNeedle,
                start = needleBase,
                end = needleTip,
                strokeWidth = min(w, h) * 0.008f,
                cap = StrokeCap.Round
            )

            // Hub circles
            val hubR = min(w, h) * 0.07f
            drawCircle(GaugeHub, radius = hubR, center = Offset(cx, cy))
            drawCircle(GaugeNeedle, radius = min(w, h) * 0.022f, center = Offset(cx, cy))
            drawCircle(AutomotiveBackground, radius = min(w, h) * 0.010f, center = Offset(cx, cy))
        }

        // Center speed readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = 24.dp)
        ) {
            Text(
                text = speedKmh.toInt().toString(),
                style = TextStyle(
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Thin,
                    color = AutomotiveText,
                    letterSpacing = (-2).sp
                )
            )
            Text(
                text = "KM/H",
                style = TextStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = AutomotiveTextMuted,
                    letterSpacing = 3.sp
                )
            )
            if (gpsSpeedKmh >= 0f) {
                Text(
                    text = "GPS ${gpsSpeedKmh.toInt()}",
                    style = TextStyle(
                        fontSize = 9.sp,
                        color = GaugeNormal,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}