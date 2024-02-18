package com.example.safepiconnect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.safepiconnect.databinding.ActivityDeviceBinding
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import java.util.UUID

class DeviceActivity : AppCompatActivity() {


    private lateinit var binding: ActivityDeviceBinding
    private lateinit var clientDevice: ClientBleGatt
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        binding = ActivityDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Warning Using deprecated method
        val serverDevice: ServerDevice = intent.getParcelableExtra<ServerDevice>("SELECTED_DEVICE")!! as ServerDevice
        serverDevice?.let {
            // Assuming BleDevice has methods getName() and getAddress()
            val name = it.name ?: "Unknown Device"
            val address = it.address


            // Add text views to your table rows
            addDeviceDetailToRow(binding.row0, "Device Name: ", name)
            addDeviceDetailToRow(binding.row1, "MAC Address: ", address)
            connectToDevice(address)
        }

    }

    private fun connectToDevice(address: String) {
        lifecycleScope.launch {
            if (ActivityCompat.checkSelfPermission(
                    this@DeviceActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
//                return
            }

            val connection = ClientBleGatt.connect(this@DeviceActivity, address, this)
            val services = connection.discoverServices()
            val service = services.findService(UUID.fromString("A07498CA-AD5B-474E-940D-16F1FBE7E8CD"))!!
            val readCharacteristic = service?.findCharacteristic(UUID.fromString("51FF12BB-3ED8-46E5-B4F9-D64E2FEC021B"))!!
            val message = readCharacteristic.read().value?.toString(Charsets.UTF_8) ?: ""
            val writeCharacteristic = service?.findCharacteristic(UUID.fromString("52FF12BB-3ED8-46E5-B4F9-D64E2FEC021B"))!!
            val secretStringMessage = "This is my message"
            val secretMessage = secretStringMessage.toByteArray(Charsets.UTF_8)
            val dataByteArray = DataByteArray(secretMessage)
            writeCharacteristic.write(dataByteArray, BleWriteType.DEFAULT)

            Log.d("DeviceActivity", message)
        }
    }


    private fun addDeviceDetailToRow(row: TableRow, label: String, value: String) {
        val labelView = TextView(this).apply { text = label }
        val valueView = TextView(this).apply { text = value }
        row.addView(labelView)
        row.addView(valueView)
    }
}