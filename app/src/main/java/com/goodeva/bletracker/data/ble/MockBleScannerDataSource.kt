package com.goodeva.bletracker.data.ble

import com.goodeva.bletracker.domain.model.BleDevice
import com.goodeva.bletracker.domain.util.DistanceCalculator
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MockBleScannerDataSource @Inject constructor() {

    private data class MockTemplate(
        val macAddress: String,
        val name: String?,
        val baseRssi: Int,
        val txPower: Int
    )

    private val mockTemplates = listOf(
        MockTemplate("C8:2B:96:44:11:AB", "Wireless Earbuds Pro", -24, -59),
        MockTemplate("E4:5F:01:22:9A:1C", "Smart Watch Ultra", -38, -59),
        MockTemplate("FA:10:88:99:32:01", "BLE AirTag Tracker", -56, -59),
        MockTemplate("D0:03:4F:61:A4:E8", "Smart Home Temperature Sensor", -72, -62),
        MockTemplate("B2:44:90:1E:55:70", "Vehicle OBD2 Dongle", -84, -65),
        MockTemplate("AA:BB:CC:11:22:33", "Office Gateway Beacon #402", -92, -60),
        MockTemplate("02:88:1A:33:90:FF", null, -66, -59)
    )

    fun scanDevices(): Flow<BleDevice> = flow {
        // Emit perangkat awal
        mockTemplates.forEach { template ->
            emit(createDevice(template))
        }

        // Loop simulasi dengan fluktuasi sinyal dinamis
        while (currentCoroutineContext().isActive) {
            delay(1000)
            val selected = mockTemplates.random()
            emit(createDevice(selected))
        }
    }

    private fun createDevice(template: MockTemplate): BleDevice {
        // Jitter sinyal acak antara -4 s/d +4 dBm untuk simulasi pergerakan dinamis
        val jitter = Random.nextInt(-4, 5)
        val currentRssi = (template.baseRssi + jitter).coerceIn(-100, -10)
        return BleDevice(
            macAddress = template.macAddress,
            name = template.name,
            rssi = currentRssi,
            txPower = template.txPower,
            estimatedDistance = DistanceCalculator.calculateDistance(currentRssi, template.txPower),
            lastSeenTimestamp = System.currentTimeMillis()
        )
    }
}
