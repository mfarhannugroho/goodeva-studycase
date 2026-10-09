package com.goodeva.bletracker.domain.repository

import com.goodeva.bletracker.domain.model.BleDevice
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getAllHistory(): Flow<List<BleDevice>>
    suspend fun saveDevice(device: BleDevice)
    suspend fun searchHistory(query: String): List<BleDevice>
    suspend fun clearHistory()
}
