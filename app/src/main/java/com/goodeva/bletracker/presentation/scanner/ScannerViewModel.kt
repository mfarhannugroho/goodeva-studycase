package com.goodeva.bletracker.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodeva.bletracker.domain.model.BleDevice
import com.goodeva.bletracker.domain.repository.BleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DeviceSortOrder {
    STRONGEST_FIRST,
    NAME_ASC,
    RECENT_FIRST
}

data class ScannerUiState(
    val isScanning: Boolean = false,
    val isSimulationMode: Boolean = false,
    val searchQuery: String = "",
    val rssiThreshold: Int = -100, // Default -100 dBm (Semua perangkat)
    val hideUnnamed: Boolean = false,
    val sortOrder: DeviceSortOrder = DeviceSortOrder.STRONGEST_FIRST,
    val devices: List<BleDevice> = emptyList(),
    val totalDiscoveredCount: Int = 0,
    val errorMessage: String? = null
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val bleRepository: BleRepository
) : ViewModel() {

    private val _rawDevices = MutableStateFlow<List<BleDevice>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _rssiThreshold = MutableStateFlow(-100)
    private val _hideUnnamed = MutableStateFlow(false)
    private val _sortOrder = MutableStateFlow(DeviceSortOrder.STRONGEST_FIRST)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ScannerUiState> = combine(
        bleRepository.isScanning,
        bleRepository.isSimulationMode,
        _rawDevices,
        _searchQuery,
        _rssiThreshold,
        _hideUnnamed,
        _sortOrder,
        _errorMessage
    ) { params ->
        val isScanning = params[0] as Boolean
        val isSimulationMode = params[1] as Boolean
        @Suppress("UNCHECKED_CAST")
        val rawList = params[2] as List<BleDevice>
        val query = params[3] as String
        val threshold = params[4] as Int
        val hideUnnamed = params[5] as Boolean
        val sortOrder = params[6] as DeviceSortOrder
        val error = params[7] as String?

        // 1. Filter berdasarkan threshold RSSI
        val thresholdFiltered = rawList.filter { it.rssi >= threshold }

        // 2. Filter perangkat tanpa nama jika opsi aktif
        val unnamedFiltered = if (hideUnnamed) {
            thresholdFiltered.filter { !it.name.isNullOrBlank() }
        } else {
            thresholdFiltered
        }

        // 3. Filter berdasarkan query pencarian (Nama atau MAC)
        val queryFiltered = if (query.isBlank()) {
            unnamedFiltered
        } else {
            val q = query.trim().lowercase()
            unnamedFiltered.filter {
                (it.name?.lowercase()?.contains(q) == true) ||
                        it.macAddress.lowercase().contains(q)
            }
        }

        // 4. Pengurutan (Default: Sinyal Terkuat / RSSI tertinggi mendekati 0)
        val sortedList = when (sortOrder) {
            DeviceSortOrder.STRONGEST_FIRST -> queryFiltered.sortedByDescending { it.rssi }
            DeviceSortOrder.NAME_ASC -> queryFiltered.sortedBy { it.displayName.lowercase() }
            DeviceSortOrder.RECENT_FIRST -> queryFiltered.sortedByDescending { it.lastSeenTimestamp }
        }

        ScannerUiState(
            isScanning = isScanning,
            isSimulationMode = isSimulationMode,
            searchQuery = query,
            rssiThreshold = threshold,
            hideUnnamed = hideUnnamed,
            sortOrder = sortOrder,
            devices = sortedList,
            totalDiscoveredCount = rawList.size,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScannerUiState()
    )

    fun startScan() {
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                bleRepository.startScan().collect { list ->
                    _rawDevices.value = list
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Terjadi kesalahan saat memindai BLE"
            }
        }
    }

    fun stopScan() {
        bleRepository.stopScan()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onRssiThresholdChanged(threshold: Int) {
        _rssiThreshold.value = threshold
    }

    fun onToggleHideUnnamed(hide: Boolean) {
        _hideUnnamed.value = hide
    }

    fun onSortOrderChanged(sortOrder: DeviceSortOrder) {
        _sortOrder.value = sortOrder
    }

    fun toggleSimulationMode(enabled: Boolean) {
        bleRepository.setSimulationMode(enabled)
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
