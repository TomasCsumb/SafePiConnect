package com.example.safepiconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.safepiconnect.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.connectButton.setOnClickListener{
            val intent = Intent(this, ScannerActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpVariables() {

        // You can now use connectButton for further operations
        // Example: connectButton.setOnClickListener { ... }
    }
}

