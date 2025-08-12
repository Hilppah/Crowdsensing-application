package com.crowdsensing

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.crowdsensing.Sensors.SensorType
import com.crowdsensing.ViewUtils.setupDropdownMenu
import com.crowdsensing.ViewUtils.setupNavigationSpinner
import com.crowdsensing.ViewUtils.updateSwitchColors
import com.crowdsensing.sensor.SensorController
import com.crowdsensing.sensor.SensorResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class HomeFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var wifiScanner: WifiScanner

    private var isRecording = false

    private lateinit var switchMap: Map<SensorType, Switch>
    private lateinit var textViewMap: Map<SensorType, TextView>
    private lateinit var sensorController: SensorController
    private lateinit var wifiData: TextView
    private lateinit var switchWifi: Switch
    private lateinit var inputSamplingRate: EditText
    private lateinit var buttonStop: Button
    private lateinit var buttonStart: Button

    val fragment = NewDataFragment.newInstance(
        sensorData = "hiiii test"
    )

    private val sensorMeasurements =mutableListOf<SensorResult>()
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val wifiScanInterval: Long = 5000
    private val wifiScanHandler = Handler()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

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

        val useCaseSpinner = view.findViewById<MaterialAutoCompleteTextView>(R.id.useCase_spinner)
        setupDropdownMenu(requireContext(), useCaseSpinner, resources.getStringArray(R.array.spinner_itemsAction))

        wifiData = view.findViewById(R.id.textViewWifi)
        switchWifi = view.findViewById(R.id.switchWifi)
        inputSamplingRate = view.findViewById(R.id.input_sampling_rate)
        buttonStart = view.findViewById(R.id.buttonStart)
        buttonStop = view.findViewById(R.id.buttonStop)

        switchMap = mapOf(
            SensorType.GYROSCOPE to view.findViewById(R.id.switchGyro),
            SensorType.ACCELEROMETER to view.findViewById(R.id.switchAccelerometer),
            SensorType.PROXIMITY to view.findViewById(R.id.switchProximity),
            SensorType.MAGNETIC_FIELD to view.findViewById(R.id.switchCompass)
        )

        textViewMap = mapOf(
            SensorType.GYROSCOPE to view.findViewById(R.id.textViewGyro),
            SensorType.ACCELEROMETER to view.findViewById(R.id.textViewAccelerometer),
            SensorType.PROXIMITY to view.findViewById(R.id.textViewProximity),
            SensorType.MAGNETIC_FIELD to view.findViewById(R.id.textViewCompass)
        )

        wifiScanner = WifiScanner(
            requireContext(),
            requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager,
            wifiScanHandler,
            wifiScanInterval
        ) { result ->
            wifiData.text = result
            wifiData.visibility = if (result.isNotBlank()) View.VISIBLE else View.GONE
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        sensorController = SensorController(requireContext(), object : SensorController.SensorDataListener {
            override fun onSensorData(sensorType: SensorType, result: SensorResult) {
                onSensorDataUpdated(sensorType, result)
            }
        })

        setupSwitchListeners()
        setupButtons()

        return view
    }

    private fun setupSwitchListeners() {
        switchWifi.setOnCheckedChangeListener { _, isChecked ->
            updateSwitchColors(switchWifi, isChecked, requireContext())
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    wifiScanner.start()
                } else {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            } else {
                wifiScanner.stop()
                wifiData.text = ""
                wifiData.visibility = View.GONE
            }
        }

        switchMap.forEach { (sensorType, sw) ->
            sw.setOnCheckedChangeListener { _, isChecked ->
                updateSwitchColors(sw, isChecked, requireContext())

                val textView = textViewMap[sensorType]
                if (!isChecked) {
                    textView?.text = ""
                    textView?.visibility = View.GONE
                }
            }
        }
    }

    private fun logSensorDataToLogcat() {
        val tag = "SensorDataLogger"
        Log.i(tag, "Type,Timestamp,Values")
        sensorMeasurements.forEach {
            val valuesString = it.values.joinToString(";")
            Log.i(tag, "${it.display},${it.display},$valuesString")
        }
        Toast.makeText(requireContext(), "Sensor data logged to Logcat", Toast.LENGTH_SHORT).show()
    }

    private fun setupButtons() {
        buttonStart.setOnClickListener {
            val rate = inputSamplingRate.text.toString().toLongOrNull()
            if (rate == null || rate <= 0) {
                inputSamplingRate.error = "Enter a positive number"
                return@setOnClickListener
            }
            startRecording(rate)
        }

        buttonStop.setOnClickListener {
            stopRecording()
            val recordedData = sensorMeasurements.joinToString("\n") {
                "${it.display} -> ${it.values.joinToString(", ")}"
            }

            val newDataFragment = NewDataFragment().apply {
                arguments = Bundle().apply {
                    putString("sensor_data", recordedData)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer,
                    NewDataFragment.newInstance("Measured at: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}")
                )
                .addToBackStack(null)
                .commit()
        }
    }

    fun onSensorDataUpdated(sensorType: SensorType, result: SensorResult) {
        activity?.runOnUiThread {
            if (isRecording) {
                sensorMeasurements.add(
                    SensorResult(sensorType.name, result.values)
                )
            }

            val switch = switchMap[sensorType]
            val textView = textViewMap[sensorType]
            val isOn = switch?.isChecked == true

            if (isOn) {
                textView?.text = result.display
                textView?.visibility = View.VISIBLE
            } else {
                textView?.visibility = View.GONE
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                wifiScanner.start()
            } else {
                wifiData.text = "Wi-Fi: Permission denied"
            }
        }
    }

    private fun startRecording(rate: Long) {
        isRecording = true
        sensorMeasurements.clear()

        val selectedSensors = switchMap.filterValues { it.isChecked }.keys
        sensorController.startSensors(selectedSensors, rate)

        if (switchWifi.isChecked) {
            wifiScanner.start()
        }
    }

    private fun stopRecording() {
        isRecording = false
        sensorController.stopSensors()
        wifiScanner.stop()
        logSensorDataToLogcat()
        sensorMeasurements.clear()
    }

    override fun onPause() {
        super.onPause()
        if (isRecording) {
            stopRecording()
        } else {
            wifiScanner.stop()
        }
    }
}
