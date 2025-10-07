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
    private val mapper = jacksonObjectMapper().apply { findAndRegisterModules() }
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
                        Toast.makeText(requireContext(), "Failed to parse data", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Log.e("ViewDataFragment", "Fetch failed: $response")
                    Toast.makeText(requireContext(), "Failed to fetch sessions", Toast.LENGTH_SHORT)
                        .show()
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
                        Toast.makeText(requireContext(), "Failed to parse data", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Log.e("ViewDataFragment", "Fetch failed: $response")
                    Toast.makeText(requireContext(), "Failed to fetch sessions", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun updateRecyclerView(sessions: List<Session>) {
        sessionAdapter.updateData(sessions)
    }

    private fun showPopup(session: Session) {
        apiClient.getData("api/sessions/${session.id}") { success, response ->
            if (success) {
                Thread {
                    try {
                        val fullSession: Session = mapper.readValue(
                            jacksonObjectMapper().readTree(response)["session"].toString()
                        )
                        activity?.runOnUiThread {
                            displaySessionPopup(fullSession)
                        }
                    } catch (e: Exception) {
                        activity?.runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Failed to parse session data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }.start()
            } else {
                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Failed to fetch session details",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    class KeyValueAdapter(private val items: List<Pair<String, String>>) :
        RecyclerView.Adapter<KeyValueAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val keyText: TextView = view.findViewById(R.id.keyText)
            val valueText: TextView = view.findViewById(R.id.valueText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_key_value, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val (key, value) = items[position]
            holder.keyText.text = key
            holder.valueText.text = value
        }
    }

    private fun displaySessionPopup(session: Session) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.view_data_session, null)
        dialogView.findViewById<TextView>(R.id.modelText).text = "Model: ${session.phoneModel}"
        dialogView.findViewById<TextView>(R.id.timeText).text =
            "Start: ${session.startTime}, End: ${session.endTime}, Measurement: ${session.chosenMeasurement}"

        fun setupRecycler(recyclerId: Int, items: List<Pair<String, String>>) {
            val recycler = dialogView.findViewById<RecyclerView>(recyclerId)
            recycler.layoutManager = LinearLayoutManager(requireContext())
            recycler.adapter = KeyValueAdapter(items)
        }

        setupRecycler(
            R.id.gpsRecycler,
            session.gps?.map { "t=${it.timestamp}" to "Lat=${it.latitude}, Lon=${it.longitude}" } ?: emptyList()
        )
        setupRecycler(
            R.id.proximityRecycler,
            session.proximity?.map { "t=${it.timestamp}" to "Proximity=${it.proximity}" } ?: emptyList()
        )
        setupRecycler(
            R.id.accelRecycler,
            session.accelerometer?.map { "t=${it.timestamp}" to "X=${it.accelX}, Y=${it.accelY}, Z=${it.accelZ}" } ?: emptyList()
        )
        setupRecycler(
            R.id.gyroRecycler,
            session.gyroscope?.map { "t=${it.timestamp}" to "X=${it.gyroX}, Y=${it.gyroY}, Z=${it.gyroZ}" } ?: emptyList()
        )
        setupRecycler(
            R.id.compassRecycler,
            session.compass?.map { "t=${it.timestamp}" to "Compass=${it.compassData}" } ?: emptyList()
        )
        setupRecycler(
            R.id.wifiRecycler,
            session.wifi?.map { "t=${it.timestamp}" to "SSID=${it.ssid}, RSSI=${it.rssi}, Status=${it.status}" } ?: emptyList()
        )
        setupRecycler(
            R.id.blueRecycler,
            session.bluetooth?.map { "t=${it.timestamp}" to "Name=${it.name}, Addr=${it.address}, RSSI=${it.rssi}, Status=${it.status}" } ?: emptyList()
        )

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
