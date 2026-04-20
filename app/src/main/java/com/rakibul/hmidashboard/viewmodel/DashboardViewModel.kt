package com.rakibul.hmidashboard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.rakibul.hmidashboard.sensors.SensorRepository
import kotlinx.coroutines.flow.StateFlow
import com.rakibul.hmidashboard.model.VehicleState

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorRepository = SensorRepository(application)
    val vehicleState: StateFlow<VehicleState> = sensorRepository.vehicleState

    init {
        sensorRepository.startSimulation()
        sensorRepository.startAccelerometer()
    }

    fun onLocationPermissionGranted() {
        sensorRepository.startGps()
    }

    override fun onCleared() {
        super.onCleared()
        sensorRepository.stop()
    }
}