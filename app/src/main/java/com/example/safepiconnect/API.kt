package com.example.safepiconnect

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest

class API {
    private val client = OkHttpClient()
    private val baseUrl = "https://safepi.org"

    suspend fun sendLoginRequest(email: String, password: String): String {
        val formBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url("$baseUrl/login")
            .post(formBody)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build()

        return withContext(Dispatchers.IO) { // Switch to IO Dispatcher for network operation
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response")
                }
                val responseBody = response.body?.string() ?: throw IOException("Response body is null")
                val jsonObject = JSONObject(responseBody)
                jsonObject.getString("access_token")
            }
        }
    }

    suspend fun sendProvisionRequest(email: String, token: String): Pair<String, String> {
        val formBody = FormBody.Builder()
            .add("email", email)
            .build()

        val request = Request.Builder()
            .url("$baseUrl/provision_pi")
            .post(formBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build()

        return withContext(Dispatchers.IO) { // Execute network call on I/O dispatcher
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response")
                }
                val responseBody = response.body?.string() ?: throw IOException("Response body is null")
                val jsonObject = JSONObject(responseBody)
                Pair(
                    jsonObject.getString("access_token"),
                    jsonObject.getString("refresh_token")
                )
            }
        }
    }
}


object Hasher {
    fun hash(input: String): String {
        val bytes = input.toString().toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}


//fun main() {
//    val api = API()
//    val test = api.get("api/getDoor")
//    println("Door1 Locked: " + api.isLocked(test))
//
//}