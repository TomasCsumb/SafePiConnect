package com.example.safepiconnect

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.safepiconnect.databinding.ActivityProvisionLoadingBinding
import kotlinx.coroutines.launch

class ProvisionLoading : AppCompatActivity() {
    private lateinit var binding: ActivityProvisionLoadingBinding

    private val scannerUtils = ScannerUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProvisionLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        provisionDevice {
            // return to main menu after provisioning is complete
            val intent = Intent(this, MainMenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun provisionDevice(callback: () -> Unit) {
        // here will be the logic for querying the api and then sending the wifi command and token
        // command to the RPi.

        // find device and connect
        lifecycleScope.launch {
            var toastMessage = ""
            val foundDevice = scannerUtils.searchDevices(
                this@ProvisionLoading, lifecycleScope,
                "", "D8:3A:DD:B6"
            )

            // connect
            foundDevice?.let { device ->
                Log.d(ContentValues.TAG, "Device found: ${device.name}, MAC: ${device.address}")
                BleDeviceManager(this@ProvisionLoading, foundDevice.address) { bleDeviceManager ->
                    lifecycleScope.launch {
                        bleDeviceManager.readChar(
                            BleDeviceManager.SERVICE_ID,
                            BleDeviceManager.READ_CHARACTERISTIC_UUID
                        )
                        toastMessage = "Writing from provisionDevice"
                        bleDeviceManager.writeChar(
                            toastMessage,
                            BleDeviceManager.SERVICE_ID,
                            BleDeviceManager.WRITE_CHARACTERISTIC_UUID
                        )
                        bleDeviceManager.disconnect()
                        toastMessage = "Device Found!"
                        Toast.makeText(this@ProvisionLoading, toastMessage, Toast.LENGTH_SHORT).show()
                        callback()
                    }
                }
            } ?: run {
                val message = "WARNING: Device not found"
                Log.d(ContentValues.TAG, message)
                Toast.makeText(this@ProvisionLoading, message, Toast.LENGTH_SHORT).show()
                callback()
            }
        }
    }
}