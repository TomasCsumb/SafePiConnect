package com.example.safepiconnect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.GeneralSecurityException


class BleDeviceManager(
    private val context: Context,
    private val address: String,
    private val onServicesInitialized: (BleDeviceManager) -> Unit // Callback function with BleDeviceManager as parameter
) {
    private lateinit var services: ClientBleGattServices
    private val lifecycleScope: LifecycleCoroutineScope by lazy {
        (context as? AppCompatActivity)?.lifecycleScope ?: throw IllegalArgumentException("Context must be an AppCompatActivity")
    }

    init {
        connectToDevice()
    }

    private fun connectToDevice() {
        lifecycleScope.launch {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Bluetooth Connect permission not granted")
                return@launch
            }

            val connection = ClientBleGatt.connect(context, address, this)
            services = connection.discoverServices() // Assign to class-level property
            onServicesInitialized(this@BleDeviceManager) // Callback after initialization, passing the current instance
        }
    }

    fun readChar(serviceID: UUID, readCharUUID: UUID) {
        if (!::services.isInitialized) {
            // Services not initialized yet, return or handle accordingly
            Log.e(TAG, "Services not initialized yet")
            return
        }

        lifecycleScope.launch {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {}
            val service = services.findService(serviceID) ?: throw IllegalStateException("Service not found")
            val readChar = service.findCharacteristic(readCharUUID) ?: throw IllegalStateException("Read characteristic not found")
            val cipherValue = readChar.read()?.value

            cipherValue?.let {
                val decryptedMessage = AESUtils.decrypt(it)
                Log.d(TAG, "Decrypted Message: ${decryptedMessage.toString(Charsets.UTF_8)}")
            } ?: Log.e(TAG, "Cipher value is null")
        }
    }

    fun writeChar(message: String, serviceID: UUID, writeCharUUID: UUID) {
        if (!::services.isInitialized) {
            // Services not initialized yet, return or handle accordingly
            Log.e(TAG, "Services not initialized yet")
            return
        }

        lifecycleScope.launch {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Handle lack of permissions here
                return@launch
            }

            val service = services.findService(serviceID) ?: throw IllegalStateException("Service not found")
            val writeChar = service.findCharacteristic(writeCharUUID) ?: throw IllegalStateException("Write characteristic not found")
            val plainText = message.toByteArray(Charsets.UTF_8)
            try {
                val encryptedText = AESUtils.encrypt(plainText)
                val dataByteArray = DataByteArray(encryptedText)
                writeChar.write(dataByteArray, BleWriteType.DEFAULT)
                Log.d(TAG, "Encrypted message written to characteristic")
            } catch (e: GeneralSecurityException) {
                Log.e(TAG, "Error encrypting message: ${e.localizedMessage}")
            }
        }
    }


    companion object {
        private const val TAG = "BleDeviceManager"
    }
}

object AESUtils {
    // do not make const because we have to convert these.
    private val hexKey = "0123456789ABCDEFFEDCBA98765432100123456789ABCDEFFEDCBA9876543210"
    private val hexIV = "0123456789ABCDEFFEDCBA9876543210"

    private val AES_KEY = hexStringToByteArray(hexKey)
    private val IV = hexStringToByteArray(hexIV)

    private fun hexStringToByteArray(hexString: String): ByteArray {
        val len = hexString.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(hexString[i], 16) shl 4) + Character.digit(hexString[i + 1], 16)).toByte()
        }
        return data
    }

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