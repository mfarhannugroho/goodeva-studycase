package com.goodeva.bletracker.domain.util

import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

object DistanceCalculator {

    private const val DEFAULT_TX_POWER = -59 // Standar RSSI pada jarak 1 meter
    private const val PATH_LOSS_EXPONENT = 2.2 // Eksponen propagasi sinyal RF dalam ruangan

    /**
     * Menghitung estimasi jarak Bluetooth dalam meter berdasarkan dBm
     * Menggunakan model Log-distance path loss:
     * Distance = 10 ^ ((TxPower - RSSI) / (10 * n))
     */
    fun calculateDistance(rssi: Int, txPower: Int? = null): Double {
        if (rssi == 0 || rssi <= -100) {
            return -1.0 // Sinyal hilang / di luar jangkauan
        }

        val effectiveTxPower = if (txPower != null && txPower in -100..-20) {
            txPower
        } else {
            DEFAULT_TX_POWER
        }

        val ratio = (effectiveTxPower - rssi).toDouble() / (10.0 * PATH_LOSS_EXPONENT)
        val distance = 10.0.pow(ratio)

        // Pembulatan ke 2 desimal
        return (distance * 100).roundToInt() / 100.0
    }

    /**
     * Memformat angka jarak menjadi teks yang mudah dibaca pengguna
     */
    fun formatDistance(distanceInMeters: Double): String {
        return when {
            distanceInMeters < 0 -> "N/A"
            distanceInMeters < 1.0 -> String.format(Locale.US, "%.1f m", distanceInMeters)
            distanceInMeters < 10.0 -> String.format(Locale.US, "%.1f m", distanceInMeters)
            else -> String.format(Locale.US, "%.0f m", distanceInMeters)
        }
    }
}
