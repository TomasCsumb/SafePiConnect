package com.example.safepiconnect

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.safepiconnect.databinding.ActivityDeviceBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        binding = ActivityDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getToken()
    }

    fun getToken() {
        val path:String = "/token"
        val body = "grant_type=client_credentials\nscope=read"
        var api = API()
        api.post(path, body, object:API.ResponseCallback{
            override fun onResponse(result: String) {
                Log.d(ContentValues.TAG, "Response received: $result")
            }
            override fun onFailure(exception: Exception) {
                Log.d(ContentValues.TAG,"Request failed: ${exception.message}")
            }
        })
    }
}