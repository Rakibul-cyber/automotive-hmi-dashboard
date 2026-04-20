package com.rakibul.hmidashboard.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibul.hmidashboard.model.VehicleState
import com.rakibul.hmidashboard.ui.components.*
import com.rakibul.hmidashboard.ui.theme.*
import com.rakibul.hmidashboard.viewmodel.DashboardViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.vehicleState.collectAsStateWithLifecycle()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AutomotiveBackground)
    ) {
        val isLandscape = maxWidth > maxHeight

        Column(modifier = Modifier.fillMaxSize()) {
            TopStatusBar(state = state)

            if (isLandscape) {
                LandscapeCluster(state = state, modifier = Modifier.weight(1f))
            } else {
                PortraitCluster(state = state, modifier = Modifier.weight(1f))
            }

            BottomMetricsStrip(state = state)
        }
    }
}

@Composable
private fun TopStatusBar(state: VehicleState) {
    val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AutomotiveSurface)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: GPS indicator + driving mode
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusDot(
                label = if (state.isGpsActive) "GPS" else "SIM",
                active = true,
                color = if (state.isGpsActive) AutomotiveSuccess else AutomotivePrimary
            )
            StatusDot(
                label = if (state.isDrivingMode) "DRIVE" else "PARK",
                active = state.isDrivingMode,
                color = if (state.isDrivingMode) AutomotiveSuccess else AutomotiveTextMuted
            )
        }

        // Center: App title
        Text(
            text = "HMI DASHBOARD",
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = AutomotiveTextMuted,
                letterSpacing = 4.sp
            )
        )

        // Right: Alert count + time
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.alerts.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AutomotiveDanger.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⚠ ${state.alerts.size}",
                        style = TextStyle(fontSize = 10.sp, color = AutomotiveDanger, fontWeight = FontWeight.Medium)
                    )
                }
            }
            Text(
                text = time,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    color = AutomotiveText,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

@Composable
private fun StatusDot(label: String, active: Boolean, color: androidx.compose.ui.graphics.Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(50))
                .background(if (active) color else AutomotiveTextDim)
        )
        Text(
            text = label,
            style = TextStyle(
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = if (active) color else AutomotiveTextDim,
                letterSpacing = 1.5.sp
            )
        )
    }
}

@Composable
private fun LandscapeCluster(state: VehicleState, modifier: Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Left: Speed gauge (largest)
        SpeedGauge(
            speedKmh = state.speedKmh,
            gpsSpeedKmh = state.gpsSpeedKmh,
            modifier = Modifier
                .weight(2.2f)
                .fillMaxHeight()
                .padding(12.dp)
        )

        // Center divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(AutomotiveTextDim.copy(alpha = 0.3f))
        )

        // Center: Status panel
        StatusPanel(
            state = state,
            modifier = Modifier
                .weight(1.4f)
                .fillMaxHeight()
                .padding(vertical = 16.dp),
        )

        // Center divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(AutomotiveTextDim.copy(alpha = 0.3f))
        )

        // Right: RPM + G-Force
        Column(
            modifier = Modifier
                .weight(1.8f)
                .fillMaxHeight()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            RpmGauge(
                rpm = state.rpm,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            GForceIndicator(
                gForceX = state.gForceX,
                gForceY = state.gForceY,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )
        }
    }
}

@Composable
private fun PortraitCluster(state: VehicleState, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Speed gauge (large, top)
        SpeedGauge(
            speedKmh = state.speedKmh,
            gpsSpeedKmh = state.gpsSpeedKmh,
            modifier = Modifier
                .fillMaxWidth()
                .weight(2.5f)
                .padding(16.dp)
        )

        // Horizontal divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AutomotiveTextDim.copy(alpha = 0.3f))
        )

        // Lower panel: RPM + Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.8f)
        ) {
            RpmGauge(
                rpm = state.rpm,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(AutomotiveTextDim.copy(alpha = 0.3f))
            )

            StatusPanel(
                state = state,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun BottomMetricsStrip(state: VehicleState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AutomotiveSurface)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            MetricBar(
                label = "BATTERY",
                value = state.batteryPercent,
                maxValue = 100f,
                displayText = "${"%.0f".format(state.batteryPercent)}%",
                barColor = BatteryGood,
                warnThreshold = 20f,
                dangerThreshold = 10f,
                modifier = Modifier.weight(1f)
            )
            MetricBar(
                label = "ENGINE",
                value = state.engineTempCelsius,
                maxValue = 120f,
                displayText = "${"%.0f".format(state.engineTempCelsius)}°C",
                barColor = TempNormal,
                warnThreshold = 95f,
                dangerThreshold = 105f,
                invertThresholds = true,
                modifier = Modifier.weight(1f)
            )
            MetricBar(
                label = "FUEL",
                value = state.fuelPercent,
                maxValue = 100f,
                displayText = "${"%.0f".format(state.fuelPercent)}%",
                barColor = FuelColor,
                warnThreshold = 15f,
                dangerThreshold = 5f,
                modifier = Modifier.weight(1f)
            )
        }
    }
}