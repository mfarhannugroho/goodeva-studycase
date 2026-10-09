package com.goodeva.bletracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.goodeva.bletracker.data.local.dao.DeviceHistoryDao
import com.goodeva.bletracker.data.local.entity.DeviceHistoryEntity

@Database(
    entities = [DeviceHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceHistoryDao(): DeviceHistoryDao
}
