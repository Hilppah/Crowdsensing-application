package com.crowdsensing

import android.Manifest
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.MaterialAutoCompleteTextView


class HomeFragment : Fragment(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null
    private var accelerometer: Sensor? = null
    private var proximity: Sensor? = null
    private var magnetometer: Sensor? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var wifiManager: android.net.wifi.WifiManager
    private lateinit var wifiScanner: WifiScanner

    private var lastSavedTime: Long = 0
    private var saveIntervalMillis: Long = 500
    private var isRecording = false

    private lateinit var gyroscopeData: TextView
    private lateinit var accelerometerData: TextView
    private lateinit var gpsData: TextView
    private lateinit var proximityData: TextView
    private lateinit var compassData: TextView
    private lateinit var navToolBar: Spinner
    private lateinit var switchGyroscope: Switch
    private lateinit var switchAccelerometer: Switch
    private lateinit var switchGPS: Switch
    private lateinit var switchProximity: Switch
    private lateinit var switchCompass: Switch
    private lateinit var wifiData: TextView
    private lateinit var switchWifi: Switch
    private lateinit var inputSamplingRate: EditText
    private lateinit var buttonStop: Button
    private lateinit var buttonStart: Button

    val sensorMeasurements = mutableListOf<SensorMeasurement>()
    private val accelGravity = FloatArray(3)
    private val accelLin = FloatArray(3)
    private val alpha = 0.8f
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasMagnet = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val wifiScanInterval: Long = 5000
    private val wifiScanHandler = android.os.Handler()


    data class SensorMeasurement(
        val type: String,
        val timestamp: Long,
        val values: FloatArray
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val navItems = resources.getStringArray(R.array.spinner_items)
        navToolBar = view.findViewById(R.id.toolbar_spinner)
        val navAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, navItems)
        navAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        navToolBar.adapter = navAdapter

        val useCaseSpinner = view.findViewById<MaterialAutoCompleteTextView>(R.id.useCase_spinner)

        val useCaseItems = resources.getStringArray(R.array.spinner_itemsAction)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            useCaseItems
        )

        wifiScanner = WifiScanner(
            context = requireContext(),
            wifiManager = wifiManager,
            handler = wifiScanHandler,
            interval = wifiScanInterval
        ) { result ->
            wifiData.text = result
            wifiData.visibility = if (result.isNotBlank()) View.VISIBLE else View.GONE
        }

        useCaseSpinner.setAdapter(adapter)
        useCaseSpinner.setDropDownBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.dropdown)
        )
        useCaseSpinner.setOnClickListener {
            useCaseSpinner.showDropDown()
        }

        useCaseSpinner.setDropDownBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.dropdown)
        )

        useCaseSpinner.setAdapter(adapter)
        useCaseSpinner.setDropDownBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.dropdown)
        )

        gyroscopeData = view.findViewById(R.id.textViewGyro)
        accelerometerData = view.findViewById(R.id.textViewAccelerometer)
        gpsData = view.findViewById(R.id.textViewGPS)
        proximityData = view.findViewById(R.id.textViewProximity)
        compassData = view.findViewById(R.id.textViewCompass)
        navToolBar = view.findViewById(R.id.toolbar_spinner)
        switchGyroscope = view.findViewById(R.id.switchGyro)
        switchAccelerometer = view.findViewById(R.id.switchAccelerometer)
        switchGPS = view.findViewById(R.id.switchGPS)
        switchProximity = view.findViewById(R.id.switchProximity)
        switchCompass = view.findViewById(R.id.switchCompass)
        wifiData = view.findViewById(R.id.textViewWifi)
        switchWifi = view.findViewById(R.id.switchWifi)
        wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
        inputSamplingRate = view.findViewById(R.id.input_sampling_rate)
        buttonStop =view.findViewById(R.id.buttonStop)
        buttonStart=view.findViewById(R.id.buttonStart)

        sensorManager = requireContext().getSystemService(SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        switchGyroscope.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchGyroscope.thumbTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorSwitchOn)
                switchGyroscope.trackTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorTrackOn)
            } else {
                switchGyroscope.thumbTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorSwitchOff)
                switchGyroscope.trackTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorTrackOff)
            }
        }

        switchWifi.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchWifi.thumbTintList = ContextCompat.getColorStateList(requireContext(), R.color.colorSwitchOn)
                switchWifi.trackTintList = ContextCompat.getColorStateList(requireContext(), R.color.colorTrackOn)

                if (checkLocationPermission()) {
                    wifiScanner.start()()
                } else {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                }
            } else{
                wifiScanner.stop()
                wifiData.text =""
                wifiData.visibility = View.GONE
                switchWifi.thumbTintList = ContextCompat.getColorStateList(requireContext(), R.color.colorSwitchOff)
                switchWifi.trackTintList = ContextCompat.getColorStateList(requireContext(), R.color.colorTrackOff)
            }
        }

        switchAccelerometer.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchAccelerometer.thumbTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorSwitchOn)
                switchAccelerometer.trackTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorTrackOn)
            } else {
                switchAccelerometer.thumbTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorSwitchOff)
                switchAccelerometer.trackTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorTrackOff)
            }
        }
        switchGPS.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchGPS.thumbTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorSwitchOn)
                switchGPS.trackTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorTrackOn)

                if (checkLocationPermission()) {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            location?.let {
                                val latitude = it.latitude
                                val longitude = it.longitude
                                gpsData.text = "GPS:\nLat: $latitude\nLon: $longitude"
                            } ?: run {
                                gpsData.text = "GPS: Location unavailable"
                            }
                        }
                } else {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            } else {
                switchGPS.thumbTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorSwitchOff)
                switchGPS.trackTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorTrackOff)
                gpsData.text = ""
            }
        }
        switchProximity.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchProximity.thumbTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorSwitchOn)
                switchProximity.trackTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorTrackOn)
            } else {
                switchProximity.thumbTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorSwitchOff)
                switchProximity.trackTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorTrackOff)
            }
        }

        switchCompass.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchCompass.thumbTintList = ContextCompat.getColorStateList(requireContext(), R.color.colorSwitchOn)
                switchCompass.trackTintList = ContextCompat.getColorStateList(requireContext(), R.color.colorTrackOn)
            } else {
                compassData.text = ""
                compassData.visibility = View.GONE
                switchCompass.thumbTintList = ContextCompat.getColorStateList(requireContext(), R.color.colorSwitchOff)
                switchCompass.trackTintList = ContextCompat.getColorStateList(requireContext(), R.color.colorTrackOff)
            }
        }

        navToolBar.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = parent.getItemAtPosition(position).toString()

                    if (selectedItem == "Search Measurements") {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, ViewDataFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
            }

        buttonStart.setOnClickListener {
            val samplingRateText = inputSamplingRate.text.toString()
            val rate = samplingRateText.toIntOrNull()

            if (rate == null || rate <= 0) {
                inputSamplingRate.error = "Enter a positive number"
                return@setOnClickListener
            }

            val samplingRateHz = rate.coerceAtMost(5) // Max 5 Hz
            saveIntervalMillis = 1000L / samplingRateHz
            lastSavedTime = 0
            isRecording = true
            sensorMeasurements.clear()

            val samplingPeriodUs = 1_000_000 / samplingRateHz
            registerSensors(samplingPeriodUs)
        }

        buttonStop.setOnClickListener {
            isRecording = false
            sensorManager.unregisterListener(this)
            wifiScanner.stop()
            val fragment = SaveMeasurementFragment.newInstance(sensorMeasurements)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
        sensorManager.unregisterListener(this)

        return view
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val currentTime = System.currentTimeMillis()
        val shouldSave = isRecording && (currentTime - lastSavedTime >= saveIntervalMillis)

        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> {
                if (switchGyroscope.isChecked) {
                    val output = Sensors.sensorGyroscope(event)
                    gyroscopeData.text = output
                    gyroscopeData.visibility = View.VISIBLE
                    if (shouldSave) saveSensorData("Gyroscope", event)
                } else gyroscopeData.visibility = View.GONE
            }

            Sensor.TYPE_ACCELEROMETER -> {
                if (switchAccelerometer.isChecked) {
                    val output = Sensors.sensorAccelerometer(event)
                    accelerometerData.text = output
                    accelerometerData.visibility = View.VISIBLE
                    if (shouldSave) saveSensorData("Accelerometer", event)
                } else accelerometerData.visibility = View.GONE

                if (switchCompass.isChecked) {
                    Sensors.sensorAccelerometer(event)
                    updateCompass()
                }
            }

            Sensor.TYPE_PROXIMITY -> {
                if (switchProximity.isChecked) {
                    val output = Sensors.sensorProximity(event)
                    proximityData.text = output
                    proximityData.visibility = View.VISIBLE
                    if (shouldSave) saveSensorData("Proximity", event)
                } else {
                    proximityData.text = ""
                    proximityData.visibility = View.GONE
                }
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                if (switchCompass.isChecked) {
                    Sensors.sensorMagnetometer(event)
                    updateCompass()
                    if (shouldSave) saveSensorData("Compass", event)
                } else {
                    compassData.text = ""
                    compassData.visibility = View.GONE
                }
            }
        }

        if (shouldSave) lastSavedTime = currentTime
    }

    private fun saveSensorData(sensorType: String, event: SensorEvent) {
        sensorMeasurements.add(
            SensorMeasurement(sensorType, System.currentTimeMillis(), event.values.clone())
        )
    }


    private fun scanWifiNetworks() {
        if (!checkLocationPermission()) {
            wifiData.text = "Wi-Fi Location permission denied"
            return
        }

        try {
            val success = wifiManager.startScan()
            if (!success) {
                wifiData.text = "Wi-Fi scan failed"
                return
            }

            val results = wifiManager.scanResults
            if (results.isNotEmpty()) {
                val sb = StringBuilder()
                sb.append("Nearby Wi-Fi Networks:\n")
                results.take(5).forEach { result -> sb.append("${result.SSID} - ${result.level} dBm\n")
                }
                wifiData.text = sb.toString()
                wifiData.visibility = View.VISIBLE
            } else {
                wifiData.text = "No Wi-Fi networks found" }
        } catch (e: SecurityException) {
            wifiData.text = "Wi-Fi scan failed: permission denied"
            e.printStackTrace()
        }
    }

    private fun updateCompass() {
        if (!hasGravity || !hasMagnet) return

        val R = FloatArray(9)
        val I = FloatArray(9)

        if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(R, orientation)
            val azimuthRad = orientation[0]
            val azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            val azimuthNormalized = (azimuthDeg + 360) % 360

            val direction = getCompassDirection(azimuthNormalized)
            compassData.text = "Compass: ${azimuthNormalized.toInt()}° ($direction)"
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    override fun onResume() {
        super.onResume()
        gyroscope?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        proximity?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        magnetometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (!checkLocationPermission()) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)

            if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    onResume()
                } else {
                    gpsData.text = "GPS: Permission denied"
                }
            }
        }

    private fun registerSensors(samplingPeriodUs: Int) {
        gyroscope?.also {
            sensorManager.registerListener(this, it, samplingPeriodUs)
        }
        accelerometer?.also {
            sensorManager.registerListener(this, it, samplingPeriodUs)
        }
        proximity?.also {
            sensorManager.registerListener(this, it, samplingPeriodUs)
        }
        magnetometer?.also {
            sensorManager.registerListener(this, it, samplingPeriodUs)
        }}

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        wifiScanHandler.removeCallbacks(wifiScanRunnable)
    }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }


