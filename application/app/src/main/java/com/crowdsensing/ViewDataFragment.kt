import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import android.widget.TextView
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
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import androidx.core.widget.doAfterTextChanged

class ViewDataFragment : Fragment() {

    private val sessionList = mutableListOf<Session>()
    private val apiClient = ApiClient("https://crowdsensing-application-1.onrender.com")
    private val mapper = jacksonObjectMapper().apply { findAndRegisterModules()}
    private lateinit var sessionAdapter: SessionAdapter
    private lateinit var searchBar: AutoCompleteTextView
    private lateinit var sortSpinner: Spinner
    private var allSessions: List<Session> = emptyList()
    private var displayedSessions: List<Session> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_viewdata, container, false)

        val navItems = resources.getStringArray(R.array.spinner_items)
        val navToolBar: Spinner = view.findViewById(R.id.toolbar_spinner)
        sortSpinner = view.findViewById(R.id.sortSpinner)
        searchBar = view.findViewById(R.id.editTextText2)
        setupNavigationSpinner(navToolBar, navItems) { selectedItem ->
            when (selectedItem) {
                "Measure" -> parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HomeFragment())
                    .commit()
            }
        }

        sessionAdapter = SessionAdapter(mutableListOf()) { session -> showPopup(session) }

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        sessionAdapter = SessionAdapter(sessionList) { session -> showPopup(session) }
        recyclerView.adapter = sessionAdapter

        fetchSessionSummaries()
        setupSearchBar()
        setupSortSpinner()

        return view
    }

    private fun setupSearchBar() {
        val suggestions = listOf(
            "Walk", "Run", "Turn left", "Turn right", "Walk up stairs", "Walk down stairs",
            "Google Pixel", "Samsung Galaxy", "iPhone",
        )
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            suggestions
        )
        searchBar.setAdapter(adapter)
        searchBar.doAfterTextChanged { editable ->
            val query = editable.toString().lowercase()
            displayedSessions = allSessions.filter { session ->
                session.chosenMeasurement?.lowercase()?.contains(query) == true ||
                        session.phoneModel.lowercase().contains(query) ||
                        session.gps?.any { gps ->
                            gps.latitude.toString().contains(query) ||
                                    gps.longitude.toString().contains(query)
                        } == true ||
                        session.compass?.any { compass ->
                            compass.compassData.toString().contains(query)
                        } == true ||
                        session.proximity?.any { prox ->
                            prox.proximity.toString().contains(query)
                        } == true ||
                        session.accelerometer?.any { accel ->
                            "${accel.accelX} ${accel.accelY} ${accel.accelZ}".contains(query)
                        } == true ||
                        session.gyroscope?.any { gyro ->
                            "${gyro.gyroX} ${gyro.gyroY} ${gyro.gyroZ}".contains(query)
                        } == true ||
                        (session.wifi?.any { wifi ->
                            (wifi.ssid?.lowercase() ?: "").contains(query) ||
                                    (wifi.rssi.toString()).contains(query) ||
                                    (wifi.status?.lowercase() ?: "").contains(query)
                        } == true) ||
                        (session.bluetooth?.any { bt ->
                            (bt.name?.lowercase() ?: "").contains(query) ||
                                    (bt.address?.lowercase() ?: "").contains(query) ||
                                    (bt.rssi.toString()).contains(query) ||
                                    (bt.status?.lowercase() ?: "").contains(query)
                        } == true) }
            applySorting()
        }
    }

    private fun setupSortSpinner() {
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                applySorting()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun applySorting() {
        displayedSessions = when (sortSpinner.selectedItem.toString()) {
            "Newest First" -> displayedSessions.sortedByDescending { it.startTime }
            "Oldest First" -> displayedSessions.sortedBy { it.startTime }
            else -> displayedSessions
        }
        updateRecyclerView(displayedSessions)
    }

    private fun fetchSessions() {
        apiClient.getData("api/sessions") { success, response ->
            activity?.runOnUiThread {
                if (success) {
                    try {
                        val sessions: List<Session> = mapper.readValue(response)
                        allSessions = sessions
                        displayedSessions = sessions
                        applySorting()
                    } catch (e: Exception) {
                        Log.e("ViewDataFragment", "Parse error", e)
                        Toast.makeText(requireContext(), "Failed to parse data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("ViewDataFragment", "Fetch failed: $response")
                    Toast.makeText(requireContext(), "Failed to fetch sessions", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchSessionSummaries() {
        apiClient.getSessionSummaries { success, response ->
            activity?.runOnUiThread {
                if (success) {
                    try {
                        val sessions: List<Session> = mapper.readValue(response)
                        allSessions = sessions
                        displayedSessions = sessions
                        applySorting()
                    } catch (e: Exception) {
                        Log.e("ViewDataFragment", "Parse error", e)
                        Toast.makeText(requireContext(), "Failed to parse data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("ViewDataFragment", "Fetch failed: $response")
                    Toast.makeText(requireContext(), "Failed to fetch sessions", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateRecyclerView(sessions: List<Session>) {
        sessionAdapter.updateData(sessions)
    }

    private fun showPopup(session: Session) {
        val gpsData = session.gps?.joinToString("\n") {
            "Lat: ${it.latitude}, Lon: ${it.longitude}, t=${it.timestamp}"
        } ?: "No GPS data"

        val compassData = session.compass?.joinToString("\n") {
            "Compass: ${it.compassData}, t=${it.timestamp}"
        } ?: "No Compass data"

        val proximityData = session.proximity?.joinToString("\n") {
            "Proximity: ${it.proximity}, t=${it.timestamp}"
        } ?: "No Proximity data"

        val accelData = session.accelerometer?.joinToString("\n") {
            "X=${it.accelX}, Y=${it.accelY}, Z=${it.accelZ}, t=${it.timestamp}"
        } ?: "No Accelerometer data"

        val gyroData = session.gyroscope?.joinToString("\n") {
            "X=${it.gyroX}, Y=${it.gyroY}, Z=${it.gyroZ}, t=${it.timestamp}"
        } ?: "No Gyroscope data"

        val wifiData = session.wifi?.joinToString("\n") {
            "SSID=${it.ssid}, RSSI=${it.rssi} dBm, Status=${it.status}, t=${it.timestamp}"
        } ?: "No Wi-Fi data"

        val bluetoothData = session.bluetooth?.joinToString("\n") {
            "Name=${it.name}, Addr=${it.address}, RSSI=${it.rssi} dBm, Status=${it.status}, t=${it.timestamp}"
        } ?: "No Bluetooth data"

        val message = """
        Model: ${session.phoneModel}
        Start: ${session.startTime}
        End: ${session.endTime}
        Measurement: ${session.chosenMeasurement}
        Frequency: ${session.frequency} Hz
        Comment: ${session.description}

        GPS: $gpsData

        Compass: $compassData

        Proximity: $proximityData

        Accelerometer: $accelData

        Gyroscope: $gyroData
        
        Wi-Fi: $wifiData
        
        Bluetooth: $bluetoothData
    """.trimIndent()

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.view_data_session, null)
        val textView = dialogView.findViewById<TextView>(R.id.sessionDetailsText)
        textView.text = message

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Session Details")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .setNegativeButton("Delete") { _, _ ->
                apiClient.deleteSession(session.id ?: "") { success, msg ->
                    activity?.runOnUiThread {
                        if (success) {
                            Toast.makeText(requireContext(), "Session deleted", Toast.LENGTH_SHORT).show()
                            allSessions = allSessions.filter { it.id != session.id }
                            displayedSessions = displayedSessions.filter { it.id != session.id }
                            updateRecyclerView(displayedSessions)
                        } else {
                            Toast.makeText(requireContext(), "Delete failed: $msg", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .show()
    }
}
