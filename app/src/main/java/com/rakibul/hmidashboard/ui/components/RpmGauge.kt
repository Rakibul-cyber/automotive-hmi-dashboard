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

private const val RPM_START_ANGLE = 150f
private const val RPM_SWEEP = 240f
private const val MAX_RPM = 8000f

@Composable
fun RpmGauge(
    rpm: Float,
    modifier: Modifier = Modifier
) {
    val animatedRpm by animateFloatAsState(
        targetValue = rpm.coerceIn(0f, MAX_RPM),
        animationSpec = tween(durationMillis = 150),
        label = "rpm_anim"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val radius = min(w, h) / 2f * 0.86f
            val ringStroke = min(w, h) * 0.052f
            val halfStroke = ringStroke / 2f

            val arcRect = Size(radius * 2f, radius * 2f)
            val arcTopLeft = Offset(cx - radius, cy - radius)

            // Background ring
            drawArc(
                color = GaugeRingBg,
                startAngle = RPM_START_ANGLE,
                sweepAngle = RPM_SWEEP,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcRect,
                style = Stroke(width = ringStroke, cap = StrokeCap.Round)
            )

            // Colored RPM arc — normal/warn/redzone
            val fraction = animatedRpm / MAX_RPM
            val activeSweep = fraction * RPM_SWEEP

            if (activeSweep > 0f) {
                val normalBreak = (5000f / MAX_RPM) * RPM_SWEEP
                val warnBreak = (6500f / MAX_RPM) * RPM_SWEEP

                fun drawZone(color: androidx.compose.ui.graphics.Color, start: Float, sweep: Float) {
                    if (sweep > 0f) drawArc(
                        color = color,
                        startAngle = RPM_START_ANGLE + start,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcRect,
                        style = Stroke(width = ringStroke, cap = StrokeCap.Butt)
                    )
                }

                when {
                    activeSweep <= normalBreak -> drawZone(GaugeNormal, 0f, activeSweep)
                    activeSweep <= warnBreak -> {
                        drawZone(GaugeNormal, 0f, normalBreak)
                        drawZone(GaugeWarning, normalBreak, activeSweep - normalBreak)
                    }
                    else -> {
                        drawZone(GaugeNormal, 0f, normalBreak)
                        drawZone(GaugeWarning, normalBreak, warnBreak - normalBreak)
                        drawZone(GaugeDanger, warnBreak, activeSweep - warnBreak)
                    }
                }
            }

            // Redline indicator (always visible at 6500+ RPM zone)
            val redlineStart = (6500f / MAX_RPM) * RPM_SWEEP
            drawArc(
                color = GaugeDanger.copy(alpha = 0.3f),
                startAngle = RPM_START_ANGLE + redlineStart,
                sweepAngle = RPM_SWEEP - redlineStart,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcRect,
                style = Stroke(width = ringStroke * 0.4f, cap = StrokeCap.Butt)
            )

            // Tick marks
            val tickOuterR = radius - halfStroke - 2.dp.toPx()
            val labelPaint = NativePaint().apply {
                isAntiAlias = true
                color = GaugeTickMajor.toArgb()
                textSize = min(w, h) * 0.038f
                textAlign = NativePaint.Align.CENTER
            }

            for (i in 0..8) {
                val tickFraction = i / 8f
                val angleRad = Math.toRadians((RPM_START_ANGLE + tickFraction * RPM_SWEEP).toDouble())
                val cosA = cos(angleRad).toFloat()
                val sinA = sin(angleRad).toFloat()

                val tickLen = min(w, h) * 0.050f
                val outer = Offset(cx + tickOuterR * cosA, cy + tickOuterR * sinA)
                val inner = Offset(cx + (tickOuterR - tickLen) * cosA, cy + (tickOuterR - tickLen) * sinA)
                drawLine(GaugeTickMajor, outer, inner, strokeWidth = 2f)

                val labelR = tickOuterR - tickLen - min(w, h) * 0.042f
                val lx = cx + labelR * cosA
                val ly = cy + labelR * sinA + labelPaint.textSize * 0.38f
                drawContext.canvas.nativeCanvas.drawText(i.toString(), lx, ly, labelPaint)
            }

            // Minor ticks
            for (i in 0..16) {
                if (i % 2 == 0) continue
                val tickFraction = i / 16f
                val angleRad = Math.toRadians((RPM_START_ANGLE + tickFraction * RPM_SWEEP).toDouble())
                val cosA = cos(angleRad).toFloat()
                val sinA = sin(angleRad).toFloat()
                val tickLen = min(w, h) * 0.026f
                val outer = Offset(cx + tickOuterR * cosA, cy + tickOuterR * sinA)
                val inner = Offset(cx + (tickOuterR - tickLen) * cosA, cy + (tickOuterR - tickLen) * sinA)
                drawLine(GaugeTickMinor, outer, inner, strokeWidth = 1f)
            }

            // Needle
            val needleAngleRad = Math.toRadians((RPM_START_ANGLE + fraction * RPM_SWEEP).toDouble())
            val needleCos = cos(needleAngleRad).toFloat()
            val needleSin = sin(needleAngleRad).toFloat()
            val needleLen = radius - halfStroke - min(w, h) * 0.09f
            val needleTip = Offset(cx + needleLen * needleCos, cy + needleLen * needleSin)
            val needleBase = Offset(cx - min(w, h) * 0.035f * needleCos, cy - min(w, h) * 0.035f * needleSin)

            drawLine(
                color = GaugeNeedle.copy(alpha = 0.25f),
                start = needleBase, end = needleTip,
                strokeWidth = min(w, h) * 0.022f, cap = StrokeCap.Round
            )
            drawLine(
                color = GaugeNeedle,
                start = needleBase, end = needleTip,
                strokeWidth = min(w, h) * 0.007f, cap = StrokeCap.Round
            )

            val hubR = min(w, h) * 0.062f
            drawCircle(GaugeHub, radius = hubR, center = Offset(cx, cy))
            drawCircle(GaugeNeedle, radius = min(w, h) * 0.020f, center = Offset(cx, cy))
            drawCircle(AutomotiveBackground, radius = min(w, h) * 0.009f, center = Offset(cx, cy))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = 20.dp)
        ) {
            Text(
                text = "%.1f".format(rpm / 1000f),
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Thin,
                    color = AutomotiveText,
                    letterSpacing = (-1).sp
                )
            )
            Text(
                text = "×1000 RPM",
                style = TextStyle(
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    color = AutomotiveTextMuted,
                    letterSpacing = 1.5.sp
                )
            )
        }
    }
}