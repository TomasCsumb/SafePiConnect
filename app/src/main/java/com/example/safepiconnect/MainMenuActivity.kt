package com.example.safepiconnect

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.safepiconnect.databinding.ActivityMainMenuBinding

class MainMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Provisioning button
        binding.provisionButton.setOnClickListener{
            val intent = Intent(this, QRScanActivity::class.java)
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

        // sniffer
        binding.snifferButton.setOnClickListener{
            val intent = Intent(this, PacketViewerActivity::class.java)
            startActivity(intent)
        }
    }
}