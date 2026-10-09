package com.goodeva.bletracker.domain.model

import com.goodeva.bletracker.domain.util.DistanceCalculator

data class BleDevice(
    val macAddress: String,
    val name: String?,
    val rssi: Int,
    val txPower: Int? = null,
    val estimatedDistance: Double = DistanceCalculator.calculateDistance(rssi, txPower),
    val proximityZone: ProximityZone = ProximityZone.fromRssi(rssi),
    val lastSeenTimestamp: Long = System.currentTimeMillis()
) {
    val displayName: String
        get() = if (!name.isNullOrBlank()) name else "Unnamed BLE Device"

    val formattedDistance: String
        get() = DistanceCalculator.formatDistance(estimatedDistance)
}
