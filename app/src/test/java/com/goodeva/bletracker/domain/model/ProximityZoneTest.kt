package com.goodeva.bletracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ProximityZoneTest {

    @Test
    fun `test mapping RSSI to ProximityZone matching study case specifications`() {
        // -10 s/d -30 dBm: Sangat Kuat (Sangat Dekat) < 1 meter
        assertEquals(ProximityZone.VERY_STRONG, ProximityZone.fromRssi(-10))
        assertEquals(ProximityZone.VERY_STRONG, ProximityZone.fromRssi(-25))
        assertEquals(ProximityZone.VERY_STRONG, ProximityZone.fromRssi(-30))
        assertEquals("< 1 meter", ProximityZone.VERY_STRONG.distanceDescription)
        assertEquals("Sangat Kuat (Sangat Dekat)", ProximityZone.VERY_STRONG.categoryName)

        // -31 s/d -50 dBm: Kuat (Dekat) 1 – 3 meter
        assertEquals(ProximityZone.STRONG, ProximityZone.fromRssi(-31))
        assertEquals(ProximityZone.STRONG, ProximityZone.fromRssi(-45))
        assertEquals(ProximityZone.STRONG, ProximityZone.fromRssi(-50))
        assertEquals("1 – 3 meter", ProximityZone.STRONG.distanceDescription)

        // -51 s/d -70 dBm: Cukup / Baik 3 – 10 meter
        assertEquals(ProximityZone.MODERATE, ProximityZone.fromRssi(-51))
        assertEquals(ProximityZone.MODERATE, ProximityZone.fromRssi(-65))
        assertEquals(ProximityZone.MODERATE, ProximityZone.fromRssi(-70))
        assertEquals("3 – 10 meter", ProximityZone.MODERATE.distanceDescription)

        // -71 s/d -80 dBm: Lemah 10 – 20 meter
        assertEquals(ProximityZone.WEAK, ProximityZone.fromRssi(-71))
        assertEquals(ProximityZone.WEAK, ProximityZone.fromRssi(-75))
        assertEquals(ProximityZone.WEAK, ProximityZone.fromRssi(-80))
        assertEquals("10 – 20 meter", ProximityZone.WEAK.distanceDescription)

        // -81 s/d -90 dBm: Sangat Lemah / Putus-putus > 20 meter
        assertEquals(ProximityZone.VERY_WEAK, ProximityZone.fromRssi(-81))
        assertEquals(ProximityZone.VERY_WEAK, ProximityZone.fromRssi(-88))
        assertEquals(ProximityZone.VERY_WEAK, ProximityZone.fromRssi(-90))
        assertEquals("> 20 meter (Batas jangkauan)", ProximityZone.VERY_WEAK.distanceDescription)

        // < -90 dBm: Sinyal Hilang (Lost)
        assertEquals(ProximityZone.LOST, ProximityZone.fromRssi(-91))
        assertEquals(ProximityZone.LOST, ProximityZone.fromRssi(-105))
        assertEquals("Terputus / Di luar jangkauan", ProximityZone.LOST.distanceDescription)
        assertEquals("Sinyal Hilang (Lost)", ProximityZone.LOST.categoryName)
    }
}
