package com.example.safepiconnect

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

        // Test updating the statusTextView directly
        binding.simpleTextView.text = "No Status Posted.."

        ProvisionLoading.USER_TOKEN.observe(this, Observer { token ->
            if (!token.isNullOrEmpty()) {
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
        var displayMessage = ""
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://safepi.org/api_phone/getDoor")
                .header("Authorization", "Bearer $token")
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    withContext(Dispatchers.Main) {  // Switch back to Main thread to update UI
                        if (!response.isSuccessful) {
                            val errorBody = response.body?.string()
                            Log.e("NetworkError", "Failed with code ${response.code} and message: $errorBody")
                            binding.simpleTextView.text = "Error: $errorBody"
                            binding.simpleTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark))
                        } else {
                            Log.d("StatusActivity", "Response: $responseBody")
                            if (responseBody == "true") {
                                displayMessage = "Door1 is locked"
                                binding.simpleTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_green_light))
                            } else if (responseBody == "false") {
                                displayMessage = "Door1 is unlocked"
                                binding.simpleTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_orange_light))
                            } else {
                                displayMessage = "No Data Received"
                                binding.simpleTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark))
                            }
                            binding.simpleTextView.text = displayMessage
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("NetworkError", "Request failed: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    binding.simpleTextView.text = "Request failed: ${e.message}"
                    binding.simpleTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark))
                }
            }
        }
    }

}
