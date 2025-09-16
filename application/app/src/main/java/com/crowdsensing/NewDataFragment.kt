package com.crowdsensing

import ViewDataFragment
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.crowdsensing.ViewUtils.setupNavigationSpinner
import com.crowdsensing.model.Session
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.lang.reflect.Type
import java.time.Instant

class NewDataFragment : Fragment() {

    private lateinit var commentEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var displayData: TextView
    private lateinit var dateTimeTextView: TextView
    private lateinit var session: Session

    private val apiClient = ApiClient("http://10.0.2.2:3000")
    val mapper = jacksonObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    companion object {
        private const val ARG_SESSION = "arg_session"

        fun newInstance(session: Session): NewDataFragment {
            return NewDataFragment().apply {
                arguments = Bundle().apply { putParcelable(ARG_SESSION, session)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_newdata, container, false)

        session =
            requireArguments().getParcelable(ARG_SESSION, Session::class.java)
                ?: throw IllegalStateException("Session must be provided to NewDataFragment")

       val sessionJson = mapper.writeValueAsString(session)
        println(sessionJson)

        dateTimeTextView = view.findViewById(R.id.textView)
        displayData = view.findViewById(R.id.viewMeasuredData)
        commentEditText = view.findViewById(R.id.editTextTextComment)
        sendButton = view.findViewById(R.id.button)

        dateTimeTextView.text = session.startTime.toString()
        displayData.text = """
            Model: ${session.phoneModel} 
    Measurement: ${session.chosenMeasurement}
    Frequency: ${session.frequency} Hz
    GPS points: ${session.gps?.size ?: 0}
    Compass points: ${session.compass?.size ?: 0}
    Proximity points: ${session.proximity?.size ?: 0}
    Accelerometer points: ${session.accelerometer?.size ?: 0}
    Gyroscope points: ${session.gyroscope?.size ?: 0}
             """.trimIndent()


        Log.i("NewDataFragment", session.toString())

        val navToolBar: Spinner = view.findViewById(R.id.toolbar_spinner)
        val navItems = resources.getStringArray(R.array.spinner_items)
        setupNavigationSpinner(navToolBar, navItems) { selectedItem ->
            when (selectedItem) {
                "View Measurements" -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, ViewDataFragment())
                        .commit()
                }
            }
        }

        setupNavigationSpinner(navToolBar, navItems) { selectedItem ->
            when (selectedItem) {
                "Measure" -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, HomeFragment())
                        .commit()
                }
            }
        }

        sendButton.setOnClickListener {
            val comment = commentEditText.text.toString()
            session = session.copy(description = comment)
            val updatedSessionJson = mapper.writeValueAsString(session)
            sendData(updatedSessionJson)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .addToBackStack(null)
                .commit()
        }


        return view
    }

    private fun sendData(sessionJson: String) {
        apiClient.postSensorData(sessionJson) { success, message ->
            activity?.runOnUiThread {
                if (success) {
                    Toast.makeText(requireContext(), "Data sent successfully: $message", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }}
