package com.example.safepiconnect

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.safepiconnect.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val REQUEST_CODE_LOCATION_PERMISSION = 101
        private const val REQUEST_CODE_BLUETOOTH_PERMISSIONS = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestLocationPermission()

        // Example of a GET request for the door status.
        val api = API()
        api.get("api/getDoor", null, object : API.ResponseCallback {
            override fun onResponse(result: String) {
                Log.d(  TAG,"Response received: $result")

                val tof = api.isLocked(result)
                if (tof) {
                    Log.d(TAG, "Door is Locked")
                } else {
                    Log.d(TAG, "Door is Unlocked")
                }
            }

            override fun onFailure(exception: Exception) {
                Log.d(TAG, "Request failed: ${exception.message}")
            }
        })

        // example of setting the status to unlocked
        val emailRequestBody = """
        {
            "email": "test@test.com",
            "password": "eb233b36632dc77950be4fc9e96d62f1c097d5dbd529ae68a2b314710791ca8e",
            "isLocked": "true"
        }
        """
        api.post("api/postDoor", emailRequestBody, object : API.ResponseCallback {
            override fun onResponse(result: String) {
                Log.d(TAG,"Response received: $result")
            }

            override fun onFailure(exception: Exception) {
                Log.d(TAG,"Request failed: ${exception.message}")
            }
        })


        binding.wifiButton.setOnClickListener {
            val intent = Intent(this, PassWiFiActivity::class.java)
            startActivity(intent)
        }

        binding.menuButton.setOnClickListener {
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.home_button -> {
                //Do something
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION_PERMISSION)
        } else {
            // If location permission is already granted, proceed to request Bluetooth permissions
            requestBluetoothPermissions()
        }
    }

    private fun requestBluetoothPermissions() {
        val requiredPermissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        if (requiredPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, requiredPermissions.toTypedArray(), REQUEST_CODE_BLUETOOTH_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location permission granted, now request Bluetooth permissions
                    requestBluetoothPermissions()
                } else {
                    // Handle the case where location permission is denied
                    Toast.makeText(this, "Location permission is required for Bluetooth scanning.", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_CODE_BLUETOOTH_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Both Bluetooth permissions are granted
                    // Proceed with your Bluetooth operations
                } else {
                    // Handle the case where Bluetooth permissions are denied
                    Toast.makeText(this, "Bluetooth permissions are required for app functionality.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
