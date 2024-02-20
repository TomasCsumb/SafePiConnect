package com.example.safepiconnect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.safepiconnect.databinding.ActivityAdminControlsBinding

class AdminControlsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminControlsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminControlsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}