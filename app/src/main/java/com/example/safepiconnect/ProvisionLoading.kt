package com.example.safepiconnect

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.safepiconnect.LoginActivity.Companion.PASSWORD
import com.example.safepiconnect.LoginActivity.Companion.USERNAME
import com.example.safepiconnect.databinding.ActivityProvisionLoadingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import java.io.IOException

class ProvisionLoading : AppCompatActivity() {
    private lateinit var binding: ActivityProvisionLoadingBinding
    private val scannerUtils = ScannerUtils()
    private val bleDeviceManager: BleDeviceManager? = null
    val api = API()

    companion object {
        val USER_TOKEN = MutableLiveData<String>()
        val ACCESS_TOKEN = MutableLiveData<String>()
        val REFRESH_TOKEN = MutableLiveData<String>()
        val DEVICE_NETWORK_CONNECTION = MutableLiveData<String>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProvisionLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setting up LiveData observers
        setupObservers()

        // Launch the first request
        USERNAME.observe(this, Observer { username ->
            PASSWORD.observe(this, Observer { password ->
                performLoginRequest(username, password)
            })
        })
    }

    private fun setupObservers() {
        // Observing user token
        USER_TOKEN.observe(this, Observer { token ->
            USERNAME.observe(this, Observer { username ->
                if (token != null) performProvisionRequest(username, token)
            })
        })

        // Observing access and refresh tokens
        ACCESS_TOKEN.observe(this, Observer { accessToken ->
            REFRESH_TOKEN.observe(this, Observer { refreshToken ->
                if (accessToken != null && refreshToken != null) startScanSearch()
            })
        })
    }

    private fun startScanSearch() {
        scannerUtils.startBleScan(this, lifecycleScope)

        lifecycleScope.launch {
            val targetDevice = scannerUtils.searchDevices(name = "safepi", macRange = "D8:3A:DD:B6")
            targetDevice?.let {
                Log.d(TAG, "Found target device: ${it.name}")

                // connect and deal with reading/writing
                BleDeviceManager(this@ProvisionLoading, targetDevice.address) { bleDeviceManager ->

                    // build the command
                    val data = "provision ${ACCESS_TOKEN.value} ${REFRESH_TOKEN.value}"
                    Log.d(TAG, "COMMAND: $data")

                    val message = "Writing from provision Device!!!!!!!"
                    bleDeviceManager.writeChar(
                        data,
                        BleDeviceManager.SERVICE_ID,
                        BleDeviceManager.WRITE_CHARACTERISTIC_UUID
                    ) { isSuccess ->
                        if (isSuccess) {
                            // Handle successful write operation
                            Log.d(TAG, "Write operation successful")
                        } else {
                            // Handle failed write operation
                            Log.e(TAG, "Write operation failed")
                        }

                    }
                    bleDeviceManager.disconnect()
                    scannerUtils.stopBleScan()
                    Toast.makeText(this@ProvisionLoading, "Successfully Prvisioned Device", Toast.LENGTH_SHORT).show()
                    navigateToMainMenu()
                }
            }  ?: run {
                withContext(Dispatchers.Main) {
                    val message = "WARNING: Device not found"
                    Log.d(ContentValues.TAG, message)
                    Toast.makeText(this@ProvisionLoading, message, Toast.LENGTH_SHORT).show()
                    navigateToMainMenu()
                }
            }
        }
    }

    private fun navigateToMainMenu() {
        val intent = Intent(this, MainMenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun performLoginRequest(username : String, password : String) {
        // Launching coroutine on IO thread for network request
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = api.sendLoginRequest(username, password)
                withContext(Dispatchers.Main) {
                    // Post value to LiveData on the main thread
                    USER_TOKEN.postValue(token)
                }
                Log.d("Login", "Token received and posted: $token")
            } catch (e: IOException) {
                Log.e("Login", "Error during login request: ${e.message}", e)
            }
        }
    }

    private fun performProvisionRequest(username : String, token: String) {
        // Again launching coroutine on IO thread for network request
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val (accessToken, refreshToken) = api.sendProvisionRequest(username, token)
                // post the tokens
                ACCESS_TOKEN.postValue(accessToken)
                REFRESH_TOKEN.postValue(refreshToken)
                Log.d("Provision", "Access Token: $accessToken, Refresh Token: $refreshToken")
            } catch (e: IOException) {
                Log.e("Provision", "Error during provision request: ${e.message}", e)
            }
        }
    }
}