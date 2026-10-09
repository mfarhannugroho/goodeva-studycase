package com.goodeva.bletracker.data.repository

import com.goodeva.bletracker.data.local.dao.DeviceHistoryDao
import com.goodeva.bletracker.data.local.entity.DeviceHistoryEntity
import com.goodeva.bletracker.domain.model.BleDevice
import com.goodeva.bletracker.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: DeviceHistoryDao
) : HistoryRepository {

    override fun getAllHistory(): Flow<List<BleDevice>> {
        return historyDao.getAllHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveDevice(device: BleDevice) {
        val existing = historyDao.getByMac(device.macAddress)
        val entity = DeviceHistoryEntity.fromDomain(device, existing?.highestRssi)
        historyDao.upsert(entity)
    }

    override suspend fun searchHistory(query: String): List<BleDevice> {
        return historyDao.searchHistory(query.trim()).map { it.toDomain() }
    }

    override suspend fun clearHistory() {
        historyDao.clearAll()
    }
}
