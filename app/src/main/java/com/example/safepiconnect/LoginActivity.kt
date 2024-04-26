package com.example.safepiconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.safepiconnect.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

interface LoginCallback {
    fun onLoginSuccess()
    fun onLoginFailed(error: String)
}

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    val api = API()  // Ensure API is setup to handle login requests

    companion object {
        val USERNAME = MutableLiveData<String>()
        val PASSWORD = MutableLiveData<String>()

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogIn.setOnClickListener {
            val username = binding.editTextUsername.text.toString()
            val password = binding.editTextPassword.text.toString()
            USERNAME.postValue(username)
            PASSWORD.postValue(password)
            performLoginRequest(username, password, object : LoginCallback {
                override fun onLoginSuccess() {
                    Toast.makeText(this@LoginActivity, "Successfully Logged in", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainMenuActivity::class.java))
                }

                override fun onLoginFailed(error: String) {
                    Toast.makeText(this@LoginActivity, "Failed Login Attempt: $error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun performLoginRequest(username: String, password: String, callback: LoginCallback) {
        lifecycleScope.launch {
            try {
                // Asynchronously send login request
                val token = withContext(Dispatchers.IO) {
                    api.sendLoginRequest(username, password)
                }
                // Check the token after returning to the Main thread
                Log.d("Login", token)
                if (token.isNullOrEmpty()) {
                    callback.onLoginFailed("Invalid username or password")
                } else {
                    // Save the token if valid and continue
                    ProvisionLoading.USER_TOKEN.postValue(token)
                    Log.d("Login", "Token received and posted: $token")
                    callback.onLoginSuccess()
                }
            } catch (e: Exception) {
                Log.e("Login", "Error during login request: ${e.message}", e)
                callback.onLoginFailed(e.message ?: "Unknown error")
            }
        }
    }

}
