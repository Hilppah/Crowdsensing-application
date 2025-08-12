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

class NewDataFragment : Fragment() {

    private lateinit var commentEditText: EditText
    private lateinit var sendButton: Button
    private val apiClient = ApiClient("http://10.0.2.2:3000")

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
            sendData(sensorData, comment)
        }

        return view
    }

    private fun sendData(sensorData: String, comment: String) {
        apiClient.postSensorData(sensorData, comment) { success, message ->
            activity?.runOnUiThread {
                if (success) {
                    Toast.makeText(requireContext(), "Data sent successfully: $message", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
