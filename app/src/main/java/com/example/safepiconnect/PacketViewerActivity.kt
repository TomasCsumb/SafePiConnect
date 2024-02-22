package com.example.safepiconnect

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.safepiconnect.databinding.ActivityPacketViewerBinding
import java.text.SimpleDateFormat
import java.util.*

class PacketViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPacketViewerBinding
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private var autoScrollToBottom = true
    private var deviceNameFilter: String = ""
    private var rssiFilter: Int = -100
    private var namedOnlyFilter = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPacketViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        requestBluetoothPermissions()
    }

    private fun setupViews() {
        // function for setting up the layout buttons and menues
        binding.scrollToggle.setOnClickListener {
            autoScrollToBottom = !autoScrollToBottom
            binding.scrollToggle.text = if (autoScrollToBottom) "Stop Auto Scroll" else "Auto Scroll"
        }

        binding.filterButton.setOnClickListener {
            binding.filterOptionsLayout.visibility = if (binding.filterOptionsLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        binding.editTextOption1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                deviceNameFilter = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.namedOnlyCheckBox.setOnCheckedChangeListener { _, isChecked ->
            namedOnlyFilter = isChecked
        }

        binding.rangeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                rssiFilter = -100 + progress
                binding.seekBarValue.text = "$rssiFilter"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun requestBluetoothPermissions() {
        val requiredPermissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (requiredPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, requiredPermissions.toTypedArray(), PERMISSION_REQUEST_CODE_BLUETOOTH_SCAN)
        } else {
            startScanning()
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            // Extract the device name from the result. Use "Unknown Device" as a fallback.
            val deviceName = result.device.name ?: "Unknown Device"
            val deviceAddress = result.device.address
            val rssi = result.rssi
            val timestampMillis = result.timestampNanos / 1_000_000 // Convert nanoseconds to milliseconds
            val humanReadableTimestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestampMillis))

            val isNamedDevice = deviceName != "Unknown Device"

            if (deviceName.contains(deviceNameFilter, ignoreCase = true) &&
                rssi >= rssiFilter &&
                (!namedOnlyFilter || isNamedDevice)) {

                val packetInfo = """
                $deviceName ($deviceAddress)
                RSSI: $rssi
                Timestamp: $humanReadableTimestamp
            """.trimIndent()

                runOnUiThread {
                    // Accessing the TextView through binding
                    binding.packetDataTextView.append("$packetInfo\n\n")

                    // Auto-scroll to the bottom only if autoScrollToBottom is true
                    if (autoScrollToBottom) {
                        binding.scrollView.post { binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
                    }
                }
            }
        }
    }


    private fun startScanning() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        bluetoothLeScanner?.startScan(scanCallback)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE_BLUETOOTH_SCAN && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startScanning()
        } else {
            Toast.makeText(this, "Permissions are required to scan BLE devices.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN), 1)
        } else {
            bluetoothLeScanner?.stopScan(scanCallback)
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE_BLUETOOTH_SCAN = 101
        private const val PERMISSION_REQUEST_CODE_BLUETOOTH_CONNECT = 102
    }

}
