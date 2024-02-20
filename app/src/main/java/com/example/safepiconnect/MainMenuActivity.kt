package com.example.safepiconnect

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.safepiconnect.databinding.ActivityMainMenuBinding
import kotlinx.coroutines.launch

class MainMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Provisioning button
        binding.provisionButton.setOnClickListener{
            provisionDevice()
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
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

//        val deviceName = "SafePi"
//        val foundDevice = ScannerActivity.DeviceManager.findDevice(deviceName, "d8:3a:dd:b6")

//        if (foundDevice != null) {
//            BleDeviceManager(this, foundDevice.address) { bleDeviceManager ->
//                lifecycleScope.launch {
//                    // Now the services are initialized, and you can safely call readChar and writeChar
//                    bleDeviceManager.readChar(BleDeviceManager.SERVICE_ID, BleDeviceManager.READ_CHARACTERISTIC_UUID)
//                    val message = "writing from provisionDevice"
//                    bleDeviceManager.writeChar(message, BleDeviceManager.SERVICE_ID, BleDeviceManager.WRITE_CHARACTERISTIC_UUID)
//                    // disconnect when done
//                    bleDeviceManager.disconnect()
//                }
//            }
//        } else {
//            Log.d(TAG, "Device not found")
//        }

    }
}