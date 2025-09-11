package com.crowdsensing

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ApiClient(private val baseUrl: String) {
    private val client = OkHttpClient()

    fun postSensorData(
        sensorJsonPayload: String,
        callback: (success: Boolean, message: String) -> Unit) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = sensorJsonPayload.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$baseUrl/api/sessions")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message ?: "Unknown error")
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        callback(true, it.body?.string() ?: "Success")
                    } else {
                        callback(false, "Error: ${it.code}")
                    }
                }
            }
        })
    }

    fun getData(
        endpoint: String,
        callback: (success: Boolean, responseBody: String) -> Unit
    ) {
        val request = Request.Builder()
            .url("$baseUrl/$endpoint")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message ?: "Unknown error")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        callback(true, it.body?.string() ?: "")
                    } else {
                        callback(false, "Error: ${it.code}")
                    }
                }
            }
        })
    }
}
