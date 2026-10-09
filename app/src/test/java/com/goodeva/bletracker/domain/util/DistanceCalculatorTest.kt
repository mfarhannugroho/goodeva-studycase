package com.goodeva.bletracker.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceCalculatorTest {

    @Test
    fun `test distance calculation with standard TxPower -59 dBm`() {
        // Pada RSSI sama dengan TxPower (-59 dBm), jarak ideal adalah 1.0 meter
        val d1 = DistanceCalculator.calculateDistance(-59, -59)
        assertEquals(1.0, d1, 0.05)

        // Pada sinyal lebih kuat dari TxPower (-30 dBm), jarak harus < 1.0 meter
        val dClose = DistanceCalculator.calculateDistance(-30, -59)
        assertTrue(dClose < 1.0)
        assertTrue(dClose > 0.0)

        // Pada sinyal lebih lemah (-80 dBm), jarak harus bertambah (> 8 meter)
        val dFar = DistanceCalculator.calculateDistance(-80, -59)
        assertTrue(dFar > 5.0)

        // Edge case sinyal hilang
        val dLost = DistanceCalculator.calculateDistance(-105, -59)
        assertEquals(-1.0, dLost, 0.0)
    }

    @Test
    fun `test format distance string helper`() {
        assertEquals("0.5 m", DistanceCalculator.formatDistance(0.52))
        assertEquals("2.4 m", DistanceCalculator.formatDistance(2.41))
        assertEquals("15 m", DistanceCalculator.formatDistance(15.2))
        assertEquals("N/A", DistanceCalculator.formatDistance(-1.0))
    }
}
