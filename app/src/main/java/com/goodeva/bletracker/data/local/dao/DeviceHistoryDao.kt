package com.goodeva.bletracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.goodeva.bletracker.data.local.entity.DeviceHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DeviceHistoryEntity)

    @Query("SELECT * FROM device_history ORDER BY lastSeenTimestamp DESC")
    fun getAllHistory(): Flow<List<DeviceHistoryEntity>>

    @Query("SELECT * FROM device_history WHERE macAddress = :macAddress LIMIT 1")
    suspend fun getByMac(macAddress: String): DeviceHistoryEntity?

    @Query("""
        SELECT * FROM device_history 
        WHERE (:query = '' OR name LIKE '%' || :query || '%' OR macAddress LIKE '%' || :query || '%')
        ORDER BY lastSeenTimestamp DESC
    """)
    suspend fun searchHistory(query: String): List<DeviceHistoryEntity>

    @Query("DELETE FROM device_history")
    suspend fun clearAll()
}
