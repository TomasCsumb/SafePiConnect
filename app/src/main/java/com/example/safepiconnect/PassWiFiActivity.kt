package com.example.safepiconnect

import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.safepiconnect.databinding.ActivityPassWiFiBinding

private var wifiPass = ""


class PassWiFiActivity : AppCompatActivity() {
    val WIFI_SERVICE: String = ""
    private lateinit var binding: ActivityPassWiFiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pass_wi_fi)
        binding = ActivityPassWiFiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getSSID()
        binding.confirmButton.setOnClickListener{
            wifiPass = binding.passwordInput.text.toString()
        }
    }

    private fun getSSID(){
        val wifiManager = getSystemService(this.WIFI_SERVICE) as? WifiManager
        val networks = if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSIONS_REQUEST_CODE)
            return
        }
        else{
            wifiManager?.scanResults

        }
        val ssids = networks?.filterNot { it.SSID.isEmpty() }?.map { it.SSID }?.distinct()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ssids ?: listOf())
        binding.SSID.adapter = adapter
    }
    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
    }
}