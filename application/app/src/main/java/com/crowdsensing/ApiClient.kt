package com.crowdsensing

import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class ApiClient(private val baseUrl: String) {

    private val client = OkHttpClient()

    fun postSensorData(
        sensorData: String,
        comment: String,
        callback: (success: Boolean, message: String) -> Unit
    ) {
        val requestBody = FormBody.Builder()
            .add("sensor_data", sensorData)
            .add("comment", comment)
            .build()

        val request = Request.Builder()
            .url(baseUrl)
            .post(requestBody)
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
