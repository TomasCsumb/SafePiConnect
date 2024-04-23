package com.example.safepiconnect

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProvisionLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // setup observer for the user token so that we can provision once the user has logged in.
        MainActivity.USER_TOKEN.observe(this, Observer { token ->
            if (token != null) {
                performProvisionRequest(token)
            }
        })

        // Launch the first request
        performLoginRequest()

        MainActivity.ACCESS_TOKEN.observe(this, Observer { accessToken ->
            if (accessToken != null) {
                // Log the access token using Android's Log class
                Log.d("AnotherActivity", "Observed Access Token: $accessToken")
            } else {
                // Optionally handle or log null cases
                Log.d("AnotherActivity", "Access Token is null")
            }
        })
        startScanSearch() {
            navigateToMainMenu()
        }
    }

    private fun startScanSearch(callback: () -> Unit) {
        scannerUtils.startBleScan(this, lifecycleScope)

        lifecycleScope.launch {
            val targetDevice = scannerUtils.searchDevices(name = "safepi", macRange = "D8:3A:DD:B6")
            targetDevice?.let {
                Log.d(TAG, "Found target device: ${it.name}")

                // connect and deal with reading/writing
                BleDeviceManager(this@ProvisionLoading, targetDevice.address) { bleDeviceManager ->


                    // TODO: Execute two writes here. the first will be the Wifi command if the read char does not show
                    //  status "connected", and wait for the status to be connected. Then send the token command and
                    //  the two tokens as payload.
                    val message = "Writing from provision Device!!!!!!!00"
                    bleDeviceManager.writeChar(
                        message,
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
                }
            }  ?: run {
                withContext(Dispatchers.Main) {
                    val message = "WARNING: Device not found"
                    Log.d(ContentValues.TAG, message)
                    Toast.makeText(this@ProvisionLoading, message, Toast.LENGTH_SHORT).show()
                }
            }
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }

    private fun navigateToMainMenu() {
        val intent = Intent(this, MainMenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    private fun performLoginRequest() {
        // Launching coroutine on IO thread for network request
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = api.sendLoginRequest("devtest@test.com", "0urPa\$\$p0rt1")
                withContext(Dispatchers.Main) {
                    // Post value to LiveData on the main thread
                    MainActivity.USER_TOKEN.postValue(token)
                }
                Log.d("Login", "Token received and posted: $token")
            } catch (e: IOException) {
                Log.e("Login", "Error during login request: ${e.message}", e)
            }
        }
    }

    private fun performProvisionRequest(token: String) {
        // Again launching coroutine on IO thread for network request
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val (accessToken, refreshToken) = api.sendProvisionRequest("devtest@test.com", token)
                // post the tokens
                MainActivity.ACCESS_TOKEN.postValue(accessToken)
                MainActivity.REFRESH_TOKEN.postValue(refreshToken)
                Log.d("Provision", "Access Token: $accessToken, Refresh Token: $refreshToken")
            } catch (e: IOException) {
                Log.e("Provision", "Error during provision request: ${e.message}", e)
            }
        }
    }
}