package com.crowdsensing

import android.Manifest
import android.content.Context.SENSOR_SERVICE
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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

    private val accelGravity = FloatArray(3)
    private val accelLin = FloatArray(3)
    private val alpha = 0.8f
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasMagnet = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

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
                switchCompass.thumbTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorSwitchOn)
                switchCompass.trackTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorTrackOn)
            } else {
                switchCompass.thumbTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorSwitchOff)
                switchCompass.trackTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorTrackOff)
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

        return view
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> {
                if (switchGyroscope.isChecked) gyro(event) else gyroscopeData.text = ""
            }

            Sensor.TYPE_ACCELEROMETER -> {
                if (switchAccelerometer.isChecked) {
                    accelerometer(event)
                    gravity[0] = event.values[0]
                    gravity[1] = event.values[1]
                    gravity[2] = event.values[2]
                    hasGravity = true
                    if (switchCompass.isChecked) updateCompass()
                } else accelerometerData.text = ""
            }

            Sensor.TYPE_PROXIMITY -> {
                if (switchProximity.isChecked) proximity(event) else proximityData.text = ""
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                if (switchCompass.isChecked) {
                    updateMagnetometer(event)
                    updateCompass()
                } else compassData.text = ""
            }
        }
    }

    private fun gyro(event: SensorEvent) {
        val (x, y, z) = event.values
        gyroscopeData.text = "Gyroscope:\nX: $x\nY: $y\nZ: $z"
    }

    private fun accelerometer(event: SensorEvent) {
        accelGravity[0] = alpha * accelGravity[0] + (1 - alpha) * event.values[0]
        accelGravity[1] = alpha * accelGravity[1] + (1 - alpha) * event.values[1]
        accelGravity[2] = alpha * accelGravity[2] + (1 - alpha) * event.values[2]

        accelLin[0] = event.values[0] - accelGravity[0]
        accelLin[1] = event.values[1] - accelGravity[1]
        accelLin[2] = event.values[2] - accelGravity[2]

        accelerometerData.text =
            "Accelerometer:\nX: ${accelLin[0]}\nY: ${accelLin[1]}\nZ: ${accelLin[2]}"
    }

    private fun proximity(event: SensorEvent) {
        val distance = event.values[0]
        proximityData.text = "Proximity: $distance cm"
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

    private fun getCompassDirection(azimuth: Float): String {
        return when (azimuth) {
            in 337.5..360.0, in 0.0..22.5 -> "North"
            in 22.5..67.5 -> "Northeast"
            in 67.5..112.5 -> "East"
            in 112.5..157.5 -> "Southeast"
            in 157.5..202.5 -> "South"
            in 202.5..247.5 -> "Southwest"
            in 247.5..292.5 -> "West"
            in 292.5..337.5 -> "Northwest"
            else -> "Unknown"
        }
    }

    private fun updateMagnetometer(event: SensorEvent) {
        geomagnetic[0] = event.values[0]
        geomagnetic[1] = event.values[1]
        geomagnetic[2] = event.values[2]
        hasMagnet = true
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

        override fun onPause() {
            super.onPause()
            sensorManager.unregisterListener(this)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }


