package com.example.safepiconnect

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Parcelable
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.safepiconnect.databinding.ActivityScannerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator

class ScannerActivity : AppCompatActivity() {

    private lateinit var bleScanner: BleScanner
    private lateinit var listViewAdapter: ArrayAdapter<String>
    private val discoveredDevices = mutableListOf<String>()
    private lateinit var binding: ActivityScannerBinding
    private val aggregator = BleScanResultAggregator()
    private var bleDevices: List<ServerDevice> = listOf()
    private var scanJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Rest of your initialization code
        bleScanner = BleScanner(this)
        listViewAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, discoveredDevices)
        binding.listViewDevices.adapter = listViewAdapter  // Access ListView via binding

        startBleScan()
        binding.listViewDevices.setOnItemClickListener { _, _, position, _ ->
            val selectedDevice = bleDevices[position]
            val intent = Intent(this, DeviceActivity::class.java).apply {
                putExtra("SELECTED_DEVICE", selectedDevice as Parcelable)
            }
            // stop scanner when entering here
            scanJob?.cancel()
            startActivity(intent)
        }
    }

    private fun startBleScan() {
        lifecycleScope.launch {
            if (ActivityCompat.checkSelfPermission(
                    this@ScannerActivity,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this@ScannerActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@ScannerActivity,
                    arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT),
                    REQUEST_CODE_PERMISSIONS
                )
                return@launch
            }

            bleScanner.scan()
                .map { scanResult ->
                    aggregator.aggregateDevices(scanResult)
                }
                .onEach { devices ->
                    bleDevices = devices // Assigning the list of devices to bleDevices
                    withContext(Dispatchers.Main) {
                        discoveredDevices.clear() // Clear the discoveredDevices list
                        discoveredDevices.addAll(devices.map { device ->
                            val deviceName = device.name ?: "Unknown Device"
                            "$deviceName - ${device.address}"
                        }) // Add all devices from devices directly
                        listViewAdapter.notifyDataSetChanged() // Notify the adapter of the data set change
                    }
                }
                .launchIn(lifecycleScope) // Launch the flow in the lifecycle scope
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, start BLE scanning
                startBleScan()
            } else {
                // Permissions not granted
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scanJob?.cancel()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 100
    }

}
