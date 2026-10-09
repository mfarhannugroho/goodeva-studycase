package com.goodeva.bletracker.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import com.goodeva.bletracker.domain.model.BleDevice
import com.goodeva.bletracker.domain.util.DistanceCalculator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleScannerDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager?.adapter

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun scanDevices(): Flow<BleDevice> = callbackFlow {
        val adapter = bluetoothAdapter
        val scanner = adapter?.bluetoothLeScanner

        if (adapter == null || !adapter.isEnabled || scanner == null) {
            close(IllegalStateException("Bluetooth tidak aktif atau tidak didukung pada perangkat ini"))
            return@callbackFlow
        }

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.let { scanResult ->
                    val device = scanResult.device ?: return
                    val scanRecord = scanResult.scanRecord

                    // Ekstraksi nama: prioritaskan record name (sering kali lebih lengkap)
                    val name = scanRecord?.deviceName
                        ?: try { device.name } catch (e: SecurityException) { null }

                    val rawRssi = scanResult.rssi
                    val txPower = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (scanResult.txPower != ScanResult.TX_POWER_NOT_PRESENT) {
                            scanResult.txPower
                        } else {
                            scanRecord?.txPowerLevel
                        }
                    } else {
                        scanRecord?.txPowerLevel
                    }

                    val bleDevice = BleDevice(
                        macAddress = device.address ?: "00:00:00:00:00:00",
                        name = name,
                        rssi = rawRssi,
                        txPower = txPower,
                        estimatedDistance = DistanceCalculator.calculateDistance(rawRssi, txPower),
                        lastSeenTimestamp = System.currentTimeMillis()
                    )

                    trySend(bleDevice)
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                results?.forEach { result ->
                    onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                close(RuntimeException("BLE Scan gagal dengan kode error: $errorCode"))
            }
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()

        try {
            scanner.startScan(null, settings, scanCallback)
        } catch (e: SecurityException) {
            close(e)
        } catch (e: Exception) {
            close(e)
        }

        awaitClose {
            try {
                scanner.stopScan(scanCallback)
            } catch (e: Exception) {
                // Ignore teardown errors
            }
        }
    }
}
