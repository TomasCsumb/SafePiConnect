package com.example.safepiconnect

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PacketViewerActivity : AppCompatActivity() {

    private lateinit var packetDataTextView: TextView
    private lateinit var scrollView: ScrollView
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private var autoScrollToBottom = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_packet_viewer)

        packetDataTextView = findViewById(R.id.packetDataTextView)
        scrollView = findViewById(R.id.scrollView)
        val scrollToggleButton: Button = findViewById(R.id.scrollToggle)

        scrollToggleButton.setOnClickListener {
            autoScrollToBottom = !autoScrollToBottom
            scrollToggleButton.text = if (autoScrollToBottom) "Auto Scroll" else "Manual Scroll"
        }

        // Check for BLUETOOTH_SCAN permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), PERMISSION_REQUEST_CODE_BLUETOOTH_SCAN)
            Toast.makeText(this@PacketViewerActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
        }

        // Check for BLUETOOTH_CONNECT permission on Android 12 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), PERMISSION_REQUEST_CODE_BLUETOOTH_CONNECT)
            Toast.makeText(this@PacketViewerActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
        } else {
            startScanning()  // Start scanning if all permissions are granted
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            // stupid permissions
            if (ContextCompat.checkSelfPermission(this@PacketViewerActivity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@PacketViewerActivity, arrayOf(Manifest.permission.BLUETOOTH_SCAN), PERMISSION_REQUEST_CODE_BLUETOOTH_SCAN)
                Toast.makeText(this@PacketViewerActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
            val deviceName = result.device.name ?: "Unknown Device"
            val deviceAddress = result.device.address
            val rssi = result.rssi
            val timestampMillis = result.timestampNanos / 1_000_000 // Convert nanoseconds to milliseconds
            val humanReadableTimestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestampMillis))

            // Getting the ScanRecord and parsing some data from it
            val scanRecord = result.scanRecord
            val serviceUuids = scanRecord?.serviceUuids?.joinToString { it.uuid.toString() } ?: "No Service UUIDs"
            val manufacturerData = scanRecord?.manufacturerSpecificData?.toString() ?: "No Manufacturer Data"

            // Constructing a detailed packet info string
            val packetInfo = """
            Device: $deviceName ($deviceAddress)
            RSSI: $rssi
            Timestamp: $humanReadableTimestamp
            Service UUIDs: $serviceUuids
            Manufacturer Data: $manufacturerData
            """.trimIndent()

            Log.d("BLE Packet", packetInfo)
            runOnUiThread {
                packetDataTextView.append("$packetInfo\n\n")

                // Auto-scroll to the bottom only if autoScrollToBottom is true
                if (autoScrollToBottom) {
                    scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
                }
            }
        }
    }

    private fun startScanning() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN
            ), PERMISSION_REQUEST_CODE_BLUETOOTH_SCAN)
        } else {
            bluetoothLeScanner?.startScan(scanCallback)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE_BLUETOOTH_CONNECT) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startScanning()
            } else {
                Toast.makeText(this, "Permissions are required to scan BLE devices.", Toast.LENGTH_SHORT).show()
            }
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
