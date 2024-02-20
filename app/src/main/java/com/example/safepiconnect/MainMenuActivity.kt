package com.example.safepiconnect

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.safepiconnect.databinding.ActivityMainMenuBinding
import kotlinx.coroutines.launch

class MainMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainMenuBinding
    private val scannerUtils = ScannerUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Provisioning button
        binding.provisionButton.setOnClickListener{
            provisionDevice()
//            val intent = Intent(this, MainMenuActivity::class.java)
//            startActivity(intent)
        }

        // start the scanner
        binding.scanButton.setOnClickListener{
            val intent = Intent(this, ScannerActivity::class.java)
            startActivity(intent)
        }

        // open controls window
        binding.controlsButton.setOnClickListener{
            val intent = Intent(this, AdminControlsActivity::class.java)
            startActivity(intent)
        }
    }
    private fun provisionDevice() {
        // here will be the logic for querying the api and then sending the wifi command and token
        // command to the RPi.

        // find device and connect
        lifecycleScope.launch {
            val foundDevice = scannerUtils.searchDevices(
                this@MainMenuActivity, lifecycleScope,
                "SafePi", "d8:3a:dd:b6"
            )

            // connect
            foundDevice?.let { device ->
                Log.d(TAG, "Device found: ${device.name}, MAC: ${device.address}")
                BleDeviceManager(this@MainMenuActivity, foundDevice.address) { bleDeviceManager ->
                    lifecycleScope.launch {
                        bleDeviceManager.readChar(
                            BleDeviceManager.SERVICE_ID,
                            BleDeviceManager.READ_CHARACTERISTIC_UUID
                        )
                        val message = "Writing from provisionDevice"
                        bleDeviceManager.writeChar(
                            message,
                            BleDeviceManager.SERVICE_ID,
                            BleDeviceManager.WRITE_CHARACTERISTIC_UUID
                        )
                        bleDeviceManager.disconnect()
                    }
                }
            } ?: run {
                val message = "Device not found"
                Log.d(TAG, message)
                Toast.makeText(this@MainMenuActivity, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}