package com.goodeva.bletracker.domain.repository

import com.goodeva.bletracker.domain.model.BleDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BleRepository {
    val isScanning: StateFlow<Boolean>
    val isSimulationMode: StateFlow<Boolean>

    fun startScan(): Flow<List<BleDevice>>
    fun stopScan()
    fun trackDevice(macAddress: String): Flow<BleDevice?>
    fun setSimulationMode(enabled: Boolean)
}
