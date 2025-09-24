package com.crowdsensing

import ViewDataFragment
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
import com.crowdsensing.model.Session
import com.crowdsensing.model.Session.AccelerometerData
import com.crowdsensing.model.Session.CompassData
import com.crowdsensing.model.Session.GPSData
import com.crowdsensing.model.Session.GyroscopeData
import com.crowdsensing.model.Session.ProximityData
import com.crowdsensing.sensor.SensorController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import java.time.Instant
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts

class HomeFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var wifiScanner: WifiScanner
    private lateinit var bluetoothScanner: BluetoothScanner

    private var isRecording = false

    private lateinit var switchMap: Map<SensorType, Switch>
    private lateinit var textViewMap: Map<SensorType, TextView>
    private lateinit var sensorController: SensorController
    private lateinit var wifiData: TextView
    private lateinit var switchWifi: Switch
    private lateinit var inputSamplingRate: EditText
    private lateinit var buttonStop: Button
    private lateinit var buttonStart: Button
    private lateinit var useCaseSpinner: MaterialAutoCompleteTextView
    private lateinit var switchBlue: Switch
    private lateinit var bluetoothData: TextView

    private val gpsData = mutableListOf<GPSData>()
    private val compassData = mutableListOf<CompassData>()
    private val proximityData = mutableListOf<ProximityData>()
    private val accelerometerData = mutableListOf<AccelerometerData>()
    private val gyroscopeData = mutableListOf<GyroscopeData>()

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val wifiScanInterval: Long = 5000
    private val wifiScanHandler = Handler()
    private var startingTimeStamp: Instant? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val navToolBar: Spinner = view.findViewById(R.id.toolbar_spinner)
        val navItems = resources.getStringArray(R.array.spinner_items)

        setupNavigationSpinner(navToolBar, navItems) { selectedItem ->
            if (selectedItem == "View Measurements") {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, ViewDataFragment())
                    .commit()
            }
        }

        useCaseSpinner = view.findViewById(R.id.useCase_spinner)
        setupDropdownMenu(requireContext(), useCaseSpinner, resources.getStringArray(R.array.spinner_itemsAction))

        wifiData = view.findViewById(R.id.textViewWifi)
        switchWifi = view.findViewById(R.id.switchWifi)
        inputSamplingRate = view.findViewById(R.id.input_sampling_rate)
        buttonStart = view.findViewById(R.id.buttonStart)
        buttonStop = view.findViewById(R.id.buttonStop)
        switchBlue = view.findViewById(R.id.switchBluetooth)
        bluetoothData = view.findViewById(R.id.textViewBluetooth)

        switchMap = mapOf(
            SensorType.GYROSCOPE to view.findViewById(R.id.switchGyro),
            SensorType.ACCELEROMETER to view.findViewById(R.id.switchAccelerometer),
            SensorType.PROXIMITY to view.findViewById(R.id.switchProximity),
            SensorType.MAGNETIC_FIELD to view.findViewById(R.id.switchCompass),
            SensorType.GPS to view.findViewById(R.id.switchGPS)
        )

        textViewMap = mapOf(
            SensorType.GYROSCOPE to view.findViewById(R.id.textViewGyro),
            SensorType.ACCELEROMETER to view.findViewById(R.id.textViewAccelerometer),
            SensorType.PROXIMITY to view.findViewById(R.id.textViewProximity),
            SensorType.MAGNETIC_FIELD to view.findViewById(R.id.textViewCompass),
            SensorType.GPS to view.findViewById(R.id.textViewGPS)
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
            override fun onSensorData(sensorType: SensorType, data: Any) {
                onSensorDataUpdated(sensorType, data)
            }
        })

        bluetoothScanner = BluetoothScanner(requireContext()) { result ->
            bluetoothData.text = result
            bluetoothData.visibility = if (result.isNotBlank()) View.VISIBLE else View.GONE
        }

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
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                }
            } else {
                wifiScanner.stop()
                wifiData.text = ""
                wifiData.visibility = View.GONE
            }
        }

        switchBlue.setOnCheckedChangeListener { _, isChecked ->
            updateSwitchColors(switchBlue, isChecked, requireContext())
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    bluetoothScanner.start()
                } else {
                    requestPermissionsLauncher.launch(
                        arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }
            } else {
                bluetoothScanner.stop()
                bluetoothData.text = ""
                bluetoothData.visibility = View.GONE
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
            isRecording = false
            sensorController.stopSensors()
            wifiScanner.stop()
            bluetoothScanner.stop()

            val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
            val selectedUseCase = useCaseSpinner.text.toString()

            val sessionRecording = Session(
                phoneModel = deviceModel,
                startTime = startingTimeStamp ?: Instant.now(),
                endTime = Instant.now(),
                description = "",
                chosenMeasurement = selectedUseCase,
                frequency = inputSamplingRate.text.toString().toLongOrNull() ?: 0L,
                gps = gpsData,
                compass = compassData,
                proximity = proximityData,
                accelerometer = accelerometerData,
                gyroscope = gyroscopeData
            )

            Log.i("recordedSession", sessionRecording.toString())

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, NewDataFragment.newInstance(sessionRecording))
                .addToBackStack(null)
                .commit()
        }
    }

    fun onSensorDataUpdated(sensorType: SensorType, data: Any) {
        activity?.runOnUiThread {
            if (isRecording) {
                when (data) {
                    is AccelerometerData -> accelerometerData.add(data)
                    is GyroscopeData -> gyroscopeData.add(data)
                    is ProximityData -> proximityData.add(data)
                    is CompassData -> compassData.add(data)
                    is GPSData -> gpsData.add(data)
                }
            }

            val switch = switchMap[sensorType]
            val textView = textViewMap[sensorType]

            if (switch?.isChecked == true) {
                textView?.visibility = View.VISIBLE
                textView?.text = when (data) {
                    is CompassData -> {
                        val azimuth = data.compassData
                        val direction = when {
                            azimuth in 337.5..360.0 || azimuth in 0.0..22.5 -> "North"
                            azimuth in 22.5..67.5 -> "Northeast"
                            azimuth in 67.5..112.5 -> "East"
                            azimuth in 112.5..157.5 -> "Southeast"
                            azimuth in 157.5..202.5 -> "South"
                            azimuth in 202.5..247.5 -> "Southwest"
                            azimuth in 247.5..292.5 -> "West"
                            azimuth in 292.5..337.5 -> "Northwest"
                            else -> ""
                        }
                        "CompassData( $direction (${azimuth.toInt()}°))"
                    }
                    is GPSData -> "GPSData( Lat: ${data.latitude}, Lng: ${data.longitude})"
                    else -> data.toString()
                }
            } else {
                textView?.visibility = View.GONE
            }
        }
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                wifiScanner.start()
            } else {
                wifiData.text = "Wi-Fi/GPS: Permission denied"
            }
            val bluetoothScanGranted = permissions[Manifest.permission.BLUETOOTH_SCAN] == true
            val bluetoothConnectGranted = permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
            if (bluetoothScanGranted && bluetoothConnectGranted) {
                bluetoothScanner.start()
            } else {
                bluetoothData.text = "Bluetooth: Permission denied"
            }
        }

    private fun startRecording(rate: Long) {
        if (switchMap[SensorType.GPS]?.isChecked == true &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        isRecording = true
        clearData()
        startingTimeStamp = Instant.now()

        val selectedSensors = switchMap.filterValues { it.isChecked }.keys
        sensorController.startSensors(selectedSensors, rate)

        if (switchWifi.isChecked) wifiScanner.start()
    }

    private fun clearData() {
        accelerometerData.clear()
        gyroscopeData.clear()
        proximityData.clear()
        compassData.clear()
        gpsData.clear()
    }

    private fun stopRecording() {
        isRecording = false
        sensorController.stopSensors()
        wifiScanner.stop()
        bluetoothScanner.stop()
    }

    override fun onPause() {
        super.onPause()
        if (isRecording) {
            stopRecording()
        } else {
            wifiScanner.stop()
            bluetoothScanner.stop()
        }
    }
}
