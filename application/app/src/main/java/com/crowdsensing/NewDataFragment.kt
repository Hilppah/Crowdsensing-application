package com.crowdsensing

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
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.Instant

class NewDataFragment : Fragment() {

    private lateinit var commentEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var displayData: TextView
    private lateinit var dateTimeTextView: TextView
    private lateinit var session: Session

    private val apiClient = ApiClient("http://10.0.2.2:3000")

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

        session = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(ARG_SESSION, Session::class.java)
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getParcelable(ARG_SESSION)
        } ?: throw IllegalStateException("Session must be provided to NewDataFragment")

        dateTimeTextView = view.findViewById(R.id.textView)
        displayData = view.findViewById(R.id.viewMeasuredData)
        commentEditText = view.findViewById(R.id.editTextText)
        sendButton = view.findViewById(R.id.button)

        dateTimeTextView.text = session.startTime.toString()
        displayData.text = """Model: ${session.phoneModel} Measurement: ${session.chosenMeasurement} Frequency: ${session.frequency} HzGPS points: ${session.gps.size}Compass points: ${session.compass.size}Proximity points: ${session.proximity.size} Accelerometer points: ${session.accelerometer.size}Gyroscope points: ${session.gyroscope.size}""".trimIndent()


        Log.i("NewDataFragment", session.toString())

        val navToolBar: Spinner = view.findViewById(R.id.toolbar_spinner)
        val navItems = resources.getStringArray(R.array.spinner_items)
        setupNavigationSpinner(navToolBar, navItems) { selectedItem ->
            when (selectedItem) {
                "Search Measurements" -> {
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
            sendData(session, comment)
        }

        return view
    }


    class InstantSerializer : JsonSerializer<Instant> {
        override fun serialize(
            src: Instant?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return JsonPrimitive(src?.toEpochMilli())
        }
    }

    class InstantDeserializer : JsonDeserializer<Instant> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Instant {
            return Instant.ofEpochMilli(json!!.asLong)
        }
    }

    private fun sendData(session: Session, comment: String) {
        apiClient.postSensorData(session, comment) { success, message ->
            activity?.runOnUiThread {
                if (success) {
                    Toast.makeText(requireContext(), "Data sent successfully: $message", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }}
