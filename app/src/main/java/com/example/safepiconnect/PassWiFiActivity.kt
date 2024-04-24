package com.example.safepiconnect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.safepiconnect.databinding.ActivityPassWifiBinding

private var wifiPass = ""


class PassWiFiActivity : AppCompatActivity() {
    val WIFI_SERVICE: String = ""
    private lateinit var binding: ActivityPassWifiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pass_wifi)
        binding = ActivityPassWifiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getSSID()
        binding.confirmButton.setOnClickListener{
            wifiPass = binding.passwordInput.text.toString()
        }
    }

    // Assuming PERMISSIONS_REQUEST_CODE is defined elsewhere as a constant
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

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
    }
}