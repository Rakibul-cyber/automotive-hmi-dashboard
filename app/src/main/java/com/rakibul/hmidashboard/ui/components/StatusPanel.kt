package com.rakibul.hmidashboard.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakibul.hmidashboard.model.VehicleState
import com.rakibul.hmidashboard.ui.theme.*

@Composable
fun StatusPanel(
    state: VehicleState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Gear indicator
        GearDisplay(gear = state.gear)

        // Drive status pill
        DrivingModePill(isDriving = state.isDrivingMode)

        // Odometer and trip
        OdometerPanel(
            odometerKm = state.odometerKm,
            tripKm = state.tripKm
        )

        // Alert banners
        if (state.alerts.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                state.alerts.forEach { alert ->
                    AlertBanner(message = alert)
                }
            }
        }
    }
}

@Composable
private fun GearDisplay(gear: String) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AutomotiveSurfaceVariant)
            .border(1.dp, AutomotiveTextDim, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = gear,
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                color = AutomotivePrimary,
                letterSpacing = (-1).sp
            )
        )
    }
    Text(
        text = "GEAR",
        style = TextStyle(
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = AutomotiveTextMuted,
            letterSpacing = 2.sp
        )
    )
}

@Composable
private fun DrivingModePill(isDriving: Boolean) {
    val color = if (isDriving) AutomotiveSuccess else AutomotiveTextMuted
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
            Text(
                text = if (isDriving) "DRIVING" else "PARKED",
                style = TextStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = color,
                    letterSpacing = 2.sp
                )
            )
        }
    }
}

@Composable
private fun OdometerPanel(odometerKm: Float, tripKm: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OdometerRow(label = "ODO", value = "${"%.0f".format(odometerKm)} km")
        OdometerRow(label = "TRIP", value = "${"%.1f".format(tripKm)} km")
    }
}

@Composable
private fun OdometerRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(AutomotiveSurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
            text = value,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Light,
                color = AutomotiveText
            )
        )
    }
}

@Composable
private fun AlertBanner(message: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "alert_blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "alert_alpha"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(AutomotiveDanger.copy(alpha = 0.15f))
            .border(1.dp, AutomotiveDanger.copy(alpha = alpha * 0.8f), RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "⚠ $message",
            style = TextStyle(
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = AutomotiveDanger.copy(alpha = alpha),
                letterSpacing = 2.sp
            )
        )
    }
}