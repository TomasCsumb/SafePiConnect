package com.example.safepiconnect

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.safepiconnect.databinding.ActivityProvisionLoadingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt

class ProvisionLoading : AppCompatActivity() {
    private lateinit var binding: ActivityProvisionLoadingBinding
    private val scannerUtils = ScannerUtils()
    private val bleDeviceManager: BleDeviceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProvisionLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        startScanSearch() {
            navigateToMainMenu()
        }
    }

    private fun startScanSearch(callback: () -> Unit) {
        scannerUtils.startBleScan(this, lifecycleScope)

        lifecycleScope.launch {
            val targetDevice = scannerUtils.searchDevices(name = "safepi", macRange = "D8:3A:DD:B6")
            targetDevice?.let {
                Log.d(TAG, "Found target device: ${it.name}")

                // connect and deal with reading/writing
                BleDeviceManager(this@ProvisionLoading, targetDevice.address) { bleDeviceManager ->

                    // this is the message that sends across.
                    val message = "Writing from provision Device!!!!!!!@!@"
                    bleDeviceManager.writeChar(
                        message,
                        BleDeviceManager.SERVICE_ID,
                        BleDeviceManager.WRITE_CHARACTERISTIC_UUID
                    ) { isSuccess ->
                        if (isSuccess) {
                            // Handle successful write operation
                            Log.d(TAG, "Write operation successful")
                        } else {
                            // Handle failed write operation
                            Log.e(TAG, "Write operation failed")
                        }
                    }
                    bleDeviceManager.disconnect()
                    scannerUtils.stopBleScan()
                    Toast.makeText(this@ProvisionLoading, "Success", Toast.LENGTH_SHORT).show()
                }
            }  ?: run {
                withContext(Dispatchers.Main) {
                    val message = "WARNING: Device not found"
                    Log.d(ContentValues.TAG, message)
                    Toast.makeText(this@ProvisionLoading, message, Toast.LENGTH_SHORT).show()
                }
            }
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }

    private fun navigateToMainMenu() {
        val intent = Intent(this, MainMenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}