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
import com.example.safepiconnect.databinding.ActivityDeviceListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nordicsemi.android.kotlin.ble.core.BleDevice
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator

class DeviceListActivity : AppCompatActivity() {

    private lateinit var bleScanner: BleScanner
    private lateinit var listViewAdapter: ArrayAdapter<String>
    private val discoveredDevices = mutableListOf<String>()
    private lateinit var binding: ActivityDeviceListBinding
    private val aggregator = BleScanResultAggregator()
    private var bleDevices: List<BleDevice> = listOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceListBinding.inflate(layoutInflater)
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
            startActivity(intent)
        }
    }


    private fun startBleScan() {
        // lifecycleScope added to stop this function when leaving activity
        lifecycleScope.launch {
            if (ActivityCompat.checkSelfPermission(
                    this@DeviceListActivity,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this@DeviceListActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@DeviceListActivity,
                    arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT),
                    REQUEST_CODE_PERMISSIONS
                )
                return@launch
            }
            // map used to create a list of devices and their properties
            // onEach used to update UI to display device info
            // launchIn used to handle stopping execution of scan
            bleScanner.scan()
                .map { aggregator.aggregateDevices(it) }
                .onEach { devices ->
                    val deviceList = devices.map { "${it.name ?: "Unknown Device"} - ${it.address}" }
                    bleDevices = devices;
                    withContext(Dispatchers.Main) {
                        discoveredDevices.clear()
                        discoveredDevices.addAll(deviceList)
                        listViewAdapter.notifyDataSetChanged()
                    }
                }
                .launchIn(lifecycleScope)
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

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 100
    }

}