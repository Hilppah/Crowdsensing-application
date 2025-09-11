import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crowdsensing.ApiClient
import com.crowdsensing.HomeFragment
import com.crowdsensing.R
import com.crowdsensing.SessionAdapter
import com.crowdsensing.ViewUtils.setupNavigationSpinner
import com.crowdsensing.model.Session

class ViewDataFragment : Fragment() {

    private val sessionList = mutableListOf<Session>()
    private lateinit var sessionAdapter: SessionAdapter
    private val apiClient = ApiClient("http://10.0.2.2:3000")
    private val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_viewdata, container, false)

        val navItems = resources.getStringArray(R.array.spinner_items)
        val navToolBar: Spinner = view.findViewById(R.id.toolbar_spinner)
        setupNavigationSpinner(navToolBar, navItems) { selectedItem ->
            when (selectedItem) {
                "Measure" -> parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HomeFragment())
                    .commit()
            }
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        sessionAdapter = SessionAdapter(sessionList) { session -> showPopup(session) }
        recyclerView.adapter = sessionAdapter

        fetchSessions()

        return view
    }

    private fun fetchSessions() {
        apiClient.getData("api/sessions") { success, response ->
            activity?.runOnUiThread {
                if (success) {
                    try {
                        val sessions: List<Session> = mapper.readValue(response)
                        sessionList.clear()
                        sessionList.addAll(sessions)
                        sessionAdapter.notifyDataSetChanged()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Failed to parse data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch sessions", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showPopup(session: Session) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Session Details")
            .setMessage(
                """
                Model: ${session.phoneModel}
                Start: ${session.startTime}
                End: ${session.endTime}
                Measurement: ${session.chosenMeasurement}
                Frequency: ${session.frequency} Hz
                Comment: ${session.description}
                GPS points: ${session.gps.size}
                Compass points: ${session.compass.size}
                Proximity points: ${session.proximity.size}
                Accelerometer points: ${session.accelerometer.size}
                Gyroscope points: ${session.gyroscope.size}
                """.trimIndent())
            .setPositiveButton("Close", null)
            .show()
    }
}