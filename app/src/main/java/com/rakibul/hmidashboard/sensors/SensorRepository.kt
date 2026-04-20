package com.rakibul.hmidashboard.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.rakibul.hmidashboard.model.VehicleState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SensorRepository(private val context: Context) {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _vehicleState = MutableStateFlow(VehicleState())
    val vehicleState: StateFlow<VehicleState> = _vehicleState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var simulationJob: Job? = null

    private val accelerometerListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            _vehicleState.update {
                it.copy(
                    gForceX = event.values[0],
                    gForceY = event.values[1],
                    gForceZ = event.values[2]
                )
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val gpsSpeed = if (location.hasSpeed()) location.speed * 3.6f else -1f
            _vehicleState.update { it.copy(gpsSpeedKmh = gpsSpeed, isGpsActive = gpsSpeed >= 0f) }
        }
        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    fun startSimulation() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            var simSpeed = 0f
            var increasing = true
            var engineTemp = 85f
            var batteryLevel = 85f
            var fuelLevel = 65f
            var odometer = 12450f
            var tripKm = 0f

            while (isActive) {
                // Realistic speed curve with smooth acceleration/braking
                val accelRate = (1.2f - simSpeed * 0.006f).coerceAtLeast(0.15f)
                val brakeRate = (0.8f + simSpeed * 0.004f).coerceAtLeast(0.2f)

                if (increasing) {
                    simSpeed += accelRate
                    if (simSpeed >= 130f) increasing = false
                } else {
                    simSpeed -= brakeRate
                    if (simSpeed <= 5f) increasing = true
                }
                simSpeed = simSpeed.coerceIn(0f, 240f)

                // RPM follows speed with noise
                val baseRpm = 800f + (simSpeed / 130f) * 5200f
                val rpmNoise = ((Math.random() - 0.5) * 150).toFloat()
                val rpm = (baseRpm + rpmNoise).coerceIn(700f, 8000f)

                // Temperature: slow warm-up, slight rise at high speed
                if (engineTemp < 90f) engineTemp += 0.015f
                if (simSpeed > 110f && engineTemp < 105f) engineTemp += 0.008f
                if (simSpeed < 30f && engineTemp > 85f) engineTemp -= 0.005f

                // Battery and fuel: very slow drain
                batteryLevel = (batteryLevel - 0.00008f).coerceAtLeast(10f)
                fuelLevel = (fuelLevel - 0.0001f).coerceAtLeast(0f)

                // Odometer ticks with simulated distance
                val distanceIncrement = simSpeed / (3600f * 20f) // per 50ms
                odometer += distanceIncrement
                tripKm += distanceIncrement

                val gear = when {
                    simSpeed < 1f -> "P"
                    simSpeed < 20f -> "1"
                    simSpeed < 40f -> "2"
                    simSpeed < 65f -> "3"
                    simSpeed < 95f -> "4"
                    simSpeed < 120f -> "5"
                    else -> "6"
                }

                val alerts = buildList {
                    if (engineTemp > 100f) add("HIGH TEMP")
                    if (batteryLevel < 20f) add("LOW BATTERY")
                    if (fuelLevel < 10f) add("LOW FUEL")
                }

                _vehicleState.update { state ->
                    state.copy(
                        speedKmh = simSpeed,
                        rpm = rpm,
                        engineTempCelsius = engineTemp,
                        batteryPercent = batteryLevel,
                        fuelPercent = fuelLevel,
                        gear = gear,
                        odometerKm = odometer,
                        tripKm = tripKm,
                        alerts = alerts,
                        isDrivingMode = simSpeed > 2f
                    )
                }

                delay(50L) // 20Hz simulation tick
            }
        }
    }

    fun startAccelerometer() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sensorManager.registerListener(
                accelerometerListener,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    @Suppress("MissingPermission")
    fun startGps() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    0f,
                    locationListener
                )
            }
        } catch (_: Exception) {}
    }

    fun stop() {
        simulationJob?.cancel()
        scope.cancel()
        sensorManager.unregisterListener(accelerometerListener)
        try { locationManager.removeUpdates(locationListener) } catch (_: Exception) {}
    }
}