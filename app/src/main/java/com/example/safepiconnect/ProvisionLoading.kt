package com.example.safepiconnect

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.safepiconnect.databinding.ActivityProvisionLoadingBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProvisionLoading : AppCompatActivity() {
    private lateinit var binding: ActivityProvisionLoadingBinding
    private val scannerUtils = ScannerUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProvisionLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        provisionDevice {
            navigateToMainMenu()
        }
    }

    private fun provisionDevice(callback: () -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Heavy operations are offloaded to the IO dispatcher
            val foundDevice = scannerUtils.searchDevices(
                this@ProvisionLoading, lifecycleScope,
                "SafePi", "D8:3A:DD:B6"
            )

            foundDevice?.let { device ->
                Log.d(ContentValues.TAG, "Device found: ${device.name}, MAC: ${device.address}")

                val bleDeviceManager = BleDeviceManager(this@ProvisionLoading, foundDevice.address) { bleDeviceManager ->
                    bleDeviceManager.readChar(
                        BleDeviceManager.SERVICE_ID,
                        BleDeviceManager.READ_CHARACTERISTIC_UUID
                    )
                    // this is the message that sends across.
                    val message = "Writing from provision Device!"
                    bleDeviceManager.writeChar(
                        message,
                        BleDeviceManager.SERVICE_ID,
                        BleDeviceManager.WRITE_CHARACTERISTIC_UUID
                    )
                    bleDeviceManager.disconnect()
                }

                withContext(Dispatchers.Main) {
                    // UI updates must be done on the main thread
                    Toast.makeText(this@ProvisionLoading, "Device Found!", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                withContext(Dispatchers.Main) {
                    val message = "WARNING: Device not found"
                    Log.d(ContentValues.TAG, message)
                    Toast.makeText(this@ProvisionLoading, message, Toast.LENGTH_SHORT).show()
                }
            }

            withContext(Dispatchers.Main) {
                // Callback execution is also switched back to the main thread
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
