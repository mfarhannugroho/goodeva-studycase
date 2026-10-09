package com.goodeva.bletracker.presentation

import com.goodeva.bletracker.domain.model.BleDevice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScannerFilterTest {

    private val device1 = BleDevice("AA:BB:CC:DD:EE:01", "Earbuds Pro", -35)
    private val device2 = BleDevice("AA:BB:CC:DD:EE:02", "Smart Watch", -75)
    private val device3 = BleDevice("AA:BB:CC:DD:EE:03", null, -60)
    private val device4 = BleDevice("11:22:33:44:55:66", "Beacon Sensor", -20)
    private val testList = listOf(device1, device2, device3, device4)

    @Test
    fun `test sorting by strongest signal (RSSI highest first)`() {
        val sorted = testList.sortedByDescending { it.rssi }
        assertEquals(device4, sorted[0]) // -20 dBm
        assertEquals(device1, sorted[1]) // -35 dBm
        assertEquals(device3, sorted[2]) // -60 dBm
        assertEquals(device2, sorted[3]) // -75 dBm
    }

    @Test
    fun `test filter by RSSI threshold minimum -50 dBm`() {
        val threshold = -50
        val filtered = testList.filter { it.rssi >= threshold }
        assertEquals(2, filtered.size)
        assertTrue(filtered.contains(device1))
        assertTrue(filtered.contains(device4))
    }

    @Test
    fun `test search filter by name and MAC address`() {
        val searchByName = testList.filter {
            (it.name?.lowercase()?.contains("watch") == true) || it.macAddress.lowercase().contains("watch")
        }
        assertEquals(1, searchByName.size)
        assertEquals("Smart Watch", searchByName[0].name)

        val searchByMac = testList.filter {
            (it.name?.lowercase()?.contains("11:22") == true) || it.macAddress.lowercase().contains("11:22")
        }
        assertEquals(1, searchByMac.size)
        assertEquals(device4.macAddress, searchByMac[0].macAddress)
    }

    @Test
    fun `test hide unnamed devices filter`() {
        val namedOnly = testList.filter { !it.name.isNullOrBlank() }
        assertEquals(3, namedOnly.size)
        assertTrue(namedOnly.none { it.name == null })
    }
}
