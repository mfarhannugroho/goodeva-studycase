package com.goodeva.bletracker.presentation.radar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodeva.bletracker.domain.model.BleDevice
import com.goodeva.bletracker.domain.model.ProximityZone
import com.goodeva.bletracker.domain.repository.BleRepository
import com.goodeva.bletracker.domain.util.DistanceCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

data class RadarUiState(
    val targetMac: String = "",
    val device: BleDevice? = null,
    val smoothedRssi: Double = -70.0,
    val smoothedDistance: Double = 5.0,
    val proximityZone: ProximityZone = ProximityZone.MODERATE,
    val rssiHistory: List<Int> = emptyList(), // Riwayat sinyal untuk grafik mini tren
    val isSignalLost: Boolean = false,
    val lastPacketReceivedMs: Long = System.currentTimeMillis()
)

@HiltViewModel
class RadarViewModel @Inject constructor(
    private val bleRepository: BleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val targetMacAddress: String = checkNotNull(savedStateHandle["macAddress"])

    private val _uiState = MutableStateFlow(RadarUiState(targetMac = targetMacAddress))
    val uiState: StateFlow<RadarUiState> = _uiState.asStateFlow()

    private val historyPoints = mutableListOf<Int>()
    private val maxHistoryPoints = 30
    private var trackingJob: Job? = null
    private var signalTimeoutJob: Job? = null

    // Faktor smoothing EMA (0.35 = keseimbangan responsif dan stabilitas visual)
    private val alpha = 0.35

    init {
        startTracking()
    }

    private fun startTracking() {
        // Pastikan scanner aktif saat membuka radar view
        if (!bleRepository.isScanning.value) {
            bleRepository.startScan()
        }

        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            bleRepository.trackDevice(targetMacAddress).collect { updatedDevice ->
                if (updatedDevice != null) {
                    processDeviceUpdate(updatedDevice)
                }
            }
        }

        // Timer pengecekan sinyal hilang (jika paket tidak masuk > 10 detik)
        signalTimeoutJob?.cancel()
        signalTimeoutJob = viewModelScope.launch {
            while (true) {
                delay(3000)
                val timeSinceLast = System.currentTimeMillis() - _uiState.value.lastPacketReceivedMs
                if (timeSinceLast > 10_000 && !_uiState.value.isSignalLost) {
                    _uiState.value = _uiState.value.copy(
                        isSignalLost = true,
                        proximityZone = ProximityZone.LOST
                    )
                }
            }
        }
    }

    private fun processDeviceUpdate(newDevice: BleDevice) {
        val currentState = _uiState.value
        val rawRssi = newDevice.rssi

        // Update riwayat sinyal sparkline
        historyPoints.add(rawRssi)
        if (historyPoints.size > maxHistoryPoints) {
            historyPoints.removeAt(0)
        }

        // Terapkan filter eksponensial (EMA) untuk pergerakan blip radar yang mulus
        val newSmoothedRssi = if (currentState.device == null) {
            rawRssi.toDouble()
        } else {
            alpha * rawRssi + (1.0 - alpha) * currentState.smoothedRssi
        }

        val smoothedDistance = DistanceCalculator.calculateDistance(
            newSmoothedRssi.roundToInt(),
            newDevice.txPower
        )

        val zone = ProximityZone.fromRssi(rawRssi)

        _uiState.value = RadarUiState(
            targetMac = targetMacAddress,
            device = newDevice,
            smoothedRssi = newSmoothedRssi,
            smoothedDistance = smoothedDistance,
            proximityZone = zone,
            rssiHistory = historyPoints.toList(),
            isSignalLost = false,
            lastPacketReceivedMs = System.currentTimeMillis()
        )
    }

    override fun onCleared() {
        super.onCleared()
        trackingJob?.cancel()
        signalTimeoutJob?.cancel()
    }
}
