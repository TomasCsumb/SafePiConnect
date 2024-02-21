package com.example.safepiconnect

import android.os.Bundle
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.safepiconnect.databinding.ActivityDeviceBinding
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.ServerDevice


class DeviceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceBinding
    private var isConnected = false
    private var bleDeviceManager: BleDeviceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        binding = ActivityDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val serverDevice: ServerDevice =
            intent.getParcelableExtra<ServerDevice>("SELECTED_DEVICE")!! as ServerDevice
        serverDevice?.let {
            // Get info
            val name = it.name ?: "Unknown Device"
            val address = it.address
            val hasName = it.hasName
            val bonded = it.isBonded

            // Add text views to table rows
            addDeviceDetailToRow(binding.row0, "Device Name: ", name)
            addDeviceDetailToRow(binding.row1, "MAC Address: ", address)
            addDeviceDetailToRow(binding.row2, "Bonded: ", bonded.toString())

            // setup connection button
            setConnectButtonClickListener(address)
            updateButtonState()
        }
    }

    private fun setConnectButtonClickListener(address: String) {
        binding.connectButton.setOnClickListener {
            if (!isConnected) {
                bleDeviceManager = BleDeviceManager(this, address) {
                    // Connection successful
                    isConnected = true
                    updateButtonState() // Update UI
                }
            } else {
                bleDeviceManager?.disconnect()
                isConnected = false
                bleDeviceManager = null
                updateButtonState() // Update UI
            }
        }
    }

    private fun updateButtonState() {
        if (isConnected) {
            val logTextView = findViewById<TextView>(R.id.logTextView)
            logTextView.visibility = View.VISIBLE
            binding.connectButton.text = getString(R.string.disconnect)
            binding.connectButton.setBackgroundColor(ContextCompat.getColor(this, R.color.light_red))
        } else {
            val logTextView = findViewById<TextView>(R.id.logTextView)
            logTextView.visibility = View.GONE
            binding.connectButton.text = getString(R.string.connect_button_text)
            binding.connectButton.setBackgroundColor(ContextCompat.getColor(this, R.color.light_green))
        }
    }

    private fun addDeviceDetailToRow(row: TableRow, label: String, value: String) {
        val labelView = TextView(this).apply { text = label }
        val valueView = TextView(this).apply { text = value }
        row.addView(labelView)
        row.addView(valueView)
    }
}





