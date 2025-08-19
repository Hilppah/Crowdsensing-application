package com.crowdsensing

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.crowdsensing.model.Session

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
                arguments = Bundle().apply {
                    putParcelable(ARG_SESSION, session)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_newdata, container, false)

        session = requireArguments().getParcelable(ARG_SESSION, Session::class.java)
            ?: throw IllegalStateException("Session must be provided to NewDataFragment")

        dateTimeTextView = view.findViewById(R.id.textView)
        displayData = view.findViewById(R.id.viewMeasuredData)
        commentEditText = view.findViewById(R.id.editTextText)
        sendButton = view.findViewById(R.id.button)

        dateTimeTextView.text = session.startTime.toString()
        displayData.text = session.phoneModel

        Log.i("hopefully during this lifetime i see data", session.toString())

        //TODO: turn session into json with jackson
        sendButton.setOnClickListener {
            val comment = commentEditText.text.toString()
            sendData("put session here as a real json", comment)
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

}
