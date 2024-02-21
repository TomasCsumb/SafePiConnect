package com.example.safepiconnect

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator

class ScannerUtils {
    private val aggregator = BleScanResultAggregator()
    private lateinit var bleScanner: BleScanner
    private var bleDevices: List<ServerDevice> = listOf()

    fun startBleScan(context: Context, scope: CoroutineScope): Job {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context,
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission denied for BLE scan")
            return Job()  // Return a empty job if permissions are not granted
        }

        bleScanner = BleScanner(context)
        return scope.launch {
            bleScanner.scan()
                .map { scanResult -> aggregator.aggregateDevices(scanResult) }
                .onEach { devices -> bleDevices = devices }
                .launchIn(this)
        }
    }

    suspend fun searchDevices(context: Context, scope: CoroutineScope, name: String?, macRange: String?): ServerDevice? {
        var foundDevice: ServerDevice? = null

        // Start the BLE scan
        val scanJob = startBleScan(context, scope)

        for (i in 1..20) {
            delay(1000)

            // Filter the list of devices for the desired device by name or MAC range
            foundDevice = if (!name.isNullOrEmpty()) {
                bleDevices.firstOrNull { device -> device.name.equals(name, ignoreCase = true) }
            } else if (!macRange.isNullOrEmpty()) {
                bleDevices.firstOrNull { device -> device.address.startsWith(macRange, ignoreCase = true) }
            } else {
                null
            }

            // If the desired device is found, break out of the loop
            if (foundDevice != null) {
                Log.d(TAG, "Desired device found: ${foundDevice!!.name}")
                break
            }
        }
        // Cancel scan job
        scanJob.cancel()
        return foundDevice
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 100
    }
}
