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
            ) {}

            val connection = ClientBleGatt.connect(this@DeviceActivity, address, this)
            val services = connection.discoverServices()
            readChar( services, SERVICE_ID, READ_CHARACTERISTIC_UUID)
            writeChar("Who runs barter town??", services, SERVICE_ID, WRITE_CHARACTERISTIC_UUID)
        }
    }

    private fun readChar(services: ClientBleGattServices, serviceID: UUID, readCharUUID: UUID) {
        lifecycleScope.launch {
            if (ActivityCompat.checkSelfPermission(
                    this@DeviceActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {}

            val service = services.findService(serviceID)!!
            val readChar = service.findCharacteristic(readCharUUID)!!
            val cipherValue = readChar.read()?.value

            // Check if cipherValue is not null
            if (cipherValue != null) {
                // Use cipherValue for decryption
                val decryptedMessage = AESUtils.decrypt(cipherValue)
                Log.d("Decrypted Message", decryptedMessage.toString(Charsets.UTF_8))
            } else {
                Log.e("Error", "Cipher value is null")
            }
        }
    }

    private fun writeChar(message: String, services: ClientBleGattServices, serviceID: UUID, writeCharUUID: UUID) {
        lifecycleScope.launch {
            if (ActivityCompat.checkSelfPermission(
                    this@DeviceActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {}
            val service = services.findService(serviceID)!!
            val writeCharacteristic = service.findCharacteristic(writeCharUUID)
            if (writeCharacteristic != null) {
                val plainText = message.toByteArray(Charsets.UTF_8)
                try {
                    val encryptedText = AESUtils.encrypt(plainText)
                    val dataByteArray = DataByteArray(encryptedText)
                    writeCharacteristic.write(dataByteArray, BleWriteType.DEFAULT)
                    Log.d("Write Success", "Encrypted message written to characteristic")
                } catch (e: GeneralSecurityException) {
                    Log.e("Encryption Error", "Error encrypting message: ${e.localizedMessage}")
                }
            } else {
                Log.e("Error", "Write characteristic not found")
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

object AESUtils {
    private val AES_KEY = byteArrayOf(
        0x01.toByte(), 0x23.toByte(), 0x45.toByte(), 0x67.toByte(), 0x89.toByte(), 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(),
        0xFE.toByte(), 0xDC.toByte(), 0xBA.toByte(), 0x98.toByte(), 0x76.toByte(), 0x54.toByte(), 0x32.toByte(), 0x10.toByte(),
        0x01.toByte(), 0x23.toByte(), 0x45.toByte(), 0x67.toByte(), 0x89.toByte(), 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(),
        0xFE.toByte(), 0xDC.toByte(), 0xBA.toByte(), 0x98.toByte(), 0x76.toByte(), 0x54.toByte(), 0x32.toByte(), 0x10.toByte()
    )
    private val IV = byteArrayOf(
        0x01.toByte(), 0x23.toByte(), 0x45.toByte(), 0x67.toByte(), 0x89.toByte(), 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(),
        0xFE.toByte(), 0xDC.toByte(), 0xBA.toByte(), 0x98.toByte(), 0x76.toByte(), 0x54.toByte(), 0x32.toByte(), 0x10.toByte()
    )

    @Throws(GeneralSecurityException::class)
    fun encrypt(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(AES_KEY, "AES")
        val ivSpec = IvParameterSpec(IV)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(plaintext)
    }

    @Throws(GeneralSecurityException::class)
    fun decrypt(ciphertext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(AES_KEY, "AES")
        val ivSpec = IvParameterSpec(IV)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(ciphertext)
    }
}





