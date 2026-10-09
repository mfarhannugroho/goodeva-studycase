package com.goodeva.bletracker.domain.model

import androidx.compose.ui.graphics.Color

enum class ProximityZone(
    val categoryName: String,
    val distanceDescription: String,
    val minRssi: Int,
    val maxRssi: Int,
    val colorHex: Long,
    val color: Color
) {
    VERY_STRONG(
        categoryName = "Sangat Kuat (Sangat Dekat)",
        distanceDescription = "< 1 meter",
        minRssi = -30,
        maxRssi = -10,
        colorHex = 0xFF00E676,
        color = Color(0xFF00E676) // Emerald Green
    ),
    STRONG(
        categoryName = "Kuat (Dekat)",
        distanceDescription = "1 – 3 meter",
        minRssi = -50,
        maxRssi = -31,
        colorHex = 0xFF00E5FF,
        color = Color(0xFF00E5FF) // Electric Cyan
    ),
    MODERATE(
        categoryName = "Cukup / Baik",
        distanceDescription = "3 – 10 meter",
        minRssi = -70,
        maxRssi = -51,
        colorHex = 0xFFFFD600,
        color = Color(0xFFFFD600) // Vibrant Amber/Yellow
    ),
    WEAK(
        categoryName = "Lemah",
        distanceDescription = "10 – 20 meter",
        minRssi = -80,
        maxRssi = -71,
        colorHex = 0xFFFF9100,
        color = Color(0xFFFF9100) // Deep Orange
    ),
    VERY_WEAK(
        categoryName = "Sangat Lemah / Putus-putus",
        distanceDescription = "> 20 meter (Batas jangkauan)",
        minRssi = -90,
        maxRssi = -81,
        colorHex = 0xFFFF3D00,
        color = Color(0xFFFF3D00) // Crimson Red
    ),
    LOST(
        categoryName = "Sinyal Hilang (Lost)",
        distanceDescription = "Terputus / Di luar jangkauan",
        minRssi = Int.MIN_VALUE,
        maxRssi = -91,
        colorHex = 0xFF757575,
        color = Color(0xFF757575) // Muted Gray
    );

    companion object {
        fun fromRssi(rssi: Int): ProximityZone {
            return when {
                rssi >= -30 -> VERY_STRONG
                rssi >= -50 -> STRONG
                rssi >= -70 -> MODERATE
                rssi >= -80 -> WEAK
                rssi >= -90 -> VERY_WEAK
                else -> LOST
            }
        }
    }
}
