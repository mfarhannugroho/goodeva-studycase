package com.goodeva.bletracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goodeva.bletracker.domain.model.BleDevice
import com.goodeva.bletracker.domain.model.ProximityZone

@Entity(tableName = "device_history")
data class DeviceHistoryEntity(
    @PrimaryKey
    val macAddress: String,
    val name: String?,
    val lastRssi: Int,
    val highestRssi: Int,
    val txPower: Int?,
    val estimatedDistance: Double,
    val lastSeenTimestamp: Long
) {
    fun toDomain(): BleDevice {
        return BleDevice(
            macAddress = macAddress,
            name = name,
            rssi = lastRssi,
            txPower = txPower,
            estimatedDistance = estimatedDistance,
            proximityZone = ProximityZone.fromRssi(lastRssi),
            lastSeenTimestamp = lastSeenTimestamp
        )
    }

    companion object {
        fun fromDomain(device: BleDevice, existingHighestRssi: Int? = null): DeviceHistoryEntity {
            val highest = when {
                existingHighestRssi == null -> device.rssi
                device.rssi > existingHighestRssi -> device.rssi
                else -> existingHighestRssi
            }
            return DeviceHistoryEntity(
                macAddress = device.macAddress,
                name = device.name,
                lastRssi = device.rssi,
                highestRssi = highest,
                txPower = device.txPower,
                estimatedDistance = device.estimatedDistance,
                lastSeenTimestamp = device.lastSeenTimestamp
            )
        }
    }
}
