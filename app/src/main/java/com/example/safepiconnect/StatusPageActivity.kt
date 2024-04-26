package com.example.safepiconnect

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.safepiconnect.databinding.ActivityStatusPageBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class StatusPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatusPageBinding
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatusPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ProvisionLoading.USER_TOKEN.observe(this, Observer { token ->
            if (!token.isNullOrEmpty()) {
                Log.d("TokenCheck", "Bearer $token")
                repeatStatusUpdate(token)
            } else {
                Log.e("StatusActivity", "Token is null or empty")
            }
        })
    }

    private fun repeatStatusUpdate(token: String) {
        lifecycleScope.launch {
            while (true) {
                updateStatus(token)
                delay(5000)
            }
        }
    }

    private suspend fun updateStatus(token: String) {
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://safepi.org/api_phone/getDoor")
                .header("Authorization", "Bearer $token")
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string()
                        Log.e("NetworkError", "Failed with code ${response.code} and message: $errorBody")
                    } else {
                        val responseBody = response.body?.string()
                        Log.d("StatusActivity", "Response: $responseBody")
                    }
                }
            } catch (e: IOException) {
                Log.e("NetworkError", "Request failed: ${e.message}", e)
            }
        }
    }
}
