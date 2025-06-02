package com.crowdsensing

import android.Manifest
import android.content.Context
import android.content.Context.SENSOR_SERVICE
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
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

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

    private val accelGravity = FloatArray(3)
    private val accelLin = FloatArray(3)
    private val alpha = 0.8f
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasMagnet = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        gyroscopeData = view.findViewById(R.id.textViewGyro)
        accelerometerData = view.findViewById(R.id.textViewAccelerometer)
        gpsData = view.findViewById(R.id.textViewGPS)
        proximityData = view.findViewById(R.id.textViewProximity)
        compassData = view.findViewById(R.id.textViewCompass)

        sensorManager = requireContext().getSystemService(SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        return view
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> gyro(event)
            Sensor.TYPE_ACCELEROMETER -> accelerometer(event)
            Sensor.TYPE_PROXIMITY -> proximity(event)
            Sensor.TYPE_PROXIMITY -> proximity(event)
            Sensor.TYPE_MAGNETIC_FIELD -> updateMagnetometer(event)
        }
        updateCompass()


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

        accelerometerData.text = "Accelerometer:\nX: ${accelLin[0]}\nY: ${accelLin[1]}\nZ: ${accelLin[2]}"
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
            compassData.text = "Compass: ${azimuthNormalized.toInt()}°"
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
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

