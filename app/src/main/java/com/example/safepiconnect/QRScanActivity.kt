package com.example.safepiconnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.safepiconnect.databinding.ActivityQrScanBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity
import kotlinx.coroutines.delay

class QRScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQrScanBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initiateScan()

    }

    private fun initiateScan() {
        IntentIntegrator(this).apply {
            captureActivity = CaptureActivity::class.java // Use the default CaptureActivity
            setOrientationLocked(false)
            initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Log.d("QRScannerActivity", "Cancelled scan")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Log.d("QRScannerActivity", "Scanned: " + result.contents)
//                Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()

                // simple check for length of key
                if (result.contents.length != 64) {
                    val intent = Intent(this, QRScanActivity::class.java)
                    Toast.makeText(this, "Scan Incomplete", Toast.LENGTH_LONG).show()
                    startActivity(intent)
                } else {
                    storeKey(this, result.contents)
                    // go to the provision loading screen
                    val intent = Intent(this, ProvisionLoading::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    fun storeKey(context: Context, key: String) {
        val sharedPreferences = context.getSharedPreferences("EncPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("encryptionKey", key)
            apply()
        }
    }
}
