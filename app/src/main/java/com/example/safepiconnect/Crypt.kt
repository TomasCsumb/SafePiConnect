package com.example.safepiconnect

import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Crypt {

    private val keyAlgorithm = "EC"
    private val exchangeAlgorithm = "ECDH"
    private val encryptionScheme = "AES/CBC/PKCS7Padding"

    fun generateECDHKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm)
        keyPairGenerator.initialize(256) // Or another key size as required
        return keyPairGenerator.generateKeyPair()
    }

    fun publicKeyToBytes(publicKey: PublicKey): ByteArray {
        return publicKey.encoded
    }

    fun bytesToPublicKey(encodedKey: ByteArray): PublicKey {
        val keyFactory = KeyFactory.getInstance(keyAlgorithm)
        val publicKeySpec = X509EncodedKeySpec(encodedKey)
        return keyFactory.generatePublic(publicKeySpec)
    }

    fun generateSharedSecret(myKeyPair: KeyPair, receivedPublicKey: PublicKey): ByteArray {
        val keyAgreement = KeyAgreement.getInstance(exchangeAlgorithm)
        keyAgreement.init(myKeyPair.private)
        keyAgreement.doPhase(receivedPublicKey, true)
        return keyAgreement.generateSecret()
    }

    fun deriveAESKeyFromSharedSecret(sharedSecret: ByteArray): SecretKeySpec {
        val hasher = MessageDigest.getInstance("SHA-256")
        val hash = hasher.digest(sharedSecret)
        return SecretKeySpec(hash, 0, 16, "AES") // Use the first 128 bits (16 bytes) for AES key
    }

    fun encryptMessage(message: String, aesKey: SecretKeySpec): String {
        val cipher = Cipher.getInstance(encryptionScheme)
        val iv = ByteArray(16) // For simplicity, using a static IV. In practice, use a random IV and transmit it alongside the encrypted data.
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(message.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encrypted) // Encode to Base64 for easy transmission
    }

    fun decryptMessage(encryptedMessage: String, aesKey: SecretKeySpec): String {
        val cipher = Cipher.getInstance(encryptionScheme)
        val iv = ByteArray(16) // Must be the same IV used for encryption
        cipher.init(Cipher.DECRYPT_MODE, aesKey, IvParameterSpec(iv))
        val decodedMessage = Base64.getDecoder().decode(encryptedMessage)
        val decrypted = cipher.doFinal(decodedMessage)
        return String(decrypted, Charsets.UTF_8)
    }

}