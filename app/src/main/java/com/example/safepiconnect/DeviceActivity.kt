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
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.GeneralSecurityException
import com.example.safepiconnect.BleDeviceManager


class DeviceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceBinding
    private lateinit var clientDevice: ClientBleGatt
    private val SERVICE_ID = UUID.fromString("A07498CA-AD5B-474E-940D-16F1FBE7E8CD")
    private val READ_CHARACTERISTIC_UUID = UUID.fromString("51FF12BB-3ED8-46E5-B4F9-D64E2FEC021B")
    private val WRITE_CHARACTERISTIC_UUID = UUID.fromString("52FF12BB-3ED8-46E5-B4F9-D64E2FEC021B")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        binding = ActivityDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Warning Using deprecated method
        val serverDevice: ServerDevice =
            intent.getParcelableExtra<ServerDevice>("SELECTED_DEVICE")!! as ServerDevice
        serverDevice?.let {
            // Assuming BleDevice has methods getName() and getAddress()
            val name = it.name ?: "Unknown Device"
            val address = it.address


            // Add text views to your table rows
            addDeviceDetailToRow(binding.row0, "Device Name: ", name)
            addDeviceDetailToRow(binding.row1, "MAC Address: ", address)

            val connectionManager = BleDeviceManager(this, address) { bleDeviceManager ->
                // Now the services are initialized, and you can safely call readChar and writeChar
                bleDeviceManager.readChar(SERVICE_ID, READ_CHARACTERISTIC_UUID)
                val message = "Who runs barter town???"
                bleDeviceManager.writeChar(message, SERVICE_ID, WRITE_CHARACTERISTIC_UUID)
            }
        }
    }

    private fun addDeviceDetailToRow(row: TableRow, label: String, value: String) {
        val labelView = TextView(this).apply { text = label }
        val valueView = TextView(this).apply { text = value }
        row.addView(labelView)
        row.addView(valueView)
    }
}





