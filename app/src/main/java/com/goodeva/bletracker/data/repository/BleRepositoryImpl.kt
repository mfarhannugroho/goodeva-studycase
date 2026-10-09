package com.goodeva.bletracker.data.repository

import com.goodeva.bletracker.data.ble.BleScannerDataSource
import com.goodeva.bletracker.data.ble.MockBleScannerDataSource
import com.goodeva.bletracker.domain.model.BleDevice
import com.goodeva.bletracker.domain.repository.BleRepository
import com.goodeva.bletracker.domain.repository.HistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleRepositoryImpl @Inject constructor(
    private val bleScannerDataSource: BleScannerDataSource,
    private val mockBleScannerDataSource: MockBleScannerDataSource,
    private val historyRepository: HistoryRepository
) : BleRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isSimulationMode = MutableStateFlow(false)
    override val isSimulationMode: StateFlow<Boolean> = _isSimulationMode.asStateFlow()

    private val devicesMap = ConcurrentHashMap<String, BleDevice>()
    private val _devicesList = MutableStateFlow<List<BleDevice>>(emptyList())

    private var scanJob: Job? = null

    override fun startScan(): Flow<List<BleDevice>> {
        if (_isScanning.value) {
            return _devicesList.asStateFlow()
        }

        _isScanning.value = true
        devicesMap.clear()
        _devicesList.value = emptyList()

        val sourceFlow = if (_isSimulationMode.value) {
            mockBleScannerDataSource.scanDevices()
        } else {
            bleScannerDataSource.scanDevices()
        }

        scanJob?.cancel()
        scanJob = repositoryScope.launch {
            sourceFlow
                .catch { error ->
                    _isScanning.value = false
                    // Jika gagal pada hardware scan (misal di emulator), fallback otomatis ke simulation
                    if (!_isSimulationMode.value && error is IllegalStateException) {
                        _isSimulationMode.value = true
                        startScan()
                    }
                }
                .collect { device ->
                    devicesMap[device.macAddress] = device
                    _devicesList.value = devicesMap.values.toList()

                    // Simpan otomatis ke database lokal (FR-4.1)
                    launch {
                        try {
                            historyRepository.saveDevice(device)
                        } catch (e: Exception) {
                            // Abaikan error background save
                        }
                    }
                }
        }

        return _devicesList.asStateFlow()
    }

    override fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        _isScanning.value = false
    }

    override fun trackDevice(macAddress: String): Flow<BleDevice?> {
        return _devicesList.map { list ->
            list.find { it.macAddress.equals(macAddress, ignoreCase = true) }
        }
    }

    override fun setSimulationMode(enabled: Boolean) {
        val changed = _isSimulationMode.value != enabled
        _isSimulationMode.value = enabled
        if (changed && _isScanning.value) {
            stopScan()
            startScan()
        }
    }
}
