package com.crowdsensing

import android.hardware.Sensor
import com.crowdsensing.model.Session
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ApiClient(private val baseUrl: String) {
    private val client = OkHttpClient()

    fun postSensorData(
        sensorData: Session,
        comment: String,
        callback: (success: Boolean, message: String) -> Unit
    ) {
        val gson = Gson()
        val json = gson.toJson(sensorData)

        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

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
