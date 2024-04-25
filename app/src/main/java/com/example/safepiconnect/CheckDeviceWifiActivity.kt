package com.example.safepiconnect

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.safepiconnect.databinding.ActivityCheckDeviceWifiBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CheckDeviceWifiActivity : AppCompatActivity(){
    private lateinit var binding: ActivityCheckDeviceWifiBinding
    private val scannerUtils = ScannerUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckDeviceWifiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkDeviceNetworkConnection()
    }

    private fun checkDeviceNetworkConnection() {
        scannerUtils.startBleScan(this, lifecycleScope)

        lifecycleScope.launch {
            val targetDevice = scannerUtils.searchDevices(name = "safepi", macRange = "D8:3A:DD:B6")
            targetDevice?.let { device ->
                Log.d(ContentValues.TAG, "Found target device: ${device.name}")

                // Initialize and use BleDeviceManager to read characteristics
                BleDeviceManager(this@CheckDeviceWifiActivity, targetDevice.address) { bleDeviceManager ->
                    // read operation
                    bleDeviceManager.readChar(
                        BleDeviceManager.SERVICE_ID,
                        BleDeviceManager.READ_CHARACTERISTIC_UUID
                    )
                    bleDeviceManager.disconnect()
                    scannerUtils.stopBleScan()
                }

                // Observe the LiveData for network connection status
                ProvisionLoading.DEVICE_NETWORK_CONNECTION.observe(this@CheckDeviceWifiActivity) { connectionStatus ->
                    if (connectionStatus == "connected:True") {
                        runOnUiThread {
                            val qrIntent = Intent(this@CheckDeviceWifiActivity, QRScanActivity::class.java)
                            startActivity(qrIntent)
                        }
                    } else {
                        runOnUiThread {
                            val wifiIntent = Intent(this@CheckDeviceWifiActivity, PassWiFiActivity::class.java)
                            startActivity(wifiIntent)
                        }
                    }
                }
            } ?: run {
                withContext(Dispatchers.Main) {
                    val message = "WARNING: Device not found"
                    Log.d(ContentValues.TAG, message)
                    Toast.makeText(this@CheckDeviceWifiActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}