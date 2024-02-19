package com.example.safepiconnect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.safepiconnect.databinding.ActivityPassWiFiBinding
private var networkSSID = "";
private var wifiPass = "";

class PassWiFiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPassWiFiBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pass_wi_fi)
        binding = ActivityPassWiFiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.confirmButton.setOnClickListener{
            networkSSID = binding.ssidInput.toString()
            wifiPass = binding.ssidInput.toString()
        }
    }
}