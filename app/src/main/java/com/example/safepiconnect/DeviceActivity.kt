package com.example.safepiconnect

import android.os.Bundle
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.safepiconnect.databinding.ActivityDeviceBinding
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.BleDevice

class DeviceActivity : AppCompatActivity() {


    private lateinit var binding: ActivityDeviceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        binding = ActivityDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Warning Using deprecated method
        val device: BleDevice = intent.getParcelableExtra<BleDevice>("SELECTED_DEVICE")!! as BleDevice
        device?.let {
            // Assuming BleDevice has methods getName() and getAddress()
            val name = it.name ?: "Unknown Device"
            val address = it.address

            // Add text views to your table rows
            addDeviceDetailToRow(binding.row0, "Device Name: ", name)
            addDeviceDetailToRow(binding.row1, "MAC Address: ", address)

        }

    }

    private fun connectToDevice(selectedDevice: BleDevice) {
        lifecycleScope.launch {
//            val connection = selectedDevice.connect()
        }
    }


    private fun addDeviceDetailToRow(row: TableRow, label: String, value: String) {
        val labelView = TextView(this).apply { text = label }
        val valueView = TextView(this).apply { text = value }
        row.addView(labelView)
        row.addView(valueView)
    }
}