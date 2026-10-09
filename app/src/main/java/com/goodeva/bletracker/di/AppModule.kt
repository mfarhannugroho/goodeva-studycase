package com.goodeva.bletracker.di

import android.content.Context
import androidx.room.Room
import com.goodeva.bletracker.data.local.AppDatabase
import com.goodeva.bletracker.data.local.dao.DeviceHistoryDao
import com.goodeva.bletracker.data.repository.BleRepositoryImpl
import com.goodeva.bletracker.data.repository.HistoryRepositoryImpl
import com.goodeva.bletracker.domain.repository.BleRepository
import com.goodeva.bletracker.domain.repository.HistoryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ble_tracker_database.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideDeviceHistoryDao(database: AppDatabase): DeviceHistoryDao {
        return database.deviceHistoryDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        impl: HistoryRepositoryImpl
    ): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindBleRepository(
        impl: BleRepositoryImpl
    ): BleRepository
}
