package com.rakibul.hmidashboard.model

data class VehicleState(
    val speedKmh: Float = 0f,
    val rpm: Float = 800f,
    val batteryPercent: Float = 85f,
    val engineTempCelsius: Float = 85f,
    val fuelPercent: Float = 65f,
    val gForceX: Float = 0f,
    val gForceY: Float = 0f,
    val gForceZ: Float = 9.8f,
    val gpsSpeedKmh: Float = -1f,
    val gear: String = "P",
    val isDrivingMode: Boolean = false,
    val odometerKm: Float = 12450f,
    val tripKm: Float = 0f,
    val alerts: List<String> = emptyList(),
    val isGpsActive: Boolean = false
)