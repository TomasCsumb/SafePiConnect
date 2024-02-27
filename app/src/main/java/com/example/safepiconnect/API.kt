package com.example.safepiconnect

import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest


class API {
    private val password = "mynewpassword"
    private val hashedPassword = Hasher.hash(password)
    private val client = OkHttpClient()
    private val url = "https://safepi.org/"

    interface ResponseCallback {
        fun onResponse(result: String)
        fun onFailure(exception: Exception)
    }

    fun get(path: String, queryParams: Map<String, String>? = null, callback: ResponseCallback) {
        val httpUrlBuilder = (url + path).toHttpUrlOrNull()?.newBuilder()

        // Add query parameters to the URL if any
        queryParams?.forEach { (key, value) ->
            httpUrlBuilder?.addQueryParameter(key, value)
        }

        val urlWithParams = httpUrlBuilder?.build().toString()

        val request = Request.Builder()
            .url(urlWithParams)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback.onFailure(IOException("Unexpected code $response"))
                        return
                    }

                    val result = response.body?.string() ?: "Error: Response body is null"
                    callback.onResponse(result)
                }
            }
        })
    }

    fun post(path: String, requestBody: String, callback: ResponseCallback) {
        val request = Request.Builder()
            .url(url + path)
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback.onFailure(IOException("Unexpected code $response"))
                        return
                    }

                    val result = response.body?.string() ?: "Error: Response body is null"
                    callback.onResponse(result)
                }
            }
        })
    }

    fun isLocked(json: String): Boolean {
        val jsonObject = JSONObject(json)

        // Navigate through the JSON object structure to find the isLocked boolean value
        val fields = jsonObject.getJSONObject("fields")
        val isLocked = fields.getJSONObject("isLocked")
        return isLocked.getBoolean("booleanValue")
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