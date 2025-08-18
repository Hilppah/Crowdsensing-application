import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.crowdsensing.ApiClient
import com.crowdsensing.R

class NewDataFragment : Fragment() {

    private lateinit var commentEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var displayData: TextView
    private lateinit var dateTimeTextView: TextView
    private val apiClient = ApiClient("http://10.0.2.2:3000")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_newdata, container, false)

        val dateTime = arguments?.getString("date_time") ?: "No date/time"
        val measuredData = arguments?.getString("measured_data") ?: "No measurements"

        dateTimeTextView = view.findViewById(R.id.textView)
        displayData = view.findViewById(R.id.viewMeasuredData)
        commentEditText = view.findViewById(R.id.editTextText)
        sendButton = view.findViewById(R.id.button)

        dateTimeTextView.text = dateTime
        displayData.text = measuredData

        sendButton.setOnClickListener {
            val comment = commentEditText.text.toString()
            sendData(measuredData, comment)
        }
        return view }

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
        fun newInstance(dateTime: String, measuredData: String): NewDataFragment {
            return NewDataFragment().apply {
                arguments = Bundle().apply {
                    putString("date_time", dateTime)
                    putString("measured_data", measuredData)
                }
            }
        }
    }
}
