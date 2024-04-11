package com.example.safepiconnect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.safepiconnect.databinding.ActivityDeviceBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        binding = ActivityDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}