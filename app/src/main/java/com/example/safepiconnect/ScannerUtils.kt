package com.example.safepiconnect

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator

class ScannerUtils {
    private val aggregator = BleScanResultAggregator()
    private lateinit var bleScanner: BleScanner
    private val deviceChannel = Channel<ServerDevice>(Channel.CONFLATED)
    private var scanJob: Job? = null

    fun startBleScan(context: Context, scope: CoroutineScope) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission denied for BLE scan")
            return  // Early return if permissions are not granted
        }

        bleScanner = BleScanner(context)
        scanJob = scope.launch {
            bleScanner.scan()
                .map { scanResult -> aggregator.aggregateDevices(scanResult) }
                .collect { devices ->
                    devices.forEach { device ->
                        deviceChannel.send(device)
                    }
                }
        }
    }

    suspend fun searchDevices(name: String? = null, macRange: String? = null): ServerDevice? {
        var foundDevice: ServerDevice? = null
        for (device in deviceChannel) {
            val matchesName = name?.let { device.name.equals(it, ignoreCase = true) } ?: true
            val matchesMac = macRange?.let { device.address.startsWith(it, ignoreCase = true) } ?: true
            if (matchesName || matchesMac) {
                foundDevice = device
                Log.d(TAG, "Desired device found: ${device.name} with MAC: ${device.address}")
                break
            }
        }
        return foundDevice
    }

    fun stopBleScan() {
        scanJob?.cancel()
        scanJob = null
    }
}
