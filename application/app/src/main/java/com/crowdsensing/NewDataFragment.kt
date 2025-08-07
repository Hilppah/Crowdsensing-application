package com.crowdsensing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class NewDataFragment : Fragment() {

    private val client = OkHttpClient()

    private lateinit var commentEditText : EditText
    private lateinit var sendButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_newdata, container, false)

        val sensorData = arguments?.getString("sensor_data") ?: "No data received"

        val textView: TextView = view.findViewById(R.id.textView)
        textView.text = sensorData

        commentEditText = view.findViewById(R.id.editTextText)
        sendButton = view.findViewById(R.id.button)

        sendButton.setOnClickListener {
            val comment = commentEditText.text.toString()
            sendDataToServer(sensorData, comment)
        }

        return view
    }

    private fun sendDataToServer(sensorData: String, comment: String) {
        val url = "http://10.0.2.2:3000"

        val requestBody = FormBody.Builder()
            .add("sensor_data", sensorData)
            .add("comment", comment)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {

                    println(e)
                    Toast.makeText(requireContext(), "Failed to send data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {
                    if (response.isSuccessful) {
                        println(response.body.string()+ response.code.toString())
                        Toast.makeText(requireContext(), "Data sent successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    companion object {
        fun newInstance(sensorData: String): NewDataFragment {
            val fragment = NewDataFragment()
            val args = Bundle()
            args.putString("sensor_data", sensorData)
            fragment.arguments = args
            return fragment
        }
    }
}
