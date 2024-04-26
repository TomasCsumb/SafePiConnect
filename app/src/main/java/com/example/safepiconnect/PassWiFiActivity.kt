package com.example.safepiconnect

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.safepiconnect.databinding.ActivityPassWifiBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PassWiFiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPassWifiBinding
    private val scannerUtils = ScannerUtils()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pass_wifi)
        binding = ActivityPassWifiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getSSID()

        binding.confirmButton.setOnClickListener{
            val wifiPass = binding.passwordInput.text.toString()
            val ssid = binding.SSID.selectedItem.toString()
            sendWifiCommand(ssid, wifiPass)
        }
    }

    private fun getSSID() {
        val wifiManager = getSystemService(Context.WIFI_SERVICE) as? WifiManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSIONS_REQUEST_CODE)
        } else {
            val networks = wifiManager?.scanResults
            updateSSIDList(networks)
        }
    }

    private fun updateSSIDList(networks: List<ScanResult>?) {
        val ssids = networks?.filterNot { it.SSID.isEmpty() }?.map { it.SSID }?.distinct()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ssids ?: listOf())
        binding.SSID.adapter = adapter
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getSSID() // Try getting SSIDs again after permission is granted
        } else {
            Toast.makeText(this, "Permission denied to access location.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendWifiCommand(ssid : String, password : String) {
        scannerUtils.startBleScan(this, lifecycleScope)
        showLoading(true)

        lifecycleScope.launch {
            val targetDevice = scannerUtils.searchDevices(name = "safepi", macRange = "D8:3A:DD:B6")
            targetDevice?.let {
                Log.d(ContentValues.TAG, "Found target device: ${it.name}")

                // connect and deal with reading/writing
                BleDeviceManager(this@PassWiFiActivity, targetDevice.address) { bleDeviceManager ->
                    // build the command
                    val data = "wifi ${ssid} ${password}"
//                    val data = "wifi shahome Maplegt11"
                    Log.d(ContentValues.TAG, "COMMAND: $data")

                    bleDeviceManager.writeChar(
                        data,
                        BleDeviceManager.SERVICE_ID,
                        BleDeviceManager.WRITE_CHARACTERISTIC_UUID
                    ) { isSuccess ->
                        if (isSuccess) {
                            // Handle successful write operation
                            Log.d(ContentValues.TAG, "Write operation successful")
                        } else {
                            // Handle failed write operation
                            Log.e(ContentValues.TAG, "Write operation failed")
                        }
                    }
                    bleDeviceManager.disconnect()
                    scannerUtils.stopBleScan()
                    Toast.makeText(this@PassWiFiActivity, "Sent wifi credentials", Toast.LENGTH_SHORT).show()
                    showLoading(false)
                    navigateToMainMenu()
                }
            }  ?: run {
                withContext(Dispatchers.Main) {
                    val message = "WARNING: Device not found"
                    Log.d(ContentValues.TAG, message)
                    Toast.makeText(this@PassWiFiActivity, message, Toast.LENGTH_SHORT).show()
                    showLoading(false)
                    navigateToMainMenu()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.overlayContainer.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun navigateToMainMenu() {
        val intent = Intent(this, MainMenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
    }
}